package com.example.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "pivotPairsFor64pSketches")
public class PivotPairsFor64pSketches {

    @EmbeddedId
    private PivotPairsFor64pSketchesId id;

    public PivotPairsFor64pSketchesId getId() {
        return id;
    }

    public void setId(PivotPairsFor64pSketchesId id) {
        this.id = id;
    }

    @Embeddable
    public static class PivotPairsFor64pSketchesId implements Serializable {

        public int getPivotSetId() {
            return pivotSetId;
        }

        public void setPivotSetId(int pivotSetId) {
            this.pivotSetId = pivotSetId;
        }

        public int getPivot1Id() {
            return pivot1Id;
        }

        public void setPivot1Id(int pivot1Id) {
            this.pivot1Id = pivot1Id;
        }

        public void setPivot2Id(int pivot12Id) {
            this.pivot2Id = pivot2Id;
        }

        public int getPivot2Id() {
            return pivot2Id;
        }

        @Column(name = "pivotSetId", insertable = false, updatable = false)
        private int pivotSetId;

        @Column(name = "sketchBitOrder")
        private short sketchBitOrder;

        @Column(name = "pivot1")
        private int pivot1Id;

        @Column(name = "pivot2")
        private int pivot2Id;

        public short getSketchBitOrder() {
            return sketchBitOrder;
        }

        public void setSketchBitOrder(short sketchBitOrder) {
            this.sketchBitOrder = sketchBitOrder;
        }


    }
}
