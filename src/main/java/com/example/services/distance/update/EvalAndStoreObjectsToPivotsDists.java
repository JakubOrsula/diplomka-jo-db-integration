package com.example.services.distance.update;

import com.example.model.ProteinChain;
import com.example.model.ProteinChainMetadata;
import com.example.model.json.Converter;
import com.example.model.json.DistsMetadata;
import com.example.services.storage.MetricSpacesStorageInterfaceDBImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hibernate.Session;
import vm.metricSpace.Dataset;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EvalAndStoreObjectsToPivotsDists {

    private final Session session;

    public EvalAndStoreObjectsToPivotsDists(Session session) {
        this.session = session;
    }

    public static final Logger LOGGER = Logger.getLogger(EvalAndStoreObjectsToPivotsDists.class.getName());

    private final int maxQSize = 1024;
    private final int maxDBBufferSize = 100;
    private LinkedBlockingQueue<EvaluationResult> queue = new LinkedBlockingQueue<>(maxQSize);

    public void consumeResults() {
        System.out.println("Initializing consumer");
        var storage = new MetricSpacesStorageInterfaceDBImpl(session);
        var currentPivotSet = storage.getCurrentPivotSet();
        var elgibleChainCount = storage.getElgibleProteisForDistanceComputationCount();
        var soFarProcessedChains = 0L;
        var averageChainRunningTime = 0.0;
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
            System.out.println("Time taken to fill the ArrayList: " + (fillTime / 1000.0) + " seconds");

            long insertionStartTime = System.currentTimeMillis();

            var pureComputationTimeTaken = 0.0;
            session.beginTransaction();
            for (EvaluationResult res :qContents) {
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

                averageChainRunningTime = (averageChainRunningTime * soFarProcessedChains + res.getTimeTaken()) / (soFarProcessedChains + 1);
                soFarProcessedChains += 1;
                pureComputationTimeTaken += res.getTimeTaken();
            }
            System.out.println("(flawed) parallel overhead: " + (fillTime - (pureComputationTimeTaken * 1000 / 16)) + "ms");

            session.getTransaction().commit();
            qContents.clear();

            long insertionEndTime = System.currentTimeMillis();
            long insertionTime = insertionEndTime - insertionStartTime;
            System.out.println("Time taken for insertion logic: " + (insertionTime / 1000.0) + " seconds");
            System.out.println("Processed: " + soFarProcessedChains + "/" + elgibleChainCount);
            System.out.println("Total running average: " + averageChainRunningTime + " seconds");
            System.out.println("------");
        }
        System.out.println("Consumer finished");
    }


    public void run(Dataset<String> dataset) {
        int pivotCount = 512;
        var metricSpace = dataset.getMetricSpace();
        var df = dataset.getDistanceFunction();

        Thread backgroundConsumerThread = new Thread(this::consumeResults);
        backgroundConsumerThread.start();
        System.out.println("Preparing data for computation...");
        var storage = new MetricSpacesStorageInterfaceDBImpl(session);
        var pivots = dataset.getPivotsForTheSameDataset(pivotCount);
        var proteins = dataset.getMetricObjectsFromDataset();
        Stream<Object> proteinStream = StreamSupport.stream(Spliterators.spliterator(proteins,
                storage.getElgibleProteisForDistanceComputationCount(),
                Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.SIZED),
                true);
        System.out.println("Data prepared, processing...");
        proteinStream.forEach(o -> {
            var startedAt = System.currentTimeMillis();
            int oId = (int) metricSpace.getIDOfMetricObject(o);
            Object oData = metricSpace.getDataOfMetricObject(o);
            var distMap = new HashMap<String, Double>();
            for (Object p : pivots) {
                Object pData = metricSpace.getDataOfMetricObject(p);
                float distance = df.getDistance(oData, pData);
                var pid = metricSpace.getIDOfMetricObject(p);
                distMap.put(pid.toString(), (double) distance);
            }

            var distsMetadata = new DistsMetadata();
            distsMetadata.setDists(distMap);

            try {
                var res = new EvaluationResult(startedAt, System.currentTimeMillis(), false, oId, Converter.toJsonString(distsMetadata));
                System.out.printf("Computed distances to pivots in %.2f seconds" + " for " + oId + "/" + oData + " - " + res.metadata + "%n", res.getTimeTaken());
                queue.put(res);
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
            backgroundConsumerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
