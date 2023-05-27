package com.example.dao;

import com.example.model.PivotSet;
import com.example.model.ProteinChainMetadata;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import java.util.Iterator;

public class ProteinChainMetadataDao {

    private final Session session;

    public ProteinChainMetadataDao(Session session) {
        this.session = session;
    }

    public Iterator<ProteinChainMetadata> getProteinChainMetadatasWithValidDistances(PivotSet pivotSet) {
        // todo maybe sort to make it deterministic
        session.beginTransaction();
        var queryString = "SELECT pcm FROM ProteinChainMetadata pcm " +
                "JOIN ProteinChain p ON pcm.id.proteinChain.intId = p.intId " +
                "WHERE length(pcm.pivotDistances) > 200 " +
                "and pcm.id.pivotSet = :pivotSet " +
                "and p.indexedAsDataObject = true " +
                "order by pcm.id.proteinChain.intId";
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
}
