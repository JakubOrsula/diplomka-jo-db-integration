package com.example.dao;

import com.example.model.Pivot512;
import com.example.model.PivotPairsFor64pSketches;
import com.example.model.PivotSet;
import org.hibernate.Session;

import java.util.List;

public class PivotPairsFor64pSketchesDao {
    private final Session session;

    public PivotPairsFor64pSketchesDao(Session session) {
        this.session = session;
    }

    public long pairsCount(PivotSet pivotSet) {
        session.beginTransaction();
        var parisCount = session.createQuery("SELECT count(*) from PivotPairsFor64pSketches where id.pivotSetId = :pivotSetId", Long.class)
                .setParameter("pivotSetId", pivotSet.getId())
                .getSingleResult();
        session.getTransaction().commit();
        return parisCount;
    }

    public void storePairs(PivotSet pivotSet, List<Pivot512> pivots) {
        session.beginTransaction();

        for (int i = 0; i < pivots.size(); i += 2) {
            Pivot512 pivot1 = pivots.get(i);
            Pivot512 pivot2 = pivots.get(i + 1);

            String sql = "insert into protein_chain_db.pivotPairsFor64pSketches values (:pivotSetId, :sketchBitOrder, :pivot1Id, :pivot2Id)";
            int affectedRows = session.createNativeQuery(sql)
                    .setParameter("pivotSetId", pivotSet.getId())
                    .setParameter("sketchBitOrder", (short) i)
                    .setParameter("pivot1Id", pivot1.getId().getProteinChain().getIntId())
                    .setParameter("pivot2Id", pivot2.getId().getProteinChain().getIntId())
                    .executeUpdate();
        }
        session.getTransaction().commit();
    }
}
