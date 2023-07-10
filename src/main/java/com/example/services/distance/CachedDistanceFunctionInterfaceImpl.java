package com.example.services.distance;

import com.example.dao.PivotSetDao;
import com.example.dao.ProteinChainMetadataDao;
import com.example.dao.ProteinDataDao;
import com.example.model.SimpleProtein;
import com.example.model.json.Converter;
import com.example.service.PivotService;
import com.example.service.PivotSetService;
import com.example.service.ProteinChainMetadaService;
import org.hibernate.Session;
import vm.metricSpace.distance.DistanceFunctionInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CachedDistanceFunctionInterfaceImpl<T> implements DistanceFunctionInterface<String> {
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

        var proteinChainMetadataService = new ProteinChainMetadaService(new ProteinChainMetadataDao(session), new PivotSetService(new PivotSetDao(session)));
        var pcms = proteinChainMetadataService.getProteinChainDistancesData();
        AtomicInteger counter = new AtomicInteger(0);
        //fill out the matrix
        while (pcms.hasNext() && counter.get() < sampleSize) {
            ProteinDataDao pd = pcms.next();
            // int id into row
            protToIdxMapping.put(pd.gesamtId, counter.get());
            var dist_dict = Converter.fromJsonString(pd.pivotDistances).getDists();
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
    public float getDistance(String pivotGesamt, String proteinGesamt) {
        var protIdx = protToIdxMapping.get(proteinGesamt);
        var pivotIdx = pivotToIdxMapping.get(pivotGesamt);
        if (protIdx == null || pivotIdx == null) {
            throw new Error("distance not found in cache");
        }
        return cache[protIdx][pivotIdx];
    }
}
