package com.example.services.storage;

import com.example.model.PivotSet;
import com.example.model.Pivot512;
import com.example.model.ProteinChain;
import com.example.model.SimpleProtein;
import org.hibernate.*;
import vm.metricSpace.MetricSpacesStorageInterface;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
        Iterator<SimpleProtein> iterator = new Iterator<SimpleProtein>() {
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
            public SimpleProtein next() {
                var pc = (ProteinChain) results.get(0);
                return new SimpleProtein(pc.getIntId(), pc.getGesamtId());
            }
        };

        session.getTransaction().commit();
        return (Iterator<Object>) (Iterator<?>) iterator;
    }

    public List<Object> getPivots(String pivotSetName, Object... params) {
        //todo get only current pivots based on pivotSet
        //todo the current pivotset should be locked in redis
        session.beginTransaction();

        var pivotSet = getCurrentPivotSet();
        List<Pivot512> proteinList = session.createQuery("select p from Pivot512 p join p.id.pivotSet ps where ps = :pivotSet", Pivot512.class)
                .setParameter("pivotSet", pivotSet)
                .list();
        List<SimpleProtein> spList = proteinList.stream()
                .map(pivot -> new SimpleProtein(pivot.getId().getProteinChain().getIntId(), pivot.getId().getProteinChain().getGesamtId()))
                .collect(Collectors.toList());
        session.getTransaction().commit();
        return (List<Object>) (List<?>) spList;
    }

    // Object returned must be the same as pivot and qury and object
    public List<Object> getQueryObjects(String querySetName, Object... params) {
        return null;
    }

    public void storeObjectToDataset(Object metricObject, String datasetName, Object... additionalParamsToStoreWithNewDataset) {
        //todo we need more information than just id, gesamtid to insert row into the db
        return;
//        if (metricObject instanceof SimpleProtein) {
//            ProteinChain proteinChain = this.session.get(ProteinChain.class, ((SimpleProtein) metricObject).getIntId());
//            Transaction transaction = session.beginTransaction();
//            session.save(proteinChain);
//            transaction.commit();
//        } else {
//            throw new IllegalArgumentException("metricObject must be an instance of ProteinChain");
//        }
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

    public PivotSet getCurrentPivotSet() {
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PivotSet> criteriaQuery = criteriaBuilder.createQuery(PivotSet.class);
        Root<PivotSet> root = criteriaQuery.from(PivotSet.class);

        criteriaQuery.select(root)
                .where(criteriaBuilder.equal(root.get("currentlyUsed"), 1));

        List<PivotSet> pivotSets = session.createQuery(criteriaQuery).getResultList();

        if (!pivotSets.isEmpty()) {
            if (pivotSets.size() > 1) {
                //todo move to custom checked exception
                throw new Error("Multiple active pivot sets, unrecoverable inconsistency");
            }
            PivotSet pivotSet = pivotSets.get(0);
            return pivotSet;
        } else {
            throw new Error("No pivot set found with currentlyUsed = 1");
        }
    }
}
