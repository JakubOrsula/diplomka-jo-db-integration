package com.example.services.storage;

import com.example.model.*;
import com.example.service.PivotService;
import com.example.service.distance.ProteinChainService;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.cfg.NotYetImplementedException;
import vm.metricSpace.MetricSpacesStorageInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MetricSpacesStorageInterfaceDBImpl extends MetricSpacesStorageInterface {
    private final PivotService pivotService;
    private final ProteinChainService proteinChainService;


    public MetricSpacesStorageInterfaceDBImpl(PivotService pivotService, ProteinChainService proteinChainService) {
        super();
        this.pivotService = pivotService;
        this.proteinChainService = proteinChainService;
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
        throw new NotYetImplementedException();
//        if (metricObject instanceof SimpleProtein) {
//            ProteinChain proteinChain = this.session.get(ProteinChain.class, ((SimpleProtein) metricObject).getIntId());
//            Transaction transaction = session.beginTransaction();
//            session.save(proteinChain);
//            transaction.commit();
//        } else {
//            throw new IllegalArgumentException("metricObject must be an instance of ProteinChain");
//        }
    }

    //todo used just for applying sketches
    // move to dao and and for everything besides applying sketches throw an exception
    // mic - nepotrebne ukladat pivoty
    public void storePivots(List<Object> pivots, String pivotSetName, Object... additionalParamsToStoreWithNewPivotSet) {

//        throw new NotYetImplementedException("pivotsets are created using python scripts");
        //todo why do we need to store the pivots explicitly?
    }

    public void storeQueryObjects(List<Object> queryObjs, String querySetName, Object... additionalParamsToStoreWithNewQuerySet) {

    }

    public int getPrecomputedDatasetSize(String datasetName) {
        return 0;
    }

    protected void updateDatasetSize(String datasetName, int count) {

    }
}
