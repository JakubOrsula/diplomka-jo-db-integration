package com.example.dao;

import com.example.model.PivotSet;
import com.example.model.ProteinChain;
import com.example.model.SimpleProtein;
import com.example.services.configuration.AppConfig;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import java.util.Iterator;

public class ProteinChainForDistanceDao extends ProteinChainDao {

    public ProteinChainForDistanceDao(Session session) {
        super(session);
    }

    protected org.hibernate.query.Query getChainsCoreQuery(PivotSet pivotSet, String queryString) {
        // todo maybe sort to make it deterministic
        //todo this function needs to use indexedAsDataObject not to return pivots

        queryString += " from ProteinChain p left join ProteinChainMetadata pm on p.intId = pm.id.proteinChain.intId " +
                "where p.indexedAsDataObject = true " +
                "and (pm.id.proteinChain is null" +
                " or ((length(pm.pivotDistances) < 200 or p.added > pm.lastUpdate) and pm.id.pivotSet = :pivotSet) " +
                " or (pm.id.pivotSet != :pivotSet))";
        //todo move app config to parameters
        if (AppConfig.COMPUTE_CHAIN_FROM != -1 && AppConfig.COMPUTE_CHAIN_TO != -1) {
            queryString += " and p.intId >= :chainFrom and p.intId <= :chainTo ";
        } else { //order by size destroys concept of continuous intervals by id - JO says thats wrong, only the intervals will be ordered
            queryString += " order by p.chainLength desc"; //ordering from biggest chains gives better estimates on max running time
        }
        var query = session.createQuery(queryString)
                .setParameter("pivotSet", pivotSet);
        if (AppConfig.COMPUTE_CHAIN_FROM != -1 && AppConfig.COMPUTE_CHAIN_TO != -1) {
            query.setParameter("chainFrom", AppConfig.COMPUTE_CHAIN_FROM);
            query.setParameter("chainTo", AppConfig.COMPUTE_CHAIN_TO);
        }
        return query;
    }
}
