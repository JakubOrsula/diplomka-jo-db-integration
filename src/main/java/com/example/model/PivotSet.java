package com.example.model;

import java.sql.Timestamp;
import javax.persistence.*;

@Entity
@Table(name = "pivotSet")
public class PivotSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "currentlyUsed")
    private int currentlyUsed;

    @Column(name = "added")
    private Timestamp added;

    public PivotSet() {
    }

    public PivotSet(int currentlyUsed, Timestamp added) {
        this.currentlyUsed = currentlyUsed;
        this.added = added;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCurrentlyUsed(int currentlyUsed) {
        this.currentlyUsed = currentlyUsed;
    }

    public Timestamp getAdded() {
        return added;
    }

    public void setAdded(Timestamp added) {
        this.added = added;
    }
}