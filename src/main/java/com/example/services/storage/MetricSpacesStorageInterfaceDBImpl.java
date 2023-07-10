package com.example.services.storage;

import com.example.model.*;
import com.example.service.PivotService;
import com.example.service.ProteinChainMetadaService;
import com.example.service.distance.ProteinChainService;
import org.hibernate.cfg.NotYetImplementedException;
import vm.metricSpace.MetricSpacesStorageInterface;

import java.util.*;

/*
  Very confusing class. First half used to retrieve proteins, second to learn and apply sketches. Should be at least somehow split,
  but the only correct way to handle it is to remove it as a whole.
  For example proteinChainMetadaService is needed just for applying sketches.
 */
public class MetricSpacesStorageInterfaceDBImpl extends MetricSpacesStorageInterface {
    private final PivotService pivotService;
    private final ProteinChainService proteinChainService;
    private final ProteinChainMetadaService proteinChainMetadaService;

    public MetricSpacesStorageInterfaceDBImpl(PivotService pivotService, ProteinChainService proteinChainService, ProteinChainMetadaService proteinChainMetadaService) {
        super();
        this.pivotService = pivotService;
        this.proteinChainService = proteinChainService;
        this.proteinChainMetadaService = proteinChainMetadaService;
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
        return new ArrayList<>();
    }

    public void storeObjectToDataset(Object metricObject, String datasetName, Object... additionalParamsToStoreWithNewDataset) {
        //todo we need more information than just id, gesamtid to insert row into the db
        var entry = (AbstractMap.SimpleEntry) metricObject;
        var chainId = Integer.parseInt((String) entry.getKey());
        var sketch = (long[]) entry.getValue();
        System.out.println("Will store chain " + entry.getKey() + " with " + sketch[0]);
        proteinChainMetadaService.saveSketch(chainId, sketch);
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
