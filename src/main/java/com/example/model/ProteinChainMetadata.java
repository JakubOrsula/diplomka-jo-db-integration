package com.example.model;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "proteinChainMetadata")
public class ProteinChainMetadata {

    @EmbeddedId
    private ProteinChainMetadataId id;

    @Column(name = "pivotDistances", columnDefinition = "longtext collate utf8mb4_bin", nullable = false)
    private String pivotDistances;

    @Column(name = "sketch512p", nullable = false, length = 400)
    private String sketch512p;

    @Column(name = "sketch64p", nullable = false, length = 96)
    private String sketch64p;

    @Column(name = "lastUpdate", nullable = false)
    private Timestamp lastUpdate;

    public ProteinChainMetadataId getId() {
        return id;
    }

    public void setId(ProteinChainMetadataId id) {
        this.id = id;
    }

    public String getPivotDistances() {
        return pivotDistances;
    }

    public void setPivotDistances(String pivotDistances) {
        this.pivotDistances = pivotDistances;
    }

    public String getSketch512p() {
        return sketch512p;
    }

    public void setSketch512p(String sketch512p) {
        this.sketch512p = sketch512p;
    }

    public String getSketch64p() {
        return sketch64p;
    }

    public void setSketch64p(String sketch64p) {
        this.sketch64p = sketch64p;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Embeddable
    public static class ProteinChainMetadataId implements Serializable {

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "pivotSetId")
        private PivotSet pivotSet;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "chainIntId")
        private ProteinChain proteinChain;

        public PivotSet getPivotSet() {
            return pivotSet;
        }

        public void setPivotSet(PivotSet pivotSet) {
            this.pivotSet = pivotSet;
        }

        public ProteinChain getProteinChain() {
            return proteinChain;
        }

        public void setProteinChain(ProteinChain proteinChain) {
            this.proteinChain = proteinChain;
        }

        // todo Override equals and hashCode methods
        // ...
    }
}

