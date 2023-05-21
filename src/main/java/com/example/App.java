package com.example;

import com.example.model.ProteinChain;
import com.example.model.json.Converter;
import com.example.services.distance.AbstractMetricSpaceDBImpl;
import com.example.services.distance.update.EvalAndStoreObjectsToPivotsDists;
import com.example.services.storage.GHPSketchesPivotPairsStorageDBImpl;
import com.example.services.storage.MetricSpacesStorageInterfaceDBImpl;
import com.example.services.storage.BinaryAndDBConsistencyFixer;
import org.flywaydb.core.Flyway;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import com.example.model.Protein;
import com.example.services.configuration.AppConfig;
import vm.metricSpace.Dataset;
import vm.objTransforms.learning.LearnSketchingGHP;
import vm.objTransforms.storeLearned.GHPSketchingPivotPairsStoreInterface;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 *
 */
public class App
{

    public static void print_proteins() {
        SessionFactory sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Protein.class)
                .buildSessionFactory();

        // create a new session
        Session session = sessionFactory.getCurrentSession();

        try {
            // start a transaction
            session.beginTransaction();

            // query all rows from the Protein table
            List<ProteinChain> proteins = session.createQuery("from ProteinChain").getResultList();

            // print each row
            for (var protein : proteins) {
//                System.out.println(protein);
            }
            System.out.printf("%s proteins", proteins.size());

            // commit the transaction
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // close the session and session factory
            session.close();
            sessionFactory.close();
        }
    }

    public static void migrate() {
        //todo make this work
        System.out.println( "Hello World!" );
        Flyway flyway = Flyway.configure()
                .dataSource("jdbc:mariadb://localhost:3306/protein_chain_db", "chain", "OneCha1n2RuleThem4ll")
                .locations("classpath:/db/migration")
                .load();
        flyway.migrate();

    }

    public static void print_chain_iter() {
        try (SessionFactory sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();
             Session session = sessionFactory.openSession()) {
            MetricSpacesStorageInterfaceDBImpl storage = new MetricSpacesStorageInterfaceDBImpl(session);
            Iterator<Object> objIterator  = storage.getObjectsFromDataset("???");
            Iterator<ProteinChain> proteinIterator = new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return objIterator.hasNext();
                }

                @Override
                public ProteinChain next() {
                    return (ProteinChain) objIterator.next();
                }
            };
            while (proteinIterator.hasNext()) {
                var protein = proteinIterator.next();
                System.out.println(protein.getGesamtId());
            }
        }
    }

    private static final SessionFactory sessionFactory = buildSessionFactory();

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

    private static void learn_sketches() {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {
            GHPSketchingPivotPairsStoreInterface sketchingTechStorage = new GHPSketchesPivotPairsStorageDBImpl(session);
            int[] sketchesLengths = new int[]{192}; //delka sketchu v bitech, todo separate run for 1024

            var metricSpace = new AbstractMetricSpaceDBImpl();
            var metricSpaceStorage = new MetricSpacesStorageInterfaceDBImpl(session);
            var dataset = new DatasetImpl<String>("proteinChain", metricSpace, metricSpaceStorage);

            try {
                run_create_sketches(dataset, sketchingTechStorage, sketchesLengths, metricSpaceStorage);
            } catch (IOException e) { //todo to inner
                throw new RuntimeException(e);
            }
        }
    }

    //todo merge with merhod above or split to a different class
    //todo passing of MetricSpacesStorageInterfaceDBImpl even though is available on dataset
    private static void run_create_sketches(Dataset dataset, GHPSketchingPivotPairsStoreInterface sketchingTechStorage, int[] sketchesLengths, MetricSpacesStorageInterfaceDBImpl metricSpacesStorage) throws IOException {
        int sampleSize = 10000; // 100000 todo why?
        var numberOfPivots = 64;
        // todo why 15000
        LearnSketchingGHP learn = new LearnSketchingGHP(dataset.getMetricSpace(), dataset.getMetricSpacesStorage(), sketchingTechStorage, numberOfPivots, 15000);
        String datasetName = dataset.getDatasetName();
        // voluntary step and voluntary arguments start
        float[][] dists = new float[sampleSize][numberOfPivots];
        Map<String, Integer> columnHeaders = new HashMap<>();
        Map<String, Integer> rowHeaders = new HashMap<>();

        var metricSpace = dataset.getMetricSpace();
        var pivots = metricSpacesStorage.getPivots("", numberOfPivots);
        for (int i = 0; i < pivots.size(); i++) {
            columnHeaders.put(metricSpace.getIDOfMetricObject(pivots.get(i)).toString(), i);
        }

        // todo adjust query to find missing sketches for distances and not distances
        var pcms = metricSpacesStorage.getProteinChainMetadatas("");
        AtomicInteger counter = new AtomicInteger(0);
        while (pcms.hasNext() && counter.get() < sampleSize) { //todo limiting here is just wrong. You are limiting different tables. call to get sample size will return different proteins
            var pcm = pcms.next();
            rowHeaders.put(pcm.getId().getProteinChain().getIntId().toString(), counter.get());
            var dist_dict = Converter.fromJsonString(pcm.getPivotDistances()).getDists(); //todo try catch

            columnHeaders.forEach((key, value) -> {
                Double dist = dist_dict.get(key);
                float distFloat = dist.floatValue();
                dists[counter.get()][value] = distFloat;
            });
            counter.incrementAndGet();
            System.out.println("Prepared " + counter + " matrix rows");
        }
        // voluntary step and voluntary arguments stop
        // todo balance to config and explainer, balanced bits across matrix one column has same number 0 and 1 across all rows
        // Sketches for Unbalanced bits for similarity search
        learn.evaluate(datasetName, datasetName, sampleSize, sketchesLengths, 0.5f, dists, columnHeaders, rowHeaders);
    }

    private static void computeDistances() {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {

            var metricSpace = new AbstractMetricSpaceDBImpl();
            var metricSpaceStorage = new MetricSpacesStorageInterfaceDBImpl(session);
            var dataset = new DatasetImpl<String>("proteinChain", metricSpace, metricSpaceStorage);

            var evaluator = new EvalAndStoreObjectsToPivotsDists(session);
            evaluator.run(dataset);
        }
    }

    private static void computeDiff() {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {
            var fixer = new BinaryAndDBConsistencyFixer(session);
            fixer.removeProteinChainsWithoutFile(true);
            fixer.checkGesamtIdInDatabase(new File(AppConfig.PDBE_BINARY_FILES_DIR));
        }
    }

    public static void main( String[] args )
    {
//        learn_sketches();
        computeDiff();
    }
}
