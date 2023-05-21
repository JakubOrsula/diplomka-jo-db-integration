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

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "pivotSetId")
        private PivotSet pivotSet;

        @Column(name = "sketchBitOrder")
        private short sketchBitOrder;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "pivot1")
        private Pivot512 pivot1;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "pivot2")
        private Pivot512 pivot2;

        public PivotSet getPivotSet() {
            return pivotSet;
        }

        public void setPivotSet(PivotSet pivotSet) {
            this.pivotSet = pivotSet;
        }

        public short getSketchBitOrder() {
            return sketchBitOrder;
        }

        public void setSketchBitOrder(short sketchBitOrder) {
            this.sketchBitOrder = sketchBitOrder;
        }

        public Pivot512 getPivot1() {
            return pivot1;
        }

        public void setPivot1(Pivot512 pivot1) {
            this.pivot1 = pivot1;
        }

        public Pivot512 getPivot2() {
            return pivot2;
        }

        public void setPivot2(Pivot512 pivot2) {
            this.pivot2 = pivot2;
        }

        // todo Override equals and hashCode methods
        // ...
    }
}
