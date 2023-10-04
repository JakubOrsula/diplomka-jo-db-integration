package com.example.dao;

import com.example.model.*;
import com.example.services.entrypoints.secondaryFiltering.AllSketchesResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;


import java.util.*;


public class ProteinChainMetadataDao {

    private final Session session;
    private final SessionFactory sessionFactory;

    public ProteinChainMetadataDao(Session session) {
        this(session, null);
    }

    public ProteinChainMetadataDao(Session session, SessionFactory sessionFactory) {
        this.session = session;
        this.sessionFactory = sessionFactory;
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

    public Iterator<ProteinDistanceData> getProteinDataWithValidDistances(PivotSet pivotSet) {
        return new Iterator<>() {
            private int currentPage = 0;
            int lastId = -1;
            private Iterator<ProteinDistanceData> currentPageIterator;

            @Override
            public boolean hasNext() {
                if (currentPageIterator == null || !currentPageIterator.hasNext()) {
                    loadNextPage();
                }
                return currentPageIterator != null && currentPageIterator.hasNext();
            }

            @Override
            public ProteinDistanceData next() {
                if (hasNext()) {
                    return currentPageIterator.next();
                } else {
                    throw new NoSuchElementException();
                }
            }

            private void loadNextPage() {
                currentPageIterator = null;
                session.beginTransaction();

                var queryString = "SELECT new com.example.model.ProteinDistanceData(pcm.id.proteinChain.intId, p.gesamtId, pcm.pivotDistances) " +
                        "FROM ProteinChainMetadata pcm " +
                        "JOIN ProteinChain p ON pcm.id.proteinChain.intId = p.intId " +
                        "WHERE  " +
                        " pcm.id.pivotSet = :pivotSet " +
                        "and p.indexedAsDataObject = true " +
                        "and pcm.id.proteinChain.intId > :offsetId " +
                        "order by pcm.id.proteinChain.intId";

                var query = session.createQuery(queryString, ProteinDistanceData.class)
                        .setParameter("pivotSet", pivotSet)
                        .setParameter("offsetId", lastId)
                        .setMaxResults(10000); //todo to properties, where more ram is available 100k is much faster

                List<ProteinDistanceData> results = query.getResultList();
                results.stream().parallel().forEach(ProteinDistanceData::convertJson);
                if (!results.isEmpty()) {
                    lastId = results.get(results.size() - 1).chainIntId;
                }
                currentPageIterator = results.iterator();

                session.getTransaction().commit();
                currentPage++;
            }
        };
    }

    public void saveSketchesPlainUpdate(List<SketchData> sketchDataList, int pivotSetId, ProteinChainMetadataColumns column) {
        Transaction transaction = null;
        String sql = "UPDATE proteinChainMetadata SET " + column.getColumnName() + " = :sketch WHERE pivotSetId = :pivotSetId AND chainIntId = :chainIntId";

        try {
            transaction = session.beginTransaction();

            var query = session.createNativeQuery(sql);

            long totalLoopStartTime = System.nanoTime();

            for (SketchData sketchData : sketchDataList) {
                long individualStartTime = System.nanoTime();

                query.setParameter("sketch", sketchData.getJsonSketch())
                        .setParameter("pivotSetId", pivotSetId)
                        .setParameter("chainIntId", sketchData.getChainIntId());

                query.executeUpdate();

                long individualEndTime = System.nanoTime();
                long individualTimeTaken = (individualEndTime - individualStartTime) / 1_000_000;
                System.out.println("Query executed in: " + individualTimeTaken + " ms");
            }

            long totalLoopEndTime = System.nanoTime();
            long totalLoopTimeTaken = (totalLoopEndTime - totalLoopStartTime) / 1_000_000;
            System.out.println("Total time for loop execution: " + totalLoopTimeTaken + " ms");

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public void transferSketches(ProteinChainMetadataColumns column) {
        session.beginTransaction();
        session.createNativeQuery("UPDATE proteinChainMetadata\n" +
                "    INNER JOIN proteinChainMetadataTransfer\n" +
                "    ON proteinChainMetadata.pivotSetId = proteinChainMetadataTransfer.pivotSetId\n" +
                "        AND proteinChainMetadata.chainIntId = proteinChainMetadataTransfer.chainIntId\n" +
                "SET proteinChainMetadata." + column.getColumnName() + " = proteinChainMetadataTransfer." + column.getColumnName() + ";").executeUpdate();
        session.getTransaction().commit();
    }

    public void cleanTransferTable() {
        session.beginTransaction();
        session.createNativeQuery("truncate table proteinChainMetadataTransfer").executeUpdate();
        session.getTransaction().commit();
    }


    public void saveSketchesThroughTransferTable(List<SketchData> sketchDataList, int pivotSetId, ProteinChainMetadataColumns column) {
        Transaction transaction = session.beginTransaction();

        StringBuilder insertValues = new StringBuilder();
        // unfortunately hibernate does not support mass insert prepared statements for mariadb
        for (SketchData sketchData : sketchDataList) {
            insertValues.append("(")
                    .append(pivotSetId).append(",")
                    .append(sketchData.getChainIntId()).append(", '")
                    .append(sketchData.getJsonSketch()).append("'),");
        }

        // Remove the last comma
        insertValues.setLength(insertValues.length() - 1);

        String sql = "INSERT INTO proteinChainMetadataTransfer (pivotSetId, chainIntId, " + column.getColumnName() + ") " +
                "VALUES " + insertValues;
        session.createNativeQuery(sql).executeUpdate();

        transaction.commit();
    }

    public long[] getSketch(int pivotSetId, int chainIntId, ProteinChainMetadataColumns column) {
        Transaction transaction = null;
        String sql = "SELECT " + column.getColumnName() + " FROM proteinChainMetadata WHERE pivotSetId = :pivotSetId AND chainIntId = :chainIntId";

        try {
            transaction = session.beginTransaction();

            var query = session.createNativeQuery(sql);

            query.setParameter("pivotSetId", pivotSetId)
                    .setParameter("chainIntId", chainIntId);

            var result = query.getSingleResult();

            transaction.commit();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(result.toString());
            JsonNode arrayNode = node.get("sk1024_long");

            List<Long> longList = new ArrayList<>();
            if (arrayNode.isArray()) {
                for (JsonNode arrayElement : arrayNode) {
                    longList.add(arrayElement.asLong());
                }
            }

            long[] array = longList.stream().mapToLong(l -> l).toArray();

            return array;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException(e);
        }
    }
    public AllSketchesResult getAllSketches(int pivotSetId, ProteinChainMetadataColumns column) {
        Transaction transaction = null;
        String sql = "SELECT chainIntId, " + column.getColumnName() + " FROM proteinChainMetadata WHERE pivotSetId = :pivotSetId";

        try {
            transaction = session.beginTransaction();

            var query = session.createNativeQuery(sql);

            query.setParameter("pivotSetId", pivotSetId);

            var resultList = query.getResultList();
            System.out.println("Sketch result list size: " + resultList.size());

            transaction.commit();

            ObjectMapper mapper = new ObjectMapper();
            List<long[]> arraysList = new ArrayList<>();
            Map<Integer, Integer> idToIndexMap = new HashMap<>();

            int index = 0;
            for (Object result : resultList) {
                Object[] resultArray = (Object[]) result;
                int chainIntId = ((Number) resultArray[0]).intValue();
                if (Objects.equals(resultArray[1].toString(), "")) {
                    System.out.println("Empty sketch for chainIntId: " + chainIntId);
                    continue;
                }
                JsonNode node = mapper.readTree(resultArray[1].toString());
                JsonNode arrayNode = node.get("sk1024_long");

                List<Long> longList = new ArrayList<>();
                if (arrayNode.isArray()) {
                    for (JsonNode arrayElement : arrayNode) {
                        longList.add(arrayElement.asLong());
                    }
                }

                long[] array = longList.stream().mapToLong(l -> l).toArray();
                arraysList.add(array);
                idToIndexMap.put(chainIntId, index++);
            }

            var result = new AllSketchesResult();
            result.sketches = arraysList;
            result.mapping = idToIndexMap;
            return result;
        } catch (Exception e) {
            System.out.println("Error in getAllSketches" + e.getMessage());
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException(e);
        }
    }


}

