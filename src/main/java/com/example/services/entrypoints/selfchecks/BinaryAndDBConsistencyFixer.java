package com.example.services.entrypoints.selfchecks;

import com.example.model.ProteinChain;
import com.example.services.configuration.AppConfig;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import java.io.File;
import java.util.Deque;
import java.util.LinkedList;

public class BinaryAndDBConsistencyFixer {
    private final Session session;

    public BinaryAndDBConsistencyFixer(Session session) {
        this.session = session;
    }

    public void removeProteinChainsWithoutFile(boolean dryRun) {
        ScrollableResults results = session.createQuery("from ProteinChain where indexedAsDataObject = true").scroll(ScrollMode.FORWARD_ONLY);
        var total = 0;
        var totalMissing = 0;
        while (results.next()) {
            total += 1;
            if (total % 1000 == 0) {
                System.out.println("Processed " + total / 1000 + "k chains");
            }
            ProteinChain proteinChain = (ProteinChain) results.get(0);

            String gesamtId = proteinChain.getGesamtId();
            //skip pivots
            if (gesamtId.startsWith("@")) {
                continue;
            }
            String fileName =
                    AppConfig.DATASET_BINARY_DIR + "/" +
                    gesamtId.substring(1, 3).toLowerCase() + "/" +
                    gesamtId + ".bin";

            File file = new File(fileName);
            if (!file.exists()) {
                totalMissing += 1;
                System.out.println(fileName + " not found");
                if (dryRun) {

                } else {
                    session.beginTransaction();
                    proteinChain.setIndexedAsDataObject(false);
                    session.getTransaction().commit();
                }
            }
        }
        System.out.println(totalMissing + "/" + total + " missing");
    }

    public void CheckGesamtBinaryFilesAreInDB(File rootDirectory) {
        System.out.println("Checking if all gesamt binary files are in the database");
        Deque<File> stack = new LinkedList<>();
        stack.push(rootDirectory);

        int totalFiles = 0;
        int missingFiles = 0;

        while (!stack.isEmpty()) {
            File directory = stack.pop();
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            totalFiles++;
                            if (totalFiles % 1000 == 0) {
                                System.out.println("Processed " + totalFiles/1000 + "k files");
                            }

                            String fileName = file.getName();
                            String gesamtId = fileName.substring(0, fileName.lastIndexOf('.'));

                            ProteinChain proteinChain = session.createQuery("FROM ProteinChain WHERE gesamtId = :gesamtId", ProteinChain.class)
                                    .setParameter("gesamtId", gesamtId)
                                    .uniqueResult();

                            if (proteinChain == null) {
                                missingFiles++;

                                System.out.println("GesamtId " + gesamtId + " not found in the database. File: " + fileName);
                            }
                        } else if (file.isDirectory()) {
                            stack.push(file);
                        }
                    }
                }
            }
        }

        System.out.println("Missing files: " + missingFiles);
        System.out.println("Total files processed: " + totalFiles);
    }

    public void CheckCifFilesHaveAtLeastOneChainFile(File rootDirectory) {
        System.out.println("Checking if all cif files have at least one chain file");
        Deque<File> stack = new LinkedList<>();
        stack.push(rootDirectory);

        int totalFiles = 0;
        int missingFiles = 0;

        while (!stack.isEmpty()) {
            File directory = stack.pop();
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File cifFile : files) {
                        if (cifFile.isFile()) {
                            totalFiles++;
                            if (totalFiles % 1000 == 0) {
                                System.out.println("Processed " + totalFiles/1000 + "k files");
                            }

                            String cifFileName = cifFile.getName();
                            String cifname = cifFileName.substring(0, cifFileName.lastIndexOf('.')).toUpperCase();

                            var middle_slug = cifFile.getParentFile().getName();
                            var associated_bins = new File(AppConfig.DATASET_BINARY_DIR + "/" + middle_slug);
                            if (!associated_bins.exists()) {
                                System.out.println("Associated bins dir does not exist: " + associated_bins.getAbsolutePath());
                                missingFiles++;
                                continue;
                            }
                            
                            var bin_files = associated_bins.listFiles();
                            var seenFlag = false;
                            for (var bin_file : bin_files) {
                                var bin_file_name = bin_file.getName();
                                if (bin_file_name.substring(0, bin_file_name.lastIndexOf(':')).equals(cifname)) {
                                    seenFlag = true;
                                    break;
                                }
                            }
                            if (!seenFlag) {
                                missingFiles++;
                                System.out.println("Missing bin file for " + cifFile.getAbsolutePath());
                                if (!AppConfig.DRY_RUN) {
                                    if (missingFiles > 10000) {
                                        System.out.println("In this run 10k cif files were removed. It is likely that something went wrong. Aborting.");
                                        System.out.println("If you are sure that everything is fine, run this program again");
                                        return;
                                    }
                                    if (!cifFile.delete()) {
                                        System.out.println("Failed to delete " + cifFile.getAbsolutePath());
                                    }
                                }
                            }
                            
                        } else if (cifFile.isDirectory()) {
                            stack.push(cifFile);
                        }
                    }
                }
            }
        }

        System.out.println("Missing files: " + missingFiles);
        System.out.println("Total files processed: " + totalFiles);
    }

}
