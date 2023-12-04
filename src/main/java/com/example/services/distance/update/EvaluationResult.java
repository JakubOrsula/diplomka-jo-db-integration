package com.example.services.distance.update;

public class EvaluationResult {
    public final long startedAtMillis;
    public final long finishedAtMillis;
    public final boolean endMarker;
    public final int proteinChainIntId;
    public final String metadata;

    public EvaluationResult(long startedAtMillis, long finishedAtMillis, boolean endMarker, int proteinChainIntId, String metadata) {
        this.startedAtMillis = startedAtMillis;
        this.finishedAtMillis = finishedAtMillis;
        this.endMarker = endMarker;
        this.proteinChainIntId = proteinChainIntId;
        this.metadata = metadata;
    }

    public double getTimeTaken() {
        return (finishedAtMillis - startedAtMillis) / 1000.0;
    }
}