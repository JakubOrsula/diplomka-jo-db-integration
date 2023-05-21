package com.example.services.storage;

import com.example.model.PivotPairsFor64pSketches;
import org.hibernate.Session;
import vm.metricSpace.AbstractMetricSpace;
import vm.objTransforms.storeLearned.GHPSketchingPivotPairsStoreInterface;

import java.util.List;

public class GHPSketchesPivotPairsStorageDBImpl implements GHPSketchingPivotPairsStoreInterface {


    private final Session session;

    public GHPSketchesPivotPairsStorageDBImpl(Session session) {

        this.session = session;
    }

    @Override
    public void storeSketching(String resultName, AbstractMetricSpace<Object> metricSpace, List<Object> pivots, Object... additionalInfoToStoreWithLearningSketching) {
        // jednorozmernej list pivotu (cely objekty asi getId.... vrati id)
        // list pivotu ide tak ze
        // sketchbitorder1-pivot1, sketchbitorder1-pivot2, sketchbitorder1-pivot1, sketchbitorder2pivot2

    }

    // returns list of pivots that are used for sketching
    @Override
    public List<String[]> loadPivotPairsIDs(String sketchesName) {
        //todo what about 512? - cez sketchesname
        session.beginTransaction();
        var list = session.createQuery("from PivotPairsFor64pSketches", PivotPairsFor64pSketches.class).list();
        session.getTransaction().commit();
        return list.stream().map(p -> new String[]{p.getId().getPivot1().toString(), p.getId().getPivot2().toString()}).toList();
    }
}
