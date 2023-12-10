package com.example.services.entrypoints.learnSketches;

import com.example.dao.*;
import com.example.service.PivotPairsForXpSketchesService;
import com.example.service.PivotService;
import com.example.service.PivotSetService;
import com.example.service.ProteinChainMetadataService;
import com.example.service.distance.ProteinChainService;
import com.example.services.configuration.AppConfig;
import com.example.services.DatasetAbstractionLayer.Proteins.ProteinAbstractMetricSpaceDBImpl;
import com.example.services.distance.CachedDistanceFunctionInterfaceImpl;
import com.example.services.storage.DatasetImpl;
import com.example.services.storage.GHPSketchesPivotPairsStorageDBImpl;
import com.example.services.storage.MetricSpacesStorageInterfaceDBImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import vm.objTransforms.learning.LearnSketchingGHP;
import vm.objTransforms.storeLearned.GHPSketchingPivotPairsStoreInterface;

import java.io.IOException;

import static com.example.CliApp.getSessionFactory;

public class LearnSketches {

    public static void run(SessionFactory sessionFactory) throws IOException {
        try (Session session = sessionFactory.openSession()) {
            var pivotSetService = new PivotSetService(new PivotSetDao(session));
            var pivotService = new PivotService(new PivotDao(session), pivotSetService);
            var pivotPairsForXpSketchesService = new PivotPairsForXpSketchesService(pivotSetService, new PivotPairsForXpSketchesDao(session));
            if (pivotPairsForXpSketchesService.pairsCount() > 0) {
                System.out.println("Pivot pairs already stored. Skipping.");
                System.out.println("If you want to relearn the sketches, delete the pivot pairs from the database.");
                return;
            }
            GHPSketchingPivotPairsStoreInterface sketchingTechStorage = new GHPSketchesPivotPairsStorageDBImpl(session, pivotPairsForXpSketchesService, pivotSetService);
            int[] sketchesLengths = new int[]{AppConfig.SKETCH_LEARNING_SKETCH_LENGTH};
            var sampleSize = AppConfig.SKETCH_LEARNING_SAMPLE_SIZE;
            var numberOfPivots = AppConfig.SKETCH_LEARNING_PIVOTS_COUNT;

            var distanceFunction = new CachedDistanceFunctionInterfaceImpl<String>(session, pivotService, sampleSize, numberOfPivots);
            var metricSpace = new ProteinAbstractMetricSpaceDBImpl(distanceFunction);
            //we don't want the code to interact directly with db
            var proteinChainService = new ProteinChainService(pivotSetService, new ProteinChainForLearningSketchesDao(session));
            var proteinChainMetadaService = new ProteinChainMetadataService(new ProteinChainMetadataDao(session), pivotSetService);
            var metricSpaceStorage = new MetricSpacesStorageInterfaceDBImpl(pivotService, proteinChainService, proteinChainMetadaService);

            var dataset = new DatasetImpl<String>("proteinChain", metricSpace, metricSpaceStorage);


            // todo why 15000 - number determined by vlada mic to work the best
            LearnSketchingGHP learn = new LearnSketchingGHP(dataset, sketchingTechStorage, numberOfPivots, 15000);

            // Sketches for Unbalanced bits for similarity search
            learn.evaluate(dataset, sampleSize, sketchesLengths, AppConfig.SKETCH_LEARNING_BALANCE);

        }
    }
}
