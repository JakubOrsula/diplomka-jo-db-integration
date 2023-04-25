package com.example.model;
import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "proteinChain")
public class ProteinChain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "intId")
    private Integer intId;

    @Column(name = "gesamtId", nullable = false)
    private String gesamtId;

    @Column(name = "chainLength", nullable = false)
    private Integer chainLength;

    @Column(name = "indexedAsDataObject", nullable = false)
    private Boolean indexedAsDataObject;

    @Column(name = "added", nullable = false)
    private Timestamp added;

    // constructors, getters and setters

    public ProteinChain() {
    }

    public ProteinChain(String gesamtId, Integer chainLength, Boolean indexedAsDataObject) {
        this.gesamtId = gesamtId;
        this.chainLength = chainLength;
        this.indexedAsDataObject = indexedAsDataObject;
    }

    public Integer getIntId() {
        return intId;
    }

    public void setIntId(Integer intId) {
        this.intId = intId;
    }

    public String getGesamtId() {
        return gesamtId;
    }

    public void setGesamtId(String gesamtId) {
        this.gesamtId = gesamtId;
    }

    public Integer getChainLength() {
        return chainLength;
    }

    public void setChainLength(Integer chainLength) {
        this.chainLength = chainLength;
    }

    public Boolean getIndexedAsDataObject() {
        return indexedAsDataObject;
    }

    public void setIndexedAsDataObject(Boolean indexedAsDataObject) {
        this.indexedAsDataObject = indexedAsDataObject;
    }

    public Timestamp getAdded() {
        return added;
    }

    public void setAdded(Timestamp added) {
        this.added = added;
    }
}