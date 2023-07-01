package com.example.dao;

import com.example.model.Pivot512;
import com.example.model.PivotSet;
import org.hibernate.Session;

import java.math.BigInteger;
import java.util.List;

public class PivotPairsForXpSketchesDao {
    private final Session session;

    public PivotPairsForXpSketchesDao(Session session) {
        this.session = session;
    }

    public long pairsCount(PivotSet pivotSet, String storageTableName) {
        session.beginTransaction();
        var parisCount = session.createNativeQuery("SELECT count(*) from protein_chain_db." + storageTableName + " where pivotSetId = :pivotSetId")
                .setParameter("pivotSetId", pivotSet.getId())
                .getSingleResult();
        session.getTransaction().commit();
        return ((BigInteger) parisCount).longValue();
    }

    public void storePairs(PivotSet pivotSet, List<Pivot512> pivots, String storageTableName) {
        session.beginTransaction();

        for (int i = 0; i < pivots.size(); i += 2) {
            Pivot512 pivot1 = pivots.get(i);
            Pivot512 pivot2 = pivots.get(i + 1);

            String sql = "insert into protein_chain_db." + storageTableName + " values (:pivotSetId, :sketchBitOrder, :pivot1Id, :pivot2Id)";
            var affectedRows = session.createNativeQuery(sql)
                    .setParameter("pivotSetId", pivotSet.getId())
                    .setParameter("sketchBitOrder", (short) i)
                    .setParameter("pivot1Id", pivot1.getId().getProteinChain().getIntId())
                    .setParameter("pivot2Id", pivot2.getId().getProteinChain().getIntId())
                    .executeUpdate();
        }
        session.getTransaction().commit();
    }
}
