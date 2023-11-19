package com.example;

import com.beust.jcommander.JCommander;
import com.example.services.configuration.AppConfig;
import com.example.services.configuration.Args;
import com.example.services.entrypoints.applySketches.ApplySketches;
import com.example.services.entrypoints.generatePivotCsvs.GeneratePivotCsvs;
import com.example.services.entrypoints.generateSubConfigs.GenerateSubConfigs;
import com.example.services.entrypoints.ground_truth.GroundTruth;
import com.example.services.entrypoints.runMessiff.RunMessiff;
import com.example.services.entrypoints.selfchecks.ConsistencyCheck;
import com.example.services.entrypoints.distanceComputation.DistanceComputation;
import com.example.services.entrypoints.learnSketches.LearnSketches;
import com.example.services.entrypoints.secondaryFiltering.LearnSecondaryFilteringWithGHPSketches;
import com.example.services.entrypoints.selfchecks.GesamtLibIntegrationCheck;
import com.example.services.entrypoints.updateDataset.UpdateDataset;
import com.example.services.utils.JavaUtils;
import org.hibernate.SessionFactory;

import java.io.*;

import static com.example.services.utils.DatabaseUtils.buildSessionFactory;
import static com.example.services.utils.DatabaseUtils.migrate;

/**
 * Hello world!
 *
 */
public class CliApp
{

    private static SessionFactory sessionFactory = null;

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Checks whether all distances were computed and if not how many are missing.
     */
    private static void checkComputedDistances(SessionFactory sessionFactory) {

        ConsistencyCheck.CheckComputedDistances(sessionFactory);
    }

    private static void checkGesamtLibPresence() {
        GesamtLibIntegrationCheck.checkLibraryExists();
    }

    private static void checkGesamtBinaryFilesAreInDB() {
        ConsistencyCheck.CheckGesamtBinaryFilesAreInDB(new File(AppConfig.DATASET_BINARY_DIR));
    }

    private static void removeProteinChainsWithoutFile(boolean dryRun) {
        ConsistencyCheck.RemoveProteinChainsWithoutFile(dryRun);
    }

    private static void computeDistances(SessionFactory sf) {
        DistanceComputation.computeDistances(sf);
    }

    private static void learnSketches(SessionFactory sf) {
        try {
            LearnSketches.run(sf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void applySketches(SessionFactory sessionFactory) {
        try {
            ApplySketches.run(sessionFactory, AppConfig.SKETCH_LEARNING_SKETCH_LENGTH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void secondaryFiltering(SessionFactory sessionFactory) {
        LearnSecondaryFilteringWithGHPSketches.start(sessionFactory, 1024);

    }

    private static void generatePivotPairs(SessionFactory sessionFactory) {
        GeneratePivotCsvs.run(sessionFactory, "pivotPairsFor64pSketches.csv", "pivotPairsFor512pSketches");

    }

    private static void groundTruth() {
        GroundTruth.run();
    }

    private static void updateDataset() {
        UpdateDataset.run(
                AppConfig.DATASET_REMOTE_URL,
                AppConfig.DATASET_MIRROR_DIR,
                AppConfig.DATASET_RAW_DIR,
                AppConfig.DATASET_BINARY_DIR,
                AppConfig.DATASET_UPDATE_SCRIPT_PATH,
                AppConfig.SUBCONFIGS_PYTHON_INI_CONFIG_PATH);
    }

    private static void generateSubConfigs() {
        GenerateSubConfigs.run(AppConfig.SUBCONFIGS_PYTHON_INI_CONFIG_PATH);
    }

    private static void runMessiff() {
        RunMessiff.run();
    }

    private static void loadLibrary(String libraryName) {
        String resourcePath = "/lib/" + System.mapLibraryName(libraryName);

        // Extract the resource to a temp file
        try (InputStream in = CliApp.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new UnsatisfiedLinkError("The library " + libraryName + " was not found inside the JAR.");
            }

            File tempLib = File.createTempFile(libraryName, ".so");
            tempLib.deleteOnExit(); // Ensures the file is deleted when the JVM exits

            // Use try-with-resources to ensure all resources are closed
            try (OutputStream out = new FileOutputStream(tempLib)) {
                byte[] buffer = new byte[1024];
                int readBytes;
                while ((readBytes = in.read(buffer)) != -1) {
                    out.write(buffer, 0, readBytes);
                }
                out.flush(); // Ensure all changes are written before loading
            }

            // Load the library from the temp file
            System.load(tempLib.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the library", e);
        }
    }

    //todo normal runner
    public static void main(String[] args) {
        try {
            JavaUtils.validateNonNullProperties(AppConfig.class);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

//        loadLibrary("ProteinDistance");
        Args arguments = new Args();
        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);

        if (arguments.version) {
            System.out.println(3.010);
            return;
        }

        AppConfig.DRY_RUN = arguments.dryRun;

        migrate();
        sessionFactory = buildSessionFactory();

        switch (arguments.runFunction) {
            case "checkGesamtLibPresence" -> checkGesamtLibPresence();
            case "checkComputedDistances" -> checkComputedDistances(sessionFactory);
            case "checkGesamtBinaryFilesAreInDB" -> checkGesamtBinaryFilesAreInDB();
            case "removeProteinChainsWithoutFile" -> removeProteinChainsWithoutFile(arguments.dryRun);
            case "computeDistances" -> computeDistances(sessionFactory);
            case "learnSketches" -> learnSketches(sessionFactory);
            case "applySketches" -> applySketches(sessionFactory);
            case "secondaryFiltering" -> secondaryFiltering(sessionFactory);
            case "generatePivotPairs" -> generatePivotPairs(sessionFactory);
            case "groundTruth" -> groundTruth();
            case "updateDataset" -> updateDataset();
            case "generateSubConfigs" -> generateSubConfigs();
            case "runMessiff" -> runMessiff();
            default -> System.out.println("Invalid function name passed. Please check the function name and try again.");
        }
        sessionFactory.close();
    }
}
