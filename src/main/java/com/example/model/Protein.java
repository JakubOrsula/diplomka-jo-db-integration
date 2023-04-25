package com.example.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "protein")
public class Protein {

    @Id
    @Column(name = "pdbId", nullable = false)
    private String pdbId;

    @Column(name = "name", nullable = false)
    private String name;

    public Protein() {}

    public Protein(String pdbId, String name) {
        this.pdbId = pdbId;
        this.name = name;
    }

    public String getPdbId() {
        return pdbId;
    }

    public void setPdbId(String pdbId) {
        this.pdbId = pdbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Optional: Override toString() to print object details.
    @Override
    public String toString() {
        return "Protein{" +
                "pdbId='" + pdbId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
