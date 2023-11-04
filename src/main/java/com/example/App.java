package com.example;

import com.beust.jcommander.JCommander;
import com.example.services.configuration.AppConfig;
import com.example.services.configuration.Args;
import com.example.services.entrypoints.applySketches.ApplySketches;
import com.example.services.entrypoints.generatePivotCsvs.GeneratePivotCsvs;
import com.example.services.entrypoints.ground_truth.GroundTruth;
import com.example.services.entrypoints.selfchecks.ConsistencyCheck;
import com.example.services.entrypoints.distanceComputation.DistanceComputation;
import com.example.services.entrypoints.learnSketches.LearnSketches;
import com.example.services.entrypoints.secondaryFiltering.LearnSecondaryFilteringWithGHPSketches;
import com.example.services.entrypoints.selfchecks.GesamtLibIntegrationCheck;
import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.*;

/**
 * Hello world!
 *
 */
public class App
{

    private static SessionFactory sessionFactory = null;

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();

            // Set the connection details from the configuration class
            configuration.setProperty("hibernate.connection.url", AppConfig.HIBERNATE_CONNECTION_URL);
            configuration.setProperty("hibernate.connection.username", AppConfig.HIBERNATE_CONNECTION_USERNAME);
            configuration.setProperty("hibernate.connection.password", AppConfig.HIBERNATE_CONNECTION_PASSWORD);
            configuration.configure("hibernate.cfg.xml");

            return configuration.buildSessionFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build the SessionFactory.", e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(AppConfig.FLYWAY_CONNECTION_URL,
                        AppConfig.FLYWAY_CONNECTION_USERNAME,
                        AppConfig.FLYWAY_CONNECTION_PASSWORD)
                .defaultSchema(AppConfig.FLYWAY_CONNECTION_SCHEMA)
                .locations("classpath:/db/migration")
                .load();
        flyway.migrate();
    }

    /**
     * Checks whether all distances were computed and if not how many are missing.
     */
    private static void checkComputedDistances() {

        ConsistencyCheck.CheckComputedDistances();
    }

    private static void checkGesamtLibPresence() {
        GesamtLibIntegrationCheck.checkLibraryExists();
    }

    private static void checkGesamtBinaryFilesAreInDB() {
        ConsistencyCheck.CheckGesamtBinaryFilesAreInDB(new File(AppConfig.PDBE_BINARY_FILES_DIR));
    }

    private static void removeProteinChainsWithoutFile(boolean dryRun) {
        ConsistencyCheck.RemoveProteinChainsWithoutFile(dryRun);
    }

    private static void computeDistances() {
        DistanceComputation.computeDistances();
    }

    private static void learnSketches() {
        try {
            LearnSketches.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void applySketches() {
        try {
            ApplySketches.run(AppConfig.SKETCH_LEARNING_SKETCH_LENGTH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void secondaryFiltering() {
        LearnSecondaryFilteringWithGHPSketches.start();

    }

    private static void generatePivotPairs() {
        try {
            GeneratePivotCsvs.run("pivotPairsFor64pSketches.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void groundTruth() {
        GroundTruth.run();
    }

    private static void loadLibrary(String libraryName) {
        String resourcePath = "/lib/" + System.mapLibraryName(libraryName);

        // Extract the resource to a temp file
        try (InputStream in = App.class.getResourceAsStream(resourcePath)) {
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
//        loadLibrary("ProteinDistance");
        Args arguments = new Args();
        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);

        AppConfig.DRY_RUN = arguments.dryRun;

        migrate();
        sessionFactory = buildSessionFactory();

        switch (arguments.runFunction) {
            case "checkGesamtLibPresence" -> checkGesamtLibPresence();
            case "checkComputedDistances" -> checkComputedDistances();
            case "checkGesamtBinaryFilesAreInDB" -> checkGesamtBinaryFilesAreInDB();
            case "removeProteinChainsWithoutFile" -> removeProteinChainsWithoutFile(arguments.dryRun);
            case "computeDistances" -> computeDistances();
            case "learnSketches" -> learnSketches();
            case "applySketches" -> applySketches();
            case "secondaryFiltering" -> secondaryFiltering();
            case "generatePivotPairs" -> generatePivotPairs();
            case "groundTruth" -> groundTruth();
            default ->
                    System.out.println("Invalid function name passed. Please check the function name and try again.");
        }
    }
}
