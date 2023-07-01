package com.example;

import com.beust.jcommander.JCommander;
import com.example.services.configuration.AppConfig;
import com.example.services.configuration.Args;
import com.example.services.entrypoints.applySketches.ApplySketches;
import com.example.services.entrypoints.consistencyCheck.ConsistencyCheck;
import com.example.services.entrypoints.distanceComputation.DistanceComputation;
import com.example.services.entrypoints.learnSketches.LearnSketches;
import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App
{

    public static void migrate() {
        //todo make this work
        System.out.println( "Hello World!" );
        Flyway flyway = Flyway.configure()
                .dataSource("jdbc:mariadb://localhost:3306/protein_chain_db", "chain", "OneCha1n2RuleThem4ll")
                .locations("classpath:/db/migration")
                .load();
        flyway.migrate();

    }

    // todo whole session handling is wrong
    private static SessionFactory sessionFactory = buildSessionFactory();

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
            e.printStackTrace();
            throw new RuntimeException("Failed to build the SessionFactory.", e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private static void quickChecks() {
        ConsistencyCheck.CheckComputedDistances();
    }

    private static void slowChecks(boolean dryRun) {
        ConsistencyCheck.RemoveProteinChainsWithoutFile(true);
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
        ApplySketches.run(AppConfig.SKETCH_LEARNING_SKETCH_LENGTH);
    }

    //todo normal runner
    public static void main(String[] args) {
        Args arguments = new Args();
        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);

        AppConfig.DRY_RUN = arguments.dryRun;

        //todo add dry run support
        switch (arguments.runFunction) {
            case "quickChecks" -> quickChecks();
            case "slowChecks" -> slowChecks(arguments.dryRun);
            case "computeDistances" -> computeDistances();
            case "learnSketches" -> learnSketches();
            case "applySketches" -> applySketches();
            default ->
                    System.out.println("Invalid function name passed. Please check the function name and try again.");
        }
    }
}
