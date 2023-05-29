package com.example.dao;

import com.example.model.Pivot512;
import com.example.model.PivotSet;
import com.example.model.SimpleProtein;
import com.example.services.utils.ListUtils;
import org.hibernate.Session;

import java.util.List;
import java.util.stream.Collectors;

public class PivotDao {
    private final Session session;

    public PivotDao(Session session) {
        this.session = session;
    }

    public List<Pivot512> getPivots(PivotSet pivotSet) {
        session.beginTransaction();
        //todo in addition to this db design being stupid and using compound keys, we need to sort because the lib is relying on this returning same results upon multiple calls
        List<Pivot512> pivotList = session.createQuery("select p from Pivot512 p where p.id.pivotSet.id = :pivotSetId order by p.id.proteinChain.intId", Pivot512.class)
                .setParameter("pivotSetId", pivotSet.getId())
                .list();
        session.getTransaction().commit();
        return pivotList;
    }
}
