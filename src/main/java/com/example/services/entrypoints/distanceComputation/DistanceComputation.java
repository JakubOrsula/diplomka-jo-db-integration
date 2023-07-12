package com.example.services.entrypoints.distanceComputation;

import com.example.dao.PivotDao;
import com.example.dao.PivotSetDao;
import com.example.dao.ProteinChainForDistanceDao;
import com.example.dao.ProteinChainMetadataDao;
import com.example.service.PivotService;
import com.example.service.PivotSetService;
import com.example.service.ProteinChainMetadataService;
import com.example.service.distance.ProteinChainService;
import com.example.services.distance.AbstractMetricSpaceDBImpl;
import com.example.services.distance.DistanceFunctionInterfaceImpl;
import com.example.services.distance.update.EvalAndStoreObjectsToPivotsDists;
import com.example.services.entrypoints.DatasetImpl;
import com.example.services.storage.MetricSpacesStorageInterfaceDBImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import static com.example.App.getSessionFactory;

public class DistanceComputation {
    public static void computeDistances() {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {

            var metricSpace = new AbstractMetricSpaceDBImpl(new DistanceFunctionInterfaceImpl<String>());
            var pivotSetService = new PivotSetService(new PivotSetDao(session));
            var pivotService = new PivotService(new PivotDao(session), pivotSetService);
            var proteinChainService = new ProteinChainService(pivotSetService, new ProteinChainForDistanceDao(session));
            var proteinChainMetadaService = new ProteinChainMetadataService(new ProteinChainMetadataDao(session), pivotSetService);
            var metricSpaceStorage = new MetricSpacesStorageInterfaceDBImpl(pivotService, proteinChainService, proteinChainMetadaService);
            var dataset = new DatasetImpl<String>("proteinChain", metricSpace, metricSpaceStorage);
            var evaluator = new EvalAndStoreObjectsToPivotsDists(session, pivotSetService);
            evaluator.run(dataset);
        }
    }
}
