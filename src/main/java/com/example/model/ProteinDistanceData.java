package com.example.model;

import com.example.model.json.Converter;
import com.example.model.json.DistsMetadata;

import java.io.IOException;

//todo better name
public class ProteinDistanceData {
    public final int chainIntId;
    public final String gesamtId;
    private final String pivotDistances;
    public DistsMetadata metadata;

    public ProteinDistanceData(int chainIntId, String gesamtId, String pivotDistances) {
        this.chainIntId = chainIntId;
        this.gesamtId = gesamtId;
        this.pivotDistances = pivotDistances;
    }

    public void convertJson() {
        try {
            this.metadata = Converter.fromJsonString(this.pivotDistances);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
