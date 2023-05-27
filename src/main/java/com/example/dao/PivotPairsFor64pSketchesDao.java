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
        var parisCount = session.createQuery("SELECT count(*) from PivotPairsFor64pSketches where id.pivotSet = :pivotSet", Long.class)
                .setParameter("pivotSet", pivotSet)
                .getSingleResult();
        session.getTransaction().commit();
        return parisCount;
    }

    public void storePairs(PivotSet pivotSet, List<Pivot512> pivots) {
        session.beginTransaction();

        for (int i = 0; i < pivots.size(); i += 2) {
            Pivot512 pivot1 = pivots.get(i);
            Pivot512 pivot2 = pivots.get(i + 1);

            PivotPairsFor64pSketches pivotPair = new PivotPairsFor64pSketches();
            PivotPairsFor64pSketches.PivotPairsFor64pSketchesId pairId = new PivotPairsFor64pSketches.PivotPairsFor64pSketchesId();

            pairId.setPivotSet(pivotSet);
            pairId.setSketchBitOrder((short) i);
            pairId.setPivot1(pivot1);
            pairId.setPivot2(pivot2);

            pivotPair.setId(pairId);
            session.save(pivotPair);
        }

        session.getTransaction().commit();
    }
}
