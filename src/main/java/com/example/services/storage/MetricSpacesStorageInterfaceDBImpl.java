package com.example.services.storage;

import com.example.model.PivotSet;
import com.example.model.Pivot512;
import com.example.model.ProteinChain;
import org.hibernate.*;
import vm.metricSpace.MetricSpacesStorageInterface;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MetricSpacesStorageInterfaceDBImpl extends MetricSpacesStorageInterface {
    private final Session session;

    public MetricSpacesStorageInterfaceDBImpl(Session session) {
        super();
        this.session = session;
    }


    public Iterator<Object> getObjectsFromDataset(String datasetName, Object... params) {
        // query all rows from the Protein table
        // todo maybe sort to make it deterministic
        session.beginTransaction();

        ScrollableResults results = session.createQuery("from ProteinChain").scroll(ScrollMode.FORWARD_ONLY);
        Iterator<ProteinChain> iterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                boolean hasNext = results.next();
                if (!hasNext) {
                    results.close();
                    session.close();
                }
                return hasNext;
            }

            @Override
            public ProteinChain next() {
                return (ProteinChain) results.get(0);
            }
        };

        session.getTransaction().commit();
        return (Iterator<Object>) (Iterator<?>) iterator;
    }

    public List<Object> getPivots(String pivotSetName, Object... params) {
        //todo get only current pivots based on pivotSet
        session.beginTransaction();

        var proteinList = session.createQuery("from Pivot512").list();
        session.getTransaction().commit();
        return (List<Object>) (List<?>) proteinList;
    }

    // Object returned must be the same as pivot and qury and object
    public List<Object> getQueryObjects(String querySetName, Object... params) {
        return null;
    }

    public void storeObjectToDataset(Object metricObject, String datasetName, Object... additionalParamsToStoreWithNewDataset) {
        if (metricObject instanceof ProteinChain) {
            ProteinChain proteinChain = (ProteinChain) metricObject;
            Transaction transaction = session.beginTransaction();
            session.save(proteinChain);
            transaction.commit();
        } else {
            throw new IllegalArgumentException("metricObject must be an instance of ProteinChain");
        }
    }

    //todo typo
    public void storePivots(List<Object> pivots, String pivotSetName, Object... additionalParamsToStoreWithNewPivotSet) {
        List<Long> pivotIds = pivots.stream()
                .map(o -> (Long) o)
                .collect(Collectors.toList());

        // Create a new PivotSet
        PivotSet pivotSet = new PivotSet();
        pivotSet.setCurrentlyUsed(0);
        pivotSet.setAdded(new Timestamp(System.currentTimeMillis()));


        session.beginTransaction();
        session.save(pivotSet);

        // Create Pivot512 objects using the list of longs as chainIntId and the id of the created PivotSet as pivotSetId
        for (Long chainIntId : pivotIds) {
            Pivot512 pivot512 = new Pivot512();

            Pivot512.Pivot512Id pivot512Id = new Pivot512.Pivot512Id();
            pivot512Id.setProteinChain(session.load(ProteinChain.class, chainIntId));
            pivot512Id.setPivotSet(pivotSet);

            pivot512.setId(pivot512Id);

            session.save(pivot512); //todo maybe use persist instead
        }
        session.getTransaction().commit();
    }

    public void storeQueryObjects(List<Object> queryObjs, String querySetName, Object... additionalParamsToStoreWithNewQuerySet) {

    }

    public int getPrecomputedDatasetSize(String datasetName) {
        return 0;
    }

    protected void updateDatasetSize(String datasetName, int count) {

    }
}
