package com.example.service;

import com.example.dao.ProteinChainMetadataDao;
import com.example.model.*;
import com.example.services.configuration.AppConfig;
import com.example.services.entrypoints.secondaryFiltering.AllSketchesResult;

import java.util.Iterator;
import java.util.List;

public class ProteinChainMetadataService {
    private final ProteinChainMetadataDao proteinChainMetadataDao;
    private final PivotSetService pivotSetService;

    public ProteinChainMetadataService(ProteinChainMetadataDao proteinChainMetadataDao, PivotSetService pivotSetService) {
        this.proteinChainMetadataDao = proteinChainMetadataDao;
        this.pivotSetService = pivotSetService;
    }

    public Iterator<ProteinChainMetadata> getProteinChainMetadatas() {
        PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
        return proteinChainMetadataDao.getProteinChainMetadatasWithValidDistances(pivotSet);
    }

    public Iterator<String> getProteinChainDistances() {
        PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
        //todo rename jazykolam
        return proteinChainMetadataDao.getProteinDistancesWithValidDistances(pivotSet);
    }
    public Iterator<ProteinDistanceData> getProteinChainDistancesData() {
        PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
        //todo rename jazykolam
        return proteinChainMetadataDao.getProteinDataWithValidDistances(pivotSet);
    }

    private ProteinChainMetadataColumns columnBasedOnPivotCount() {
        int pivotCount = AppConfig.SKETCH_LEARNING_PIVOTS_COUNT;
        return switch (pivotCount) {
            case 64 -> ProteinChainMetadataColumns.SKETCH_64P;
            case 512 -> ProteinChainMetadataColumns.SKETCH_512P;
            default -> throw new Error("Unknown pivot count");
        };
    }

    public void transferSketches() {
        proteinChainMetadataDao.transferSketches(columnBasedOnPivotCount());
    }

    public void ensureEmptyTransferTable() {
        proteinChainMetadataDao.cleanTransferTable();
    }

    public void saveSketches(List<SketchData> sketchDataList) {
        PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
        sketchDataList.stream().parallel().forEach(SketchData::convertSketch);
        proteinChainMetadataDao.saveSketchesThroughTransferTable(sketchDataList, pivotSet.getId(), columnBasedOnPivotCount());
    }

    public long[] getSketch(int proteinChainId) {
        PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
        return proteinChainMetadataDao.getSketch(pivotSet.getId(), proteinChainId, ProteinChainMetadataColumns.SKETCH_512P);
    }

    public AllSketchesResult getAllSketches() {
        PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
        return proteinChainMetadataDao.getAllSketches(pivotSet.getId(), ProteinChainMetadataColumns.SKETCH_512P);
    }
}
