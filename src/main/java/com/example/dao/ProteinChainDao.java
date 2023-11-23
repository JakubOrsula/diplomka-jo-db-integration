package com.example.dao;

import com.example.model.PivotSet;
import com.example.model.ProteinChain;
import com.example.model.SimpleProtein;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import java.util.ArrayList;
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
        return getChainsCoreQuery(pivotSet, "select p.intId, p.gesamtId");
    }


    public Iterator<SimpleProtein> getProteinChainIterator(PivotSet pivotSet) {
        return getProteinChainIterator(pivotSet, -1);
    }

    public Iterator<SimpleProtein> getProteinChainIterator(PivotSet pivotSet, int limit) {
        // query all rows from the Protein table
        session.beginTransaction();
        var query = getChainsSelectQuery(pivotSet);
        List<Object[]> results = query.getResultList();
        session.getTransaction().commit();
        //todo sql limit
        if (limit != -1 && results.size() >= limit) {
            System.out.println("Limiting to " + limit);
            results = results.subList(0, limit);
        }
        return results.stream()
                .map(pc -> new SimpleProtein((Integer) pc[0], (String) pc[1]))
                .iterator();

    }

    public List<SimpleProtein> getProteinsThatAreQueryObjects() {
        var query = session.createQuery("select pc.intId, pc.gesamtId from QueryProtein qp join ProteinChain pc on qp.proteinChain.intId = pc.intId where pc.indexedAsDataObject = true");
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(pc -> new SimpleProtein((Integer) pc[0], (String) pc[1])).toList();
    }
}
