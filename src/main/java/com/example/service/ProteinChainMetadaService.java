package com.example.service;

import com.example.dao.ProteinChainMetadataDao;
import com.example.model.PivotSet;
import com.example.model.ProteinChainMetadata;

import java.util.Iterator;

public class ProteinChainMetadaService {
    private final ProteinChainMetadataDao proteinChainMetadataDao;
    private final PivotSetService pivotSetService;

    public ProteinChainMetadaService(ProteinChainMetadataDao proteinChainMetadataDao, PivotSetService pivotSetService) {
        this.proteinChainMetadataDao = proteinChainMetadataDao;
        this.pivotSetService = pivotSetService;
    }

    public Iterator<ProteinChainMetadata> getProteinChainMetadatas() {
        PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
        return proteinChainMetadataDao.getProteinChainMetadatasWithValidDistances(pivotSet);
    }

    }
