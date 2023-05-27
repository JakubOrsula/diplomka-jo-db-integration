package com.example.dao;

import com.example.model.PivotSet;
import com.example.model.ProteinChain;
import com.example.model.SimpleProtein;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import java.util.Iterator;

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
        ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
        int finalLimit = limit;
        Iterator<SimpleProtein> iterator = new Iterator<>() {
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
        return iterator;
    }
}
