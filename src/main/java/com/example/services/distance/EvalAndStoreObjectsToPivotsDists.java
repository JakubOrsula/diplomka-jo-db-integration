package com.example.services.distance;

import com.example.model.ProteinChain;
import com.example.model.ProteinChainMetadata;
import com.example.model.json.Converter;
import com.example.model.json.DistsMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hibernate.Session;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.distance.DistanceFunctionInterface;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Vlada
 */
public class EvalAndStoreObjectsToPivotsDists {

    private final Session session;

    public EvalAndStoreObjectsToPivotsDists(Session session) {
        this.session = session;
    }

    public static final Logger LOGGER = Logger.getLogger(EvalAndStoreObjectsToPivotsDists.class.getName());


    public void run(Dataset<String> dataset) {
        int pivotCount = 512;
        var metricSpace = dataset.getMetricSpace();
        var df = dataset.getDistanceFunction();

        var pivots = dataset.getPivotsForTheSameDataset(pivotCount);
        var proteins = dataset.getMetricObjectsFromDataset();
        Stream<Object> proteinStream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(proteins, Spliterator.ORDERED), true);
        proteinStream.forEach(o -> {
            String oId = metricSpace.getIDOfMetricObject(o).toString();
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
                System.out.print(oData);
                System.out.print(": ");
                System.out.println(Converter.toJsonString(distsMetadata));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
//        for (int i = 1; proteins.hasNext(); i++) {
//            Object o = proteins.next();
//            String oId = metricSpace.getIDOfMetricObject(o).toString();
//            Object oData = metricSpace.getDataOfMetricObject(o);
//            var distMap = new HashMap<String, Double>();
//            for (Object p : pivots) {
//                Object pData = metricSpace.getDataOfMetricObject(p);
//                float distance = df.getDistance(oData, pData);
//                var pid = metricSpace.getIDOfMetricObject(p);
//                distMap.put(pid.toString(), (double) distance);
//            }
//
//            var distsMetadata = new DistsMetadata();
//            distsMetadata.setDists(distMap);
//
//            try {
//                System.out.print(oData);
//                System.out.print(": ");
//                System.out.println(Converter.toJsonString(distsMetadata));
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }


//            ProteinChain proteinChain = this.session.get(ProteinChain.class, oId);
//            var pmId = new ProteinChainMetadata.ProteinChainMetadataId();
//            pmId.setProteinChain(proteinChain);
//            pmId.setPivotSet(1); //todo
//
//            try {
//                var pMetadata = new ProteinChainMetadata();
//                pMetadata.setPivotDistances(Converter.toJsonString(distsMetadata));
//                pMetadata.setId();
//            } catch (JsonProcessingException e) {
//                //todo log
//                throw new RuntimeException(e);
//            }
//            if (i % 10000 == 0) {
//                LOGGER.log(Level.INFO, "Evaluated {0} objects", i);
//            }
//        }
    }

}
