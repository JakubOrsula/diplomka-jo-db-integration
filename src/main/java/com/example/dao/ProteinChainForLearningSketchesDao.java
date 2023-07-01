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
        // todo the requirement for distances to be up to date was dropped
        // if we really only want _valid_ distances we should first run the consistency check
        queryString += " from ProteinChain p inner join ProteinChainMetadata pm on p.intId = pm.id.proteinChain.intId " +
                "where length(pm.pivotDistances) > 200 " +
                "and pm.id.pivotSet = :pivotSet " +
                "and p.indexedAsDataObject = true " +
                "order by p.intId";
        return session.createQuery(queryString)
                .setParameter("pivotSet", pivotSet);
    }
}
