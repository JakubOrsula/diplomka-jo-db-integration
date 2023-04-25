package com.example.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pivot512")
public class Pivot512 {

    @EmbeddedId
    private Pivot512Id id;

    public Pivot512Id getId() {
        return id;
    }

    public void setId(Pivot512Id id) {
        this.id = id;
    }

    @Embeddable
    public static class Pivot512Id implements Serializable {

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "chainIntId")
        private ProteinChain proteinChain;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "pivotSetId")
        private PivotSet pivotSet;

        public ProteinChain getProteinChain() {
            return proteinChain;
        }

        public void setProteinChain(ProteinChain proteinChain) {
            this.proteinChain = proteinChain;
        }

        public PivotSet getPivotSet() {
            return pivotSet;
        }

        public void setPivotSet(PivotSet pivotSet) {
            this.pivotSet = pivotSet;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pivot512Id)) return false;
            Pivot512Id that = (Pivot512Id) o;
            return proteinChain.equals(that.proteinChain) && pivotSet.equals(that.pivotSet);
        }

        @Override
        public int hashCode() {
            return Objects.hash(proteinChain, pivotSet);
        }
    }
}