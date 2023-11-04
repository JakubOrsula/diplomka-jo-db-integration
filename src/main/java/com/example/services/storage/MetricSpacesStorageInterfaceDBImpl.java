package com.example.services.storage;

import com.example.model.*;
import com.example.service.PivotService;
import com.example.service.ProteinChainMetadataService;
import com.example.service.distance.ProteinChainService;
import com.example.services.configuration.AppConfig;
import org.hibernate.cfg.NotYetImplementedException;
import vm.metricSpace.AbstractMetricSpacesStorage;

import java.util.*;

/*
  Very confusing class. First half used to retrieve proteins, second to learn and apply sketches. Should be at least somehow split,
  but the only correct way to handle it is to remove it as a whole.
  For example proteinChainMetadaService is needed just for applying sketches.
 */
public class MetricSpacesStorageInterfaceDBImpl extends AbstractMetricSpacesStorage {
    private final PivotService pivotService;
    private final ProteinChainService proteinChainService;
    private final ProteinChainMetadataService proteinChainMetadataService;

    public MetricSpacesStorageInterfaceDBImpl(PivotService pivotService, ProteinChainService proteinChainService, ProteinChainMetadataService proteinChainMetadataService) {
        super();
        this.pivotService = pivotService;
        this.proteinChainService = proteinChainService;
        this.proteinChainMetadataService = proteinChainMetadataService;
    }

    public Iterator<Object> getObjectsFromDataset(String datasetName, Object... params) {
        // todo use the limit less function for without limit queries
        //apply limit
        var limit = -1;
        if (params.length > 0) {
            limit = (int) params[0];
        }

        var iterator = proteinChainService.getProteinChainIterator(limit);
        return (Iterator<Object>) (Iterator<?>) iterator;
    }

    public List<Object> getPivots(String pivotSetName, Object... params) {
        List<SimpleProtein> spList = (params.length > 0) ? pivotService.GetPivotsAsSimpleProteins((int) params[0]) : pivotService.GetPivotsAsSimpleProteins();
        return (List<Object>) (List<?>) spList;
    }

    // Object returned must be the same as pivot and query and object
    // todo we are not handling queryobjects currently
    public List<Object> getQueryObjects(String querySetName, Object... params) {
        var loadedQueryObjects = proteinChainService.getProteinsThatAreQueryObjects();
        System.out.println("Loaded query objects: " + loadedQueryObjects.size());
        return (List<Object>) (List<?>) loadedQueryObjects;
    }

    @Override
    public void storeObjectToDataset(Object metricObject, String datasetName, Object... additionalParamsToStoreWithNewDataset) {
        throw new RuntimeException("use storeObjectsToDataset to store in bulk instead");
    }

    //todo used just for applying sketches
    //todo move the implementation details to a service
    public synchronized int storeObjectsToDataset(Iterator<Object> it, int count, String datasetName, Object... additionalParamsToStoreWithNewDataset) {
        if (AppConfig.DRY_RUN) {
            return 0;
        }
        List<SketchData> sketchDataList = new ArrayList<>();

        proteinChainMetadataService.ensureEmptyTransferTable();
        long counter = 0;
        while (it.hasNext()) {
            counter++;
            AbstractMap.SimpleEntry entry = (AbstractMap.SimpleEntry) it.next();
            int chainId = Integer.parseInt((String) entry.getKey());
            long[] sketch = (long[]) entry.getValue();

            if (sketch.length == 0) {
                sketch = new long[]{0};
            }

            sketchDataList.add(new SketchData(chainId, sketch));

            final int batch_size = 5000;
            if (sketchDataList.size() == batch_size || !it.hasNext()) {
                long startInsert = System.currentTimeMillis();
                proteinChainMetadataService.saveSketches(sketchDataList);
                sketchDataList.clear();
                long endInsert = System.currentTimeMillis();
                System.out.println("Batch inserted in: " + ((endInsert - startInsert) / 1000.0) + " seconds. Total inserted rows: " + counter);
            }
        }
        System.out.println("Sketches will be transferred");
        proteinChainMetadataService.transferSketches();
        System.out.println("Sketches transferred");
        proteinChainMetadataService.ensureEmptyTransferTable();
        return 0;
    }

    //todo used just for applying sketches
    // move to dao and and for everything besides applying sketches throw an exception
    // mic - nepotrebne ukladat pivoty
    public void storePivots(List<Object> pivots, String pivotSetName, Object... additionalParamsToStoreWithNewPivotSet) {
        //we are not storing pivot sketches as we do not allow qurerying for pivots
    }

    public void storeQueryObjects(List<Object> queryObjs, String querySetName, Object... additionalParamsToStoreWithNewQuerySet) {
        throw new NotYetImplementedException();

    }

    public int getPrecomputedDatasetSize(String datasetName) {
        return 0;
    }

    protected void updateDatasetSize(String datasetName, int count) {

    }
}
