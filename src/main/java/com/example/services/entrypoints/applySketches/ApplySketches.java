package com.example.services.entrypoints.applySketches;

import com.example.dao.*;
import com.example.service.PivotPairsForXpSketchesService;
import com.example.service.PivotService;
import com.example.service.PivotSetService;
import com.example.service.distance.ProteinChainService;
import com.example.services.distance.AbstractMetricSpaceDBImpl;
import com.example.services.entrypoints.DatasetImpl;
import com.example.services.storage.GHPSketchesPivotPairsStorageDBImpl;
import com.example.services.storage.MetricSpacesStorageInterfaceDBImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import vm.objTransforms.perform.TransformDataToGHPSketches;
import vm.objTransforms.storeLearned.GHPSketchingPivotPairsStoreInterface;

import static com.example.App.getSessionFactory;

public class ApplySketches {

    public static void run(int sketchesLength) {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {
            var pivotSetService = new PivotSetService(new PivotSetDao(session));
            var pivotService = new PivotService(new PivotDao(session), pivotSetService);
            var pivotPairsFor64pSketchesService = new PivotPairsForXpSketchesService(pivotSetService, new PivotPairsForXpSketchesDao(session));
            GHPSketchingPivotPairsStoreInterface sketchingTechStorage = new GHPSketchesPivotPairsStorageDBImpl(session, pivotPairsFor64pSketchesService, pivotSetService);
            int[] sketchesLengths = new int[]{sketchesLength};

            //todo
            var metricSpace = new AbstractMetricSpaceDBImpl(null);
            //for learning sketches will return proteins with distance
            //todo corrent the "for" naming later
            var proteinChainDao = new ProteinChainForLearningSketchesDao(session);
            var proteinChainService = new ProteinChainService(pivotSetService, proteinChainDao);
            var metricSpaceStorage = new MetricSpacesStorageInterfaceDBImpl(pivotService, proteinChainService);
            var dataset = new DatasetImpl<String>("proteinChain", metricSpace, metricSpaceStorage);

            TransformDataToGHPSketches evaluator = new TransformDataToGHPSketches(dataset, sketchingTechStorage, metricSpaceStorage);
            evaluator.createSketchesForDatasetPivotsAndQueries(sketchesLengths);
        }
    }
}
