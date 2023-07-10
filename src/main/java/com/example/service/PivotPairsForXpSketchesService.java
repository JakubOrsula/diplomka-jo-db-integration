package com.example.service;

import com.example.dao.PivotPairsForXpSketchesDao;
import com.example.model.Pivot512;
import com.example.model.PivotPairsFor64pSketches;
import com.example.model.PivotSet;
import com.example.services.configuration.AppConfig;

import java.util.List;

public class PivotPairsForXpSketchesService {
    private final PivotSetService pivotSetService;
    private final PivotPairsForXpSketchesDao pivotPairsForXpSketchesDao;

    public PivotPairsForXpSketchesService(PivotSetService pivotSetService, PivotPairsForXpSketchesDao pivotPairsForXpSketchesDao) {
        this.pivotSetService = pivotSetService;
        this.pivotPairsForXpSketchesDao = pivotPairsForXpSketchesDao;
    }

    private String tableNameBasedOnPivotCount() {
        var pivotCount = AppConfig.SKETCH_LEARNING_PIVOTS_COUNT;
        return switch (pivotCount) {
            case 64 -> "pivotPairsFor64pSketches";
            case 512 -> "pivotPairsFor512pSketches";
            default -> throw new Error("unknown pivot count");
        };
    }

    private long pairsCount() {
        PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
        return pivotPairsForXpSketchesDao.pairsCount(pivotSet, tableNameBasedOnPivotCount());
    }

    public void storePairs(List<Pivot512> pivots) {
        if (pairsCount() > 0) {
            throw new IllegalArgumentException("Pivots for current pivotset are already stored");
        }
        PivotSet pivotSet =  pivotSetService.GetCurrentPivotSet();
        pivotPairsForXpSketchesDao.storePairs(pivotSet, pivots, tableNameBasedOnPivotCount());
    }

    public List<String[]> loadPivotPairsIDs() {
        if (pairsCount() == 0) {
            throw new IllegalArgumentException("No sketches for current pivot set.");
        }
        PivotSet pivotSet =  pivotSetService.GetCurrentPivotSet();
        return pivotPairsForXpSketchesDao.loadPairs(pivotSet, tableNameBasedOnPivotCount());
    }
}
