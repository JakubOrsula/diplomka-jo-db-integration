package com.example.services.storage;

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

    public void removeProteinChainsWithoutFile(boolean dry_run) {
        ScrollableResults results = session.createQuery("from ProteinChain").scroll(ScrollMode.FORWARD_ONLY);
        var total = 0;
        var pivots = 0;
        var totalMissing = 0;
        while (results.next()) {
            total += 1;
            ProteinChain proteinChain = (ProteinChain) results.get(0);

            String gesamtId = proteinChain.getGesamtId();
            //skip pivots
            if (gesamtId.startsWith("@")) {
                pivots += 1;
                continue;
            }
            String fileName =
                    AppConfig.PDBE_BINARY_FILES_DIR + "/" +
                    gesamtId.substring(1, 3).toLowerCase() + "/" +
                    gesamtId + ".bin";

            File file = new File(fileName);
            if (!file.exists()) {
                totalMissing += 1;
                if (dry_run) {
                    System.out.println(fileName + " not found");
                } else {
                    session.delete(proteinChain);
                    session.flush();
                    session.clear();
                }
            }
        }
        System.out.println(totalMissing + "/" + total + " missing");
        System.out.println(pivots + " pivots skipped");
    }

    public void checkGesamtIdInDatabase(File rootDirectory) {
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
                            totalFiles++; // Increment total files counter

                            String fileName = file.getName();
                            String gesamtId = fileName.substring(0, fileName.lastIndexOf('.'));

                            ProteinChain proteinChain = session.createQuery("FROM ProteinChain WHERE gesamtId = :gesamtId", ProteinChain.class)
                                    .setParameter("gesamtId", gesamtId)
                                    .uniqueResult();

                            if (proteinChain == null) {
                                missingFiles++;
                                // Perform desired operations if gesamtId is not in the database
                                System.out.println("GesamtId " + gesamtId + " not found in the database. File: " + fileName);
                            }
                        } else if (file.isDirectory()) {
                            stack.push(file);
                        }
                    }
                }
            }
        }

        System.out.println("Total files processed: " + totalFiles);
        System.out.println("Missing files: " + missingFiles);
    }

}
