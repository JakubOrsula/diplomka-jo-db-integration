package com.example.service.distance;

import com.example.dao.ProteinChainDao;
import com.example.model.PivotSet;
import com.example.model.SimpleProtein;
import com.example.service.PivotSetService;

import java.util.Iterator;

public class ProteinChainService {
    private final PivotSetService pivotSetService;
    private final ProteinChainDao proteinChainDao;

    public ProteinChainService(PivotSetService pivotSetService, ProteinChainDao proteinChainDao) {
        this.pivotSetService = pivotSetService;
        this.proteinChainDao = proteinChainDao;
    }

    public Long getChainsCount() {
        var pivotSet = pivotSetService.GetCurrentPivotSet();
        return (Long) proteinChainDao.getChainsCount(pivotSet);
    }

    public Iterator<SimpleProtein> getProteinChainIterator() {
        var pivotSet = pivotSetService.GetCurrentPivotSet();
        return proteinChainDao.getProteinChainIterator(pivotSet);
    }

    public Iterator<SimpleProtein> getProteinChainIterator(int limit) {
        var pivotSet = pivotSetService.GetCurrentPivotSet();
        return proteinChainDao.getProteinChainIterator(pivotSet, limit);
    }


}
