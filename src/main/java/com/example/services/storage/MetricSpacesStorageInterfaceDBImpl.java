package com.example.services.storage;

import com.example.model.*;
import com.example.services.configuration.AppConfig;
import org.hibernate.*;
import vm.metricSpace.MetricSpacesStorageInterface;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

//todo most of this class is stupid because its paired to specific functionality required by abstract metric space and adding various quirks to stay compliant
// the quirked methods should be moved to specialized loaders and only generic one should stay here


public class MetricSpacesStorageInterfaceDBImpl extends MetricSpacesStorageInterface {
    private final Session session;

    public MetricSpacesStorageInterfaceDBImpl(Session session) {
        super();
        this.session = session;
    }

    public Iterator<ProteinChainMetadata> getProteinChainMetadatas(String datasetName, Object... params) {
        // todo add some display of x/y proteins sketches ca be computed based on missing distances
        // todo maybe sort to make it deterministic
        session.beginTransaction();
        PivotSet pivotSet = getCurrentPivotSet();
        var queryString = "FROM ProteinChainMetadata WHERE pivotDistances IS NOT NULL and id.pivotSet = :pivotSet";
        var query = session.createQuery(queryString, ProteinChainMetadata.class).setParameter("pivotSet", pivotSet);
        ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
        Iterator<ProteinChainMetadata> iterator = new Iterator<ProteinChainMetadata>() {
            @Override
            public boolean hasNext() {
                boolean hasNext = results.next();
                if (!hasNext) {
                    results.close();
                }
                return hasNext;
            }

            @Override
            public ProteinChainMetadata next() {
                return (ProteinChainMetadata) results.get(0);
            }
        };

        session.getTransaction().commit();
        return iterator;
    }

    //todo this function is specialized just for computing distances
    //todo this function needs to use indexedAsDataObject not to return pivots
    public Iterator<Object> getObjectsFromDataset(String datasetName, Object... params) {
        //apply limit
        var limit = -1;
        if (params.length > 0) {
            limit = (int) params[0];
        }

        // query all rows from the Protein table
        // todo maybe sort to make it deterministic
        session.beginTransaction();
        PivotSet pivotSet = getCurrentPivotSet();
        //todo make adjusting this query atomic with the one in getElgibleProteisForDistanceComputationCount
        var query = getChainsForComputationQuery(pivotSet);
        ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
        int finalLimit = limit;
        Iterator<SimpleProtein> iterator = new Iterator<SimpleProtein>() {
            private int retrieved = 0;
            @Override
            public boolean hasNext() {
                //todo sql limit might be better so we dont over-fetch
                if (retrieved == finalLimit) {
                    return false;
                }
                boolean hasNext = results.next();
                if (!hasNext) {
                    results.close();
                }
                return hasNext;
            }

            @Override
            public SimpleProtein next() {
                var pc = (ProteinChain) results.get(0);
                retrieved++;
                return new SimpleProtein(pc.getIntId(), pc.getGesamtId());
            }
        };

        session.getTransaction().commit();
        return (Iterator<Object>) (Iterator<?>) iterator;
    }

    public Long getChainsForComputationCountQuery(PivotSet pivotSet) {
        return (Long) getChainsForComputationQuery(pivotSet, "select count(p)").getSingleResult();
    }

    private org.hibernate.query.Query getChainsForComputationQuery(PivotSet pivotSet) {
        return getChainsForComputationQuery(pivotSet, "select p");
    }

    private org.hibernate.query.Query getChainsForComputationQuery(PivotSet pivotSet, String queryString) {
        queryString += " from ProteinChain p left join ProteinChainMetadata pm on p.intId = pm.id.proteinChain.intId " +
                "where (pm.id.proteinChain is null or " +
                "((length(pm.pivotDistances) < 200 or p.added > pm.lastUpdate) and pm.id.pivotSet = :pivotSet))";
        if (AppConfig.COMPUTE_CHAIN_FROM != -1 && AppConfig.COMPUTE_CHAIN_TO != -1) {
            queryString += " and p.intId >= :chainFrom and p.intId <= :chainTo ";
        } else { //order by size destroys concept of continuous intervals by id
            queryString += " order by p.chainLength desc"; //ordering from biggest chains gives better estimates on max running time
        }
        var query = session.createQuery(queryString)
                .setParameter("pivotSet", pivotSet);
        if (AppConfig.COMPUTE_CHAIN_FROM != -1 && AppConfig.COMPUTE_CHAIN_TO != -1) {
            query.setParameter("chainFrom", AppConfig.COMPUTE_CHAIN_FROM);
            query.setParameter("chainTo", AppConfig.COMPUTE_CHAIN_TO);
        }
        return query;
    }

    //todo add test
    private <T> List<T> getEveryNBasedOnLimit(List<T> inputList, int limit) {
        if (inputList.size() % limit != 0) {
            throw new IllegalArgumentException("Input list size is not divisible by the limit.");
        }

        List<T> result = new ArrayList<>();

        int x = inputList.size() / limit;

        for (int i = 0; i < inputList.size(); i += x) {
            result.add(inputList.get(i));
        }

        return result;
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

        if (params.length > 0) {
            spList = getEveryNBasedOnLimit(spList, (int) params[0]);
        }
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
