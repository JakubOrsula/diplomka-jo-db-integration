package com.example.dao;

import com.example.model.PivotSet;
import com.example.model.ProteinChainMetadata;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


public class ProteinChainMetadataDao {

    private final Session session;

    public ProteinChainMetadataDao(Session session) {
        this.session = session;
    }

    public Iterator<ProteinChainMetadata> getProteinChainMetadatasWithValidDistances(PivotSet pivotSet) {
        int pageSize = 1000; // Adjust as needed

        return new Iterator<>() {
            private int currentPage = 0;
            private Iterator<ProteinChainMetadata> currentPageIterator;

            @Override
            public boolean hasNext() {
                if (currentPageIterator == null || !currentPageIterator.hasNext()) {
                    loadNextPage();
                }
                return currentPageIterator != null && currentPageIterator.hasNext();
            }

            @Override
            public ProteinChainMetadata next() {
                if (hasNext()) {
                    return currentPageIterator.next();
                } else {
                    throw new NoSuchElementException();
                }
            }

            private void loadNextPage() {
                session.beginTransaction();

                var queryString = "SELECT pcm FROM ProteinChainMetadata pcm " +
                        "JOIN ProteinChain p ON pcm.id.proteinChain.intId = p.intId " +
                        "WHERE  " +
                        " pcm.id.pivotSet = :pivotSet " +
                        "and p.indexedAsDataObject = true " +
                        "order by pcm.id.proteinChain.intId";

                var query = session.createQuery(queryString, ProteinChainMetadata.class)
                        .setParameter("pivotSet", pivotSet)
                        .setFirstResult(currentPage * pageSize)
                        .setMaxResults(pageSize);

                List<ProteinChainMetadata> results = query.getResultList();
                currentPageIterator = results.iterator();

                session.getTransaction().commit();
                currentPage++;
            }
        };
    }


    public Iterator<String> getProteinDistancesWithValidDistances(PivotSet pivotSet) {
        int pageSize = 1000; // Adjust as needed

        return new Iterator<>() {
            private int currentPage = 0;
            private Iterator<String> currentPageIterator;

            @Override
            public boolean hasNext() {
                if (currentPageIterator == null || !currentPageIterator.hasNext()) {
                    loadNextPage();
                }
                return currentPageIterator != null && currentPageIterator.hasNext();
            }

            @Override
            public String next() {
                if (hasNext()) {
                    return currentPageIterator.next();
                } else {
                    throw new NoSuchElementException();
                }
            }

            private void loadNextPage() {
                session.beginTransaction();

                var queryString = "SELECT pcm.pivotDistances FROM ProteinChainMetadata pcm " +
                        "JOIN ProteinChain p ON pcm.id.proteinChain.intId = p.intId " +
                        "WHERE  " +
                        " pcm.id.pivotSet = :pivotSet " +
                        "and p.indexedAsDataObject = true " +
                        "order by pcm.id.proteinChain.intId";

                var query = session.createQuery(queryString, String.class)
                        .setParameter("pivotSet", pivotSet)
                        .setFirstResult(currentPage * pageSize)
                        .setMaxResults(pageSize);

                List<String> results = query.getResultList();
                currentPageIterator = results.iterator();

                session.getTransaction().commit();
                currentPage++;
            }
        };
    }

    public Iterator<ProteinDataDao> getProteinDataWithValidDistances(PivotSet pivotSet) {
        return new Iterator<>() {
            private int currentPage = 0;
            int lastId = -1;
            private Iterator<ProteinDataDao> currentPageIterator;

            @Override
            public boolean hasNext() {
                if (currentPageIterator == null || !currentPageIterator.hasNext()) {
                    loadNextPage();
                }
                return currentPageIterator != null && currentPageIterator.hasNext();
            }

            @Override
            public ProteinDataDao next() {
                if (hasNext()) {
                    return currentPageIterator.next();
                } else {
                    throw new NoSuchElementException();
                }
            }

            private void loadNextPage() {
                session.beginTransaction();

                var queryString = "SELECT new com.example.dao.ProteinDataDao(pcm.id.proteinChain.intId, p.gesamtId, pcm.pivotDistances) " +
                        "FROM ProteinChainMetadata pcm " +
                        "JOIN ProteinChain p ON pcm.id.proteinChain.intId = p.intId " +
                        "WHERE  " +
                        " pcm.id.pivotSet = :pivotSet " +
                        "and p.indexedAsDataObject = true " +
                        "and pcm.id.proteinChain.intId > :offsetId " +
                        "order by pcm.id.proteinChain.intId";

                var query = session.createQuery(queryString, ProteinDataDao.class)
                        .setParameter("pivotSet", pivotSet)
                        .setParameter("offsetId", lastId)
                        .setMaxResults(10000);

                List<ProteinDataDao> results = query.getResultList();
                if (!results.isEmpty()) {
                    lastId = results.get(results.size() - 1).chainIntId;
                }
                currentPageIterator = results.iterator();

                session.getTransaction().commit();
                currentPage++;
            }
        };
    }
}
