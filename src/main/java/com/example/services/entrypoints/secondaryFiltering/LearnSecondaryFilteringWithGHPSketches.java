package com.example.services.entrypoints.secondaryFiltering;

import com.example.dao.PivotDao;
import com.example.dao.PivotSetDao;
import com.example.dao.ProteinChainForLearningSketchesDao;
import com.example.dao.ProteinChainMetadataDao;
import com.example.service.PivotService;
import com.example.service.PivotSetService;
import com.example.service.ProteinChainMetadataService;
import com.example.service.distance.ProteinChainService;
import com.example.services.DatasetAbstractionLayer.Proteins.ProteinAbstractMetricSpaceDBImpl;
import com.example.services.DatasetAbstractionLayer.Sketches.SketchAbstractMetricSpaceDBImpl;
import com.example.services.configuration.AppConfig;
import com.example.services.storage.DatasetImpl;
import com.example.services.storage.MetricSpacesStorageInterfaceDBImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.bounding.nopivot.learning.LearningSecondaryFilteringWithSketches;
import vm.metricSpace.distance.bounding.nopivot.storeLearned.SecondaryFilteringWithSketchesStoreInterface;
import vm.metricSpace.distance.impl.HammingDistanceLongs;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static com.example.App.getSessionFactory;

public class LearnSecondaryFilteringWithGHPSketches {

    public static final Logger LOG = Logger.getLogger(LearnSecondaryFilteringWithGHPSketches.class.getName());


    private static DatasetImpl<String> createProteinDataset(Session session) {
        var pivotSetService = new PivotSetService(new PivotSetDao(session));
        var pivotService = new PivotService(new PivotDao(session), pivotSetService);
        var sampleSize = AppConfig.SKETCH_LEARNING_SAMPLE_SIZE; //todo better differentiate from LearnSketches
        var numberOfPivots = AppConfig.SKETCH_LEARNING_PIVOTS_COUNT;
        var metricSpace = new ProteinAbstractMetricSpaceDBImpl(null);
        //we don't want the code to interact directly with db
        var proteinChainService = new ProteinChainService(pivotSetService, new ProteinChainForLearningSketchesDao(session));
        var proteinChainMetadaService = new ProteinChainMetadataService(new ProteinChainMetadataDao(session), pivotSetService);
        var metricSpaceStorage = new MetricSpacesStorageInterfaceDBImpl(pivotService, proteinChainService, proteinChainMetadaService);

        var dataset = new DatasetImpl<String>("proteinChain", metricSpace, metricSpaceStorage);
        return dataset;
    }

    private static DatasetImpl<long[]> createSketchDataset(Session session) {
        var pivotSetService = new PivotSetService(new PivotSetDao(session));
        var pivotService = new PivotService(new PivotDao(session), pivotSetService);
        var sampleSize = AppConfig.SKETCH_LEARNING_SAMPLE_SIZE; //todo better differentiate from LearnSketches
        var numberOfPivots = AppConfig.SKETCH_LEARNING_PIVOTS_COUNT;

        var metricSpace = new SketchAbstractMetricSpaceDBImpl(new HammingDistanceLongs());
        //we don't want the code to interact directly with db
        var proteinChainService = new ProteinChainService(pivotSetService, new ProteinChainForLearningSketchesDao(session));
        var proteinChainMetadaService = new ProteinChainMetadataService(new ProteinChainMetadataDao(session), pivotSetService);
        var metricSpaceStorage = new MetricSpacesStorageInterfaceDBImpl(pivotService, proteinChainService, proteinChainMetadaService);

        var dataset = new DatasetImpl<long[]>("proteinChain", metricSpace, metricSpaceStorage);
        return dataset;
    }

    public static void start() {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {


            int sketchLength = 1024;
            Dataset fullDataset = createProteinDataset(session);
            Dataset sketchesDataset = createSketchDataset(session);
            float distIntervalForPX = 0.01f;
            float maxDistOnFullDataset = 2;
            run(fullDataset, sketchesDataset, distIntervalForPX, sketchLength, maxDistOnFullDataset);

        }
    }

    private static final void run(Dataset fullDataset, Dataset sketchesDataset, float distIntervalForPX, int sketchLength, float maxDistOnFullDataset) {
        SecondaryFilteringWithSketchesStoreInterface storage = new FSSecondaryFilteringWithSketchesStorage();
        //iDim file
        File fileOutputForIDim = new File(AppConfig.SECONDARY_FILTERING_SKETCHES_DIR,
                "iDim_" + sketchesDataset.getDatasetName() + "_"
                        + LearningSecondaryFilteringWithSketches.SKETCHES_SAMPLE_COUNT_FOR_IDIM_PX + "sk_"
                        + LearningSecondaryFilteringWithSketches.DISTS_COMPS_FOR_SK_IDIM_AND_PX + "distsForIDIM_PX"
                        + ".csv");
        if (fileOutputForIDim.exists()) {
            fileOutputForIDim.delete();
        }
        //px file
        File fileOutputForPX = new File(AppConfig.SECONDARY_FILTERING_SKETCHES_DIR, "px_" + sketchesDataset.getDatasetName() + "_"
                + LearningSecondaryFilteringWithSketches.SKETCHES_SAMPLE_COUNT_FOR_IDIM_PX + "sk_"
                + LearningSecondaryFilteringWithSketches.DISTS_COMPS_FOR_SK_IDIM_AND_PX + "distsForIDIM_PX_"
                + distIntervalForPX + "px_interval"
                + ".csv");
        if (fileOutputForPX.exists()) {
            fileOutputForPX.delete();
        }
        //mapping file preffix
        LearningSecondaryFilteringWithSketches learning = new LearningSecondaryFilteringWithSketches(
                storage,
                fullDataset,
                sketchesDataset,
                fileOutputForIDim,
                distIntervalForPX,
                maxDistOnFullDataset,
                sketchLength,
                fileOutputForPX
        );
        learning.execute();
    }
}
