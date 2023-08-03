package com.example.dao;

import com.example.model.PivotSet;
import com.example.model.ProteinChain;
import com.example.model.SimpleProtein;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import java.util.Iterator;
import java.util.List;

public abstract class ProteinChainDao {
    protected final Session session;

    public ProteinChainDao(Session session) {
        this.session = session;
    }

    protected abstract org.hibernate.query.Query getChainsCoreQuery(PivotSet pivotSet, String queryString);

    public Long getChainsCount(PivotSet pivotSet) {
        return (Long) getChainsCoreQuery(pivotSet, "select count(p)").getSingleResult();
    }

    private org.hibernate.query.Query getChainsSelectQuery(PivotSet pivotSet) {
        return getChainsCoreQuery(pivotSet, "select p");
    }


    public Iterator<SimpleProtein> getProteinChainIterator(PivotSet pivotSet) {
        return getProteinChainIterator(pivotSet, -1);
    }

    public Iterator<SimpleProtein> getProteinChainIterator(PivotSet pivotSet, int limit) {
        // query all rows from the Protein table
        session.beginTransaction();
        var query = getChainsSelectQuery(pivotSet);
        //the scroll mode is shit, it iterates the whole table at db. Use manual paging just like in pcms
        List<ProteinChain> results = query.getResultList();
        session.getTransaction().commit();
        //todo sql limit
        if (limit != -1 && results.size() >= limit) {
            System.out.println("Limiting to " + limit);
            results = results.subList(0, limit);
        }
        return results.stream()
                .map(pc -> new SimpleProtein(pc.getIntId(), pc.getGesamtId()))
                .iterator();

    }
}
