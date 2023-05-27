package com.example.service;

import com.example.dao.PivotPairsFor64pSketchesDao;
import com.example.model.Pivot512;
import com.example.model.PivotPairsFor64pSketches;
import com.example.model.PivotSet;

import java.util.List;

public class PivotPairsFor64pSketchesService {
    private final PivotSetService pivotSetService;
    private final PivotPairsFor64pSketchesDao pivotPairsFor64pSketchesDao;

    public PivotPairsFor64pSketchesService(PivotSetService pivotSetService, PivotPairsFor64pSketchesDao pivotPairsFor64pSketchesDao) {
        this.pivotSetService = pivotSetService;
        this.pivotPairsFor64pSketchesDao = pivotPairsFor64pSketchesDao;
    }

    public long pairsCount() {
        PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
        return pivotPairsFor64pSketchesDao.pairsCount(pivotSet);
    }

    public void storePairs(List<Pivot512> pivots) {
        if (pairsCount() > 0) {
            throw new IllegalArgumentException("Pivots for current pivotset are already stored");
        }
        PivotSet pivotSet =  pivotSetService.GetCurrentPivotSet();
        pivotPairsFor64pSketchesDao.storePairs(pivotSet, pivots);
    }
}
