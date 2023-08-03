package com.example.services.distance.update;

import com.example.dao.PivotSetDao;
import com.example.dao.ProteinChainForDistanceDao;
import com.example.model.PivotSet;
import com.example.model.ProteinChain;
import com.example.model.ProteinChainMetadata;
import com.example.model.json.Converter;
import com.example.model.json.DistsMetadata;
import com.example.service.PivotSetService;
import com.example.service.distance.ProteinChainService;
import com.example.services.configuration.AppConfig;
import com.example.services.utils.MapUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hibernate.Session;
import vm.metricSpace.Dataset;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EvalAndStoreObjectsToPivotsDists {

    private final Session session;
    private final PivotSetService pivotSetService;

    public EvalAndStoreObjectsToPivotsDists(Session session, PivotSetService pivotSetService) {
        this.session = session;
        this.pivotSetService = pivotSetService;
    }

    public static final Logger LOGGER = Logger.getLogger(EvalAndStoreObjectsToPivotsDists.class.getName());

    private final int maxQSize = AppConfig.CONSUMER_BUFFER_SIZE * 10;
    private final int maxDBBufferSize = AppConfig.CONSUMER_BUFFER_SIZE;
    private final LinkedBlockingQueue<EvaluationResult> queue = new LinkedBlockingQueue<>(maxQSize);


    private void compareWithResultInDb(EvaluationResult res, PivotSet currentPivotSet) {
        ProteinChain proteinChain = session.get(ProteinChain.class, res.proteinChainIntId);
        var pmId = new ProteinChainMetadata.ProteinChainMetadataId();
        pmId.setProteinChain(proteinChain);
        pmId.setPivotSet(currentPivotSet);
        ProteinChainMetadata pmc = this.session.get(ProteinChainMetadata.class, pmId);
        if (pmc == null) {
            System.out.println("could not retrieve pmc for " + proteinChain.getIntId() + ", " + currentPivotSet.getId());
            return;
        }
        try {
            var new_json = Converter.fromJsonString(res.metadata).getDists();
            var saved_json = Converter.fromJsonString(pmc.getPivotDistances()).getDists();
            if (!MapUtils.compareMaps(new_json, saved_json)) {
                System.out.println("Maps are nor the same for protein: " + res.proteinChainIntId + ", " + proteinChain.getGesamtId() + ", picotSet: " + currentPivotSet.getId());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //todo to db layer
    private void saveResultToDb(Session session, EvaluationResult res, PivotSet currentPivotSet) {
        ProteinChain proteinChain = this.session.get(ProteinChain.class, res.proteinChainIntId);
        var pmId = new ProteinChainMetadata.ProteinChainMetadataId();
        pmId.setProteinChain(proteinChain);
        pmId.setPivotSet(currentPivotSet);
        var pMetadata = new ProteinChainMetadata();
        pMetadata.setPivotDistances(res.metadata);
        pMetadata.setId(pmId);
        pMetadata.setSketch64p("");
        pMetadata.setSketch512p("");
        pMetadata.setLastUpdate(new Timestamp(System.currentTimeMillis()));
        session.merge(pMetadata);
    }

    private void consumeResults() {
        System.out.println("Initializing consumer");
        var proteinChainService = new ProteinChainService(new PivotSetService(new PivotSetDao(session)), new ProteinChainForDistanceDao(session));
        var currentPivotSet = pivotSetService.GetCurrentPivotSet();
        var elgibleChainCount = proteinChainService.getChainsCount();
        var soFarProcessedChains = 0L;
        var averageRunningTimePerChain = 0.0;
        var averageRunningTimePerChunk = 0.0;
        var soFarProcessedChunksCount = 1L;
        boolean last_element_encountered = false;

        while (!last_element_encountered) {
            var qContents = new ArrayList<EvaluationResult>();

            long fillStartTime = System.currentTimeMillis();
            while (qContents.size() < maxDBBufferSize) {
                try {
                    var res = queue.take();
                    if (res.endMarker) {
                        last_element_encountered = true;
                        break;
                    }
                    qContents.add(res);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            long fillEndTime = System.currentTimeMillis();
            long fillTime = fillEndTime - fillStartTime;
            System.out.println("------");
            System.out.printf("Time taken to fill the ArrayList(%d): " + (fillTime / 1000.0) + " seconds%n", maxDBBufferSize);
            averageRunningTimePerChunk = (averageRunningTimePerChunk * soFarProcessedChunksCount + fillTime) / (soFarProcessedChunksCount + 1);
            soFarProcessedChunksCount++;
            System.out.printf("Average time to fill the ArrayList(%d): " + (averageRunningTimePerChunk / 1000.0) + " seconds%n", maxDBBufferSize);
            System.out.printf("Average time needed to process the protein given the paralelization: %.2f seconds%n", (averageRunningTimePerChunk / 1000.0) / maxDBBufferSize);


            long insertionStartTime = System.currentTimeMillis();

            var pureComputationTimeTaken = 0.0;
            session.beginTransaction();
            for (EvaluationResult res :qContents) {
                if (AppConfig.DRY_RUN) {
                    compareWithResultInDb(res, currentPivotSet);
                } else {
                    saveResultToDb(session, res, currentPivotSet);
                }
                averageRunningTimePerChain = (averageRunningTimePerChain * soFarProcessedChains + res.getTimeTaken()) / (soFarProcessedChains + 1);
                soFarProcessedChains += 1;
                pureComputationTimeTaken += res.getTimeTaken();
            }

            session.getTransaction().commit();
            qContents.clear();

            long insertionEndTime = System.currentTimeMillis();
            long insertionTime = insertionEndTime - insertionStartTime;
            System.out.println("Time taken for insertion logic: " + (insertionTime / 1000.0) + " seconds");
            System.out.println("Processed: " + soFarProcessedChains + "/" + elgibleChainCount);
            System.out.printf("Estimated remaining time %.4f hours%n", ((averageRunningTimePerChunk / 1000.0) / maxDBBufferSize) * (elgibleChainCount-soFarProcessedChains) / 3600);
            System.out.println("Total running average: " + averageRunningTimePerChain + " seconds");
            System.out.println("------");

            //avoid thundering herd if paralleling across multiple computers
            var random = new Random();
            try {
                Thread.sleep(random.nextInt(5000 - 1000 + 1) + 5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        System.out.println("Consumer finished");
    }


    public void run(Dataset<String> dataset) { //todo loose the dataset and use your own beautiful functions
        System.out.println("Preparing data for computation...");
        int pivotCount = 512;
        var metricSpace = dataset.getMetricSpace();
        var df = dataset.getDistanceFunction();
        Thread backgroundConsumerThread = new Thread(this::consumeResults);
        var proteinChainService = new ProteinChainService(new PivotSetService(new PivotSetDao(session)), new ProteinChainForDistanceDao(session));
        var pivots = dataset.getPivots(pivotCount);
        var proteins = dataset.getMetricObjectsFromDataset();
        var elgibleChainCount = proteinChainService.getChainsCount();
        Stream<Object> proteinStream = StreamSupport.stream(Spliterators.spliterator(proteins,
                elgibleChainCount,
                Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.SIZED),
                true);

        System.out.println("Data prepared, going to process " + elgibleChainCount + " chains");
        backgroundConsumerThread.start();
        proteinStream.forEach(o -> {
            var startedAt = System.currentTimeMillis();
            int oId = Integer.parseInt((String) metricSpace.getIDOfMetricObject(o));
            Object oData = metricSpace.getDataOfMetricObject(o);
            var distMap = new HashMap<String, Double>();
            var distanceValid = true;
            for (Object p : pivots) {
                Object pData = metricSpace.getDataOfMetricObject(p);
                try {
                    float distance = df.getDistance(oData, pData);
                    var pid = metricSpace.getIDOfMetricObject(p);
                    distMap.put(pid.toString(), (double) distance);
                } catch (Exception e) {
                    distanceValid = false;
                    System.out.println("exception occured when calculating distance " + e.getMessage());
                }
            }

            var distsMetadata = new DistsMetadata();
            distsMetadata.setDists(distMap);

            try {
                var res = new EvaluationResult(startedAt, System.currentTimeMillis(), false, oId, Converter.toJsonString(distsMetadata));
                System.out.printf("Computed distances to pivots in %.2f seconds" + " for " + oId + "/" + oData + " - " + res.metadata.substring(0, 50) + "%n", res.getTimeTaken());
                if (distanceValid) {
                    queue.put(res);
                }
            } catch (JsonProcessingException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            queue.put(new EvaluationResult(0,0, true, 0, ""));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            System.out.println("Waiting for consumer to finish");
            backgroundConsumerThread.join();
            System.out.println("Done");
        } catch (InterruptedException e) {
            System.out.println("interrupted while waiting");
            throw new RuntimeException(e);
        }
    }
}
