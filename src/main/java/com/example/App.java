package com.example;

import com.example.model.ProteinChain;
import com.example.services.distance.AbstractMetricSpaceDBImpl;
import com.example.services.distance.update.EvalAndStoreObjectsToPivotsDists;
import com.example.services.storage.MetricSpacesStorageInterfaceDBImpl;
import org.flywaydb.core.Flyway;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import com.example.model.Protein;
import com.example.services.configuration.AppConfig;

import java.util.Iterator;
import java.util.List;

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

    public static void main( String[] args )
    {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {

            var metricSpace = new AbstractMetricSpaceDBImpl();
            var metricSpaceStorage = new MetricSpacesStorageInterfaceDBImpl(session);
            var dataset = new DatasetImpl<String>("proteinChain", metricSpace, metricSpaceStorage);

            var evaluator = new EvalAndStoreObjectsToPivotsDists(session);
            evaluator.run(dataset);
        }
    }
}
