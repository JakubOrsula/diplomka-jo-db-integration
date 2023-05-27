package com.example.services.entrypoints;

import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.MetricSpacesStorageInterface;

public class DatasetImpl<T> extends Dataset<String> {
  // deterministicke samples -- sort

    public DatasetImpl(String dataSetName, AbstractMetricSpace<String> metricSpace, MetricSpacesStorageInterface metricSpacesStorage) {
        this.datasetName = "proteinChain";
        this.metricSpace = metricSpace;
        this.metricSpacesStorage = metricSpacesStorage;
    }
}
