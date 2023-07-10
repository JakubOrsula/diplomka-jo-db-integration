package com.example.service;

import com.example.dao.ProteinChainMetadataDao;
import com.example.dao.ProteinDataDao;
import com.example.model.PivotSet;
import com.example.model.ProteinChainMetadata;
import com.example.model.ProteinChainMetadataColumns;
import com.example.services.configuration.AppConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    public Iterator<String> getProteinChainDistances() {
        PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
        //todo rename jazykolam
        return proteinChainMetadataDao.getProteinDistancesWithValidDistances(pivotSet);
    }
    public Iterator<ProteinDataDao> getProteinChainDistancesData() {
        PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
        //todo rename jazykolam
        return proteinChainMetadataDao.getProteinDataWithValidDistances(pivotSet);
    }

    private static String createJsonString(String key, long[] vals) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode arrayNode = mapper.createArrayNode();

        for (long val : vals) {
            arrayNode.add(val);
        }

        rootNode.set(key, arrayNode);

        return mapper.writeValueAsString(rootNode);
    }

    private ProteinChainMetadataColumns columnBasedOnPivotCount() {
        int pivotCount = AppConfig.SKETCH_LEARNING_PIVOTS_COUNT;
        return switch (pivotCount) {
            case 64 -> ProteinChainMetadataColumns.SKETCH_64P;
            case 512 -> ProteinChainMetadataColumns.SKETCH_512P;
            default -> throw new Error("Unknown pivot count");
        };
    }

    private String jsonKeyBasedOnSketchLength() {
        int sketchLength = AppConfig.SKETCH_LEARNING_SKETCH_LENGTH;
        return switch (sketchLength) {
            case 192 -> "sk192_long";
            case 1024 -> "sk1024_long";
            default -> throw new Error("Unknown sketch length");
        };
    }

    public void saveSketch(int chainId, long[] sketch) {
        PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
        try {
            proteinChainMetadataDao.saveSketch(pivotSet.getId(), chainId, columnBasedOnPivotCount(), createJsonString(jsonKeyBasedOnSketchLength(), sketch));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    }
