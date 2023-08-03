package com.example.services.distance;

import com.example.dao.PivotSetDao;
import com.example.dao.ProteinChainMetadataDao;
import com.example.model.ProteinDistanceData;
import com.example.model.SimpleProtein;
import com.example.service.PivotService;
import com.example.service.PivotSetService;
import com.example.service.ProteinChainMetadataService;
import org.hibernate.Session;
import vm.metricSpace.distance.DistanceFunctionInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CachedDistanceFunctionInterfaceImpl<T> extends DistanceFunctionInterface<String> {
    private final float[][] cache;
    private final Map<String, Integer> protToIdxMapping = new HashMap<>();
    private final Map<String, Integer> pivotToIdxMapping = new HashMap<>();

    public CachedDistanceFunctionInterfaceImpl(Session session, PivotService pivotService, int sampleSize, int numberOfPivots) throws IOException {
        cache = new float[sampleSize][numberOfPivots];
        List<SimpleProtein> dataSetSample = new ArrayList<>();

        Map<Integer, Integer> intPivotToIdxMapping = new HashMap<>();
        var pivots = pivotService.GetPivotsAsSimpleProteins(numberOfPivots);
        for (int i = 0; i < pivots.size(); i++) {
            pivotToIdxMapping.put(pivots.get(i).getGesamtId(), i);
            intPivotToIdxMapping.put(pivots.get(i).getIntId(), i);
        }

        var proteinChainMetadataService = new ProteinChainMetadataService(new ProteinChainMetadataDao(session), new PivotSetService(new PivotSetDao(session)));
        var pcms = proteinChainMetadataService.getProteinChainDistancesData();
        AtomicInteger counter = new AtomicInteger(0);
        //fill out the matrix
        while (pcms.hasNext() && counter.get() < sampleSize) {
            ProteinDistanceData pd = pcms.next();
            // int id into row
            protToIdxMapping.put(pd.gesamtId, counter.get());
            var dist_dict = pd.metadata.getDists();
            dataSetSample.add(new SimpleProtein(pd.chainIntId, pd.gesamtId));

            //dist into matrix, ordered by columnHeaders
            intPivotToIdxMapping.forEach((key, value) -> {
                Double dist = dist_dict.get(key.toString());
                float distFloat = dist.floatValue();
                cache[counter.get()][value] = distFloat;
            });
            counter.incrementAndGet();
            if (counter.getOpaque() % 10000 == 0)
                System.out.println("Prepared " + counter + " matrix rows");
        }
    }

    @Override
    public float getDistance(String gid1, String gid2) {
        Integer protIdx;
        Integer pivotIdx;
        if (gid1.startsWith("@")) {
            protIdx = protToIdxMapping.get(gid2);
            pivotIdx = pivotToIdxMapping.get(gid1);
        } else {
            protIdx = protToIdxMapping.get(gid1);
            pivotIdx = pivotToIdxMapping.get(gid2);
        }
        if (protIdx == null || pivotIdx == null) {
            if (protToIdxMapping.get(gid1) != null && pivotToIdxMapping.get(gid2) != null) {
                throw new Error("reversed distance function args " + gid1 + ", " + gid2);
            }
            throw new Error("distance not found in cache " + gid1 + ", " + gid2);
        }
        return cache[protIdx][pivotIdx];
    }
}
