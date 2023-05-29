package com.example.services.storage;

import com.example.model.Pivot512;
import com.example.model.PivotPairsFor64pSketches;
import com.example.model.ProteinChain;
import com.example.model.SimpleProtein;
import com.example.service.PivotPairsFor64pSketchesService;
import com.example.service.PivotSetService;
import org.hibernate.Session;
import vm.metricSpace.AbstractMetricSpace;
import vm.objTransforms.storeLearned.GHPSketchingPivotPairsStoreInterface;

import java.util.List;

public class GHPSketchesPivotPairsStorageDBImpl implements GHPSketchingPivotPairsStoreInterface {

    private final Session session; //todo dont use session directly use the Service
    private final PivotPairsFor64pSketchesService pivotPairsFor64pSketchesService;
    private final PivotSetService pivotSetService;


    public GHPSketchesPivotPairsStorageDBImpl(Session session, PivotPairsFor64pSketchesService pivotPairsFor64pSketchesService, PivotSetService pivotSetService) {

        this.session = session;
        this.pivotPairsFor64pSketchesService = pivotPairsFor64pSketchesService;
        this.pivotSetService = pivotSetService;
    }

    @Override
    public void storeSketching(String resultName, AbstractMetricSpace<Object> metricSpace, List<Object> pivots, Object... additionalInfoToStoreWithLearningSketching) {
        // sketchbitorder1-pivot1, sketchbitorder1-pivot2, sketchbitorder2-pivot1, sketchbitorder2pivot2
        System.out.println("Saving " + pivots.size() + " pivots");
        var currentPivotSet = pivotSetService.GetCurrentPivotSet();
        //todo as the simpleproteins already got all the info needed we can skip this step
        var pivotList = pivots.stream().map(o -> {
            var sp = (SimpleProtein) o;
            // the objects coming here are pivots, stripped of the pivotset information.
            // we are assuming the pivotset was not changed during runtime, it's the best we can do
            ProteinChain proteinChain = this.session.get(ProteinChain.class, sp.getIntId());
            var pivotId = new Pivot512.Pivot512Id();
            pivotId.setPivotSet(currentPivotSet);
            pivotId.setProteinChain(proteinChain);
            var pivot = session.get(Pivot512.class, pivotId);
            return pivot;
        }).toList();
        pivotPairsFor64pSketchesService.storePairs(pivotList);
        System.out.println("Saved");
    }

    // returns list of pivots that are used for sketching
    //todo add test that tests that this returns the same as above stores
    @Override
    public List<String[]> loadPivotPairsIDs(String sketchesName) {
        //todo what about 512? - cez sketchesname
        session.beginTransaction();
        var list = session.createQuery("from PivotPairsFor64pSketches", PivotPairsFor64pSketches.class).list();
        session.getTransaction().commit();
        return list.stream().map(p -> new String[]{String.valueOf(p.getId().getPivot1Id()), String.valueOf(p.getId().getPivot2Id())}).toList();
    }
}
