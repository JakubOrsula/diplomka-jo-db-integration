package com.example.services.entrypoints.ground_truth;

import com.example.dao.PivotDao;
import com.example.dao.PivotSetDao;
import com.example.dao.ProteinChainForDistanceDao;
import com.example.dao.ProteinChainMetadataDao;
import com.example.service.PivotService;
import com.example.service.PivotSetService;
import com.example.service.ProteinChainMetadataService;
import com.example.service.distance.ProteinChainService;
import com.example.services.DatasetAbstractionLayer.Proteins.ProteinAbstractMetricSpaceDBImpl;
import com.example.services.distance.DistanceFunctionInterfaceImpl;
import com.example.services.storage.DatasetImpl;
import com.example.services.storage.MetricSpacesStorageInterfaceDBImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import vm.evaluatorsToBeUsed.GroundTruthEvaluator;
import vm.fs.store.queryResults.FSNearestNeighboursStorageImpl;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.AbstractMetricSpacesStorage;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;
import vm.queryResults.QueryNearestNeighboursStoreInterface;

import java.util.List;

import static com.example.App.getSessionFactory;

public class GroundTruth {
    public static void run() {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {


            ProteinAbstractMetricSpaceDBImpl metricSpace = new ProteinAbstractMetricSpaceDBImpl(new DistanceFunctionInterfaceImpl<String>());
            var pivotSetService = new PivotSetService(new PivotSetDao(session));
            PivotService pivotService = new PivotService(new PivotDao(session), pivotSetService);
            ProteinChainService proteinChainService = new ProteinChainService(pivotSetService, new ProteinChainForDistanceDao(session));
            ProteinChainMetadataService proteinChainMetadaService = new ProteinChainMetadataService(new ProteinChainMetadataDao(session), pivotSetService);
            MetricSpacesStorageInterfaceDBImpl metricSpaceStorage = new MetricSpacesStorageInterfaceDBImpl(pivotService, proteinChainService, proteinChainMetadaService);

            Dataset[] datasets = new Dataset[]{
                    new DatasetImpl<>("proteinChain", metricSpace, metricSpaceStorage)
            };
            for (
                    Dataset dataset : datasets) {
                String datasetName = dataset.getDatasetName();
                String querySetName = dataset.getQuerySetName();
                int k = 1000; // 1000 for orig datasets, else 20000
                AbstractMetricSpace space = dataset.getMetricSpace();

                DistanceFunctionInterface distanceFunction = space.getDistanceFunctionForDataset(datasetName);
                AbstractMetricSpacesStorage spaceStorage = dataset.getMetricSpacesStorage();
                QueryNearestNeighboursStoreInterface groundTruthStorage = new FSNearestNeighboursStorageImpl();

                List<Object> metricQueryObjects = spaceStorage.getQueryObjects(querySetName);
                GroundTruthEvaluator gte = new GroundTruthEvaluator(space, distanceFunction, metricQueryObjects, k, groundTruthStorage);
                gte.evaluateIteratorInParallel(spaceStorage.getObjectsFromDataset(datasetName), datasetName, querySetName);
//            gte.evaluateIteratorSequentially(spaceStorage.getObjectsFromDataset(datasetName), datasetName, querySetName);
                System.gc();
            }
        }
    }
}
