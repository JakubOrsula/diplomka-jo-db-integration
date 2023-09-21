package com.example.services.entrypoints.selfchecks;

import com.example.dao.PivotSetDao;
import com.example.dao.ProteinChainForLearningSketchesDao;
import com.example.service.PivotSetService;
import com.example.service.distance.ProteinChainService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.File;

import static com.example.App.getSessionFactory;

public class ConsistencyCheck {

    /**
     * Checks whether binary chains are stored in the DB.
     * @param rootDir - root directory of binary files e.g /mnt/data/PDBe_binary
     */
    public static void CheckGesamtBinaryFilesAreInDB(File rootDir) {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {
            var fixer = new BinaryAndDBConsistencyFixer(session);
            fixer.CheckGesamtBinaryFilesAreInDB(rootDir);
        }
    }

    /**
     * It might happen that binary files from which the chains were parsed are removed.
     * In such case we have no other option than to remove the chains from DB is as well.
     * @param dryRun - if true no destructive operation will be performed
     */
    public static void RemoveProteinChainsWithoutFile(boolean dryRun) {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {
            var fixer = new BinaryAndDBConsistencyFixer(session);
            fixer.removeProteinChainsWithoutFile(dryRun);
        }
    }

    public static void CheckComputedDistances() {
        System.out.println("Getting total proteins and proteins with valid distances counts. This process will take a long time...");
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {
            var totalProteins = session.createQuery("select count(*) from ProteinChain where indexedAsDataObject=true ", Long.class).getSingleResult();

            var pService = new ProteinChainService(new PivotSetService(new PivotSetDao(session)), new ProteinChainForLearningSketchesDao(session));
            var proteinsWithValidDistances = pService.getChainsCount();
            System.out.println("Total valid proteins:         " + totalProteins);
            System.out.println("Proteins with valid distance: " + proteinsWithValidDistances);
            System.out.println("Missing:                      " + (totalProteins - proteinsWithValidDistances));
        }
    }

    public static void CheckLearnedSketches() {
        //todo check if we have 2*pivots rows in db
        //todo add migration which will ensure we have unique and complete bit order
    }
}
