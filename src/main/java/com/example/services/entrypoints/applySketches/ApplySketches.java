package com.example.services.entrypoints.applySketches;

import com.example.dao.*;
import com.example.service.PivotPairsFor64pSketchesService;
import com.example.service.PivotService;
import com.example.service.PivotSetService;
import com.example.service.distance.ProteinChainService;
import com.example.services.distance.AbstractMetricSpaceDBImpl;
import com.example.services.entrypoints.DatasetImpl;
import com.example.services.storage.GHPSketchesPivotPairsStorageDBImpl;
import com.example.services.storage.MetricSpacesStorageInterfaceDBImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import vm.metricSpace.Dataset;
import vm.metricSpace.MetricSpacesStorageInterface;
import vm.metricSpace.dataToStringConvertors.SingularisedConvertors;
import vm.objTransforms.perform.TransformDataToGHPSketches;
import vm.objTransforms.storeLearned.GHPSketchingPivotPairsStoreInterface;

import java.io.IOException;

import static com.example.App.getSessionFactory;

public class ApplySketches {

    public static void run(int sketchesLength) {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {
            var pivotSetService = new PivotSetService(new PivotSetDao(session));
            var pivotService = new PivotService(new PivotDao(session), pivotSetService);
            var pivotPairsFor64pSketchesService = new PivotPairsFor64pSketchesService(pivotSetService, new PivotPairsFor64pSketchesDao(session));
            GHPSketchingPivotPairsStoreInterface sketchingTechStorage = new GHPSketchesPivotPairsStorageDBImpl(session, pivotPairsFor64pSketchesService, pivotSetService);
            int[] sketchesLengths = new int[]{sketchesLength};

            var metricSpace = new AbstractMetricSpaceDBImpl();
            //for learning sketches will return proteins with distance
            //todo corrent the "for" naming later
            var proteinChainDao = new ProteinChainForLearningSketchesDao(session);
            var proteinChainService = new ProteinChainService(pivotSetService, proteinChainDao);
            var metricSpaceStorage = new MetricSpacesStorageInterfaceDBImpl(null, pivotService, proteinChainService);
            var dataset = new DatasetImpl<String>("proteinChain", metricSpace, metricSpaceStorage);

            TransformDataToGHPSketches evaluator = new TransformDataToGHPSketches(dataset, sketchingTechStorage, metricSpaceStorage);
            evaluator.createSketchesForDatasetPivotsAndQueries(sketchesLengths);
        }
    }
}
