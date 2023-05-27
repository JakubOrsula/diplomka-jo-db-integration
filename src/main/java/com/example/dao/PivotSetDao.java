package com.example.dao;

import com.example.model.PivotSet;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class PivotSetDao {
    private final Session session;

    public PivotSetDao(Session session) {
        this.session = session;
    }

    public PivotSet GetCurrentPivotSet() {
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
