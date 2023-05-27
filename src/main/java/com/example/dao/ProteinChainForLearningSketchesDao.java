package com.example.dao;

import com.example.model.PivotSet;
import com.example.model.ProteinChain;
import com.example.services.configuration.AppConfig;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class ProteinChainForLearningSketchesDao extends ProteinChainDao {
    public ProteinChainForLearningSketchesDao(Session session) {
        super(session);
    }

    @Override
    protected org.hibernate.query.Query getChainsCoreQuery(PivotSet pivotSet, String queryString) {
        queryString += " from ProteinChain p join ProteinChainMetadata pm on p.intId = pm.id.proteinChain.intId " +
                "where (pm.id.proteinChain is not null or " +
                "((length(pm.pivotDistances) < 200 or p.added > pm.lastUpdate) and pm.id.pivotSet = :pivotSet))";
        return session.createQuery(queryString)
                .setParameter("pivotSet", pivotSet);
    }
}
