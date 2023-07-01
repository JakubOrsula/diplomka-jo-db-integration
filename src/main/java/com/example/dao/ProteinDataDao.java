package com.example.dao;

//todo better name
public class ProteinDataDao {
    public final int chainIntId;
    public final String gesamtId;
    public final String pivotDistances;

    public ProteinDataDao(int chainIntId, String gesamtId, String pivotDistances) {
        this.chainIntId = chainIntId;
        this.gesamtId = gesamtId;
        this.pivotDistances = pivotDistances;
    }
}
