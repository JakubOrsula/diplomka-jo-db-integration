package com.example.services.entrypoints.learnSketches;

import com.example.dao.PivotDao;
import com.example.dao.PivotPairsFor64pSketchesDao;
import com.example.dao.PivotSetDao;
import com.example.dao.ProteinChainMetadataDao;
import com.example.model.SimpleProtein;
import com.example.model.json.Converter;
import com.example.service.PivotPairsFor64pSketchesService;
import com.example.service.PivotService;
import com.example.service.PivotSetService;
import com.example.service.ProteinChainMetadaService;
import com.example.service.distance.ProteinChainService;
import com.example.services.configuration.AppConfig;
import com.example.services.distance.AbstractMetricSpaceDBImpl;
import com.example.services.entrypoints.DatasetImpl;
import com.example.services.storage.GHPSketchesPivotPairsStorageDBImpl;
import com.example.services.storage.MetricSpacesStorageInterfaceDBImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import vm.metricSpace.Dataset;
import vm.objTransforms.learning.LearnSketchingGHP;
import vm.objTransforms.storeLearned.GHPSketchingPivotPairsStoreInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.App.getSessionFactory;

public class LearnSketches {

    private static void run_learn_sketches(Session session, PivotService pivotService, Dataset dataset, GHPSketchingPivotPairsStoreInterface sketchingTechStorage, int[] sketchesLengths, MetricSpacesStorageInterfaceDBImpl metricSpacesStorage) throws IOException {
        int sampleSize = AppConfig.SKETCH_LEARNING_SAMPLE_SIZE;
        var numberOfPivots = 64; //todo should we try other values?
        // todo why 15000 - number determined by vlada mic to work the best
        LearnSketchingGHP learn = new LearnSketchingGHP(dataset.getMetricSpace(), dataset.getMetricSpacesStorage(), sketchingTechStorage, numberOfPivots, 15000);
        String datasetName = dataset.getDatasetName();
        float[][] dists = new float[sampleSize][numberOfPivots];
        Map<String, Integer> columnHeaders = new HashMap<>();
        Map<String, Integer> rowHeaders = new HashMap<>();
        List<SimpleProtein> dataSetSample = new ArrayList<>();

        var pivots = pivotService.GetPivotsAsSimpleProteins(numberOfPivots);
        for (int i = 0; i < pivots.size(); i++) {
            columnHeaders.put(String.valueOf(pivots.get(i).getIntId()), i);
        }

        var proteinChainMetadataService = new ProteinChainMetadaService(new ProteinChainMetadataDao(session), new PivotSetService(new PivotSetDao(session)));
        var pcms = proteinChainMetadataService.getProteinChainMetadatas();
        AtomicInteger counter = new AtomicInteger(0);
        //fill out the matrix
        while (pcms.hasNext() && counter.get() < sampleSize) {
            var pcm = pcms.next();
            //int id into row
            rowHeaders.put(pcm.getId().getProteinChain().getIntId().toString(), counter.get());
            var dist_dict = Converter.fromJsonString(pcm.getPivotDistances()).getDists();
            dataSetSample.add(new SimpleProtein(pcm.getId().getProteinChain().getIntId(), pcm.getId().getProteinChain().getGesamtId()));

            //dist into matrix, ordered by columnHeaders
            columnHeaders.forEach((key, value) -> {
                Double dist = dist_dict.get(key);
                float distFloat = dist.floatValue();
                dists[counter.get()][value] = distFloat;
            });
            counter.incrementAndGet();
            if (counter.getOpaque() % 1000 == 0)
                System.out.println("Prepared " + counter + " matrix rows");
        }
        // Sketches for Unbalanced bits for similarity search
        learn.evaluate(datasetName, datasetName, sampleSize, sketchesLengths, AppConfig.SKETCH_LEARNING_BALANCE, dists, columnHeaders, rowHeaders, dataSetSample);
    }

    public static void run(int sketchesLength) {
        try (SessionFactory sessionFactory = getSessionFactory();
             Session session = sessionFactory.openSession()) {
            var pivotSetService = new PivotSetService(new PivotSetDao(session));
            var pivotService = new PivotService(new PivotDao(session), pivotSetService);
            var pivotPairsFor64pSketchesService = new PivotPairsFor64pSketchesService(pivotSetService, new PivotPairsFor64pSketchesDao(session));
            GHPSketchingPivotPairsStoreInterface sketchingTechStorage = new GHPSketchesPivotPairsStorageDBImpl(session, pivotPairsFor64pSketchesService, pivotSetService);
            int[] sketchesLengths = new int[]{sketchesLength};

            var metricSpace = new AbstractMetricSpaceDBImpl();
            //we don't want the code to interact directly with db
            var proteinChainService = new ProteinChainService(pivotSetService, null);
            var metricSpaceStorage = new MetricSpacesStorageInterfaceDBImpl(null, pivotService, null);
            var dataset = new DatasetImpl<String>("proteinChain", metricSpace, metricSpaceStorage);

            try {
                run_learn_sketches(session, pivotService, dataset, sketchingTechStorage, sketchesLengths, metricSpaceStorage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
