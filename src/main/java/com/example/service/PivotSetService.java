package com.example.service;

import com.example.dao.PivotSetDao;
import com.example.model.PivotSet;


public class PivotSetService {
    private final PivotSetDao pivotSetDao;

    public PivotSetService(PivotSetDao pivotSetDao) {
        this.pivotSetDao = pivotSetDao;
    }

    public PivotSet GetCurrentPivotSet() {
        return pivotSetDao.GetCurrentPivotSet();
    }
}
