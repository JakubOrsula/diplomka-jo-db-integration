package com.example.service;

import com.example.dao.PivotDao;
import com.example.dao.PivotSetDao;
import com.example.model.Pivot512;
import com.example.model.SimpleProtein;
import com.example.services.utils.ListUtils;

import java.util.List;
import java.util.stream.Collectors;

public class PivotService {
    private final PivotDao pivotDao;
    private final PivotSetService pivotSetService;

    public PivotService(PivotDao pivotDao, PivotSetService pivotSetService) {
        this.pivotDao = pivotDao;
        this.pivotSetService = pivotSetService;
    }

    public List<SimpleProtein> GetPivotsAsSimpleProteins() {
        return GetPivotsAsSimpleProteins(-1);
    }

    public List<Pivot512> GetPivots() {
        return GetPivots(-1);
    }

    public List<SimpleProtein> GetPivotsAsSimpleProteins(int splitEveryNBasedOnLimit) {
        return GetPivots(splitEveryNBasedOnLimit).stream()
                .map(pivot -> new SimpleProtein(pivot.getId().getProteinChain().getIntId(), pivot.getId().getProteinChain().getGesamtId()))
                .collect(Collectors.toList());
    }

    public List<Pivot512> GetPivots(int splitEveryNBasedOnLimit) {
        //todo handle changing of current pivot set - redis lock?
        var currentPivotSet = pivotSetService.GetCurrentPivotSet();
        var pivotList = pivotDao.getPivots(currentPivotSet);


        if (splitEveryNBasedOnLimit != -1) {
            pivotList = ListUtils.getEveryNBasedOnLimit(pivotList, splitEveryNBasedOnLimit);
        }
        return pivotList;
    }
}
