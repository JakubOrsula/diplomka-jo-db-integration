package com.example.model;

public enum ProteinChainMetadataColumns {
    SKETCH_512P("sketch512p"),
    SKETCH_64P("sketch64p");

    private String columnName;

    ProteinChainMetadataColumns(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}
