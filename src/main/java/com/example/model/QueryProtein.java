package com.example.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "queryProtein")
public class QueryProtein {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queryProteinId")
    private Integer queryProteinId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proteinChainId", nullable = false)
    private ProteinChain proteinChain;

    public QueryProtein() {
    }

    public QueryProtein(ProteinChain proteinChain) {
        this.proteinChain = proteinChain;
    }

    // Getters and Setters
    public Integer getQueryProteinId() {
        return queryProteinId;
    }

    public ProteinChain getProteinChain() {
        return proteinChain;
    }
}
