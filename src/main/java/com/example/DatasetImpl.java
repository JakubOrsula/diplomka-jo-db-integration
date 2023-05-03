package com.example;

import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.MetricSpacesStorageInterface;

public class DatasetImpl<T> extends Dataset<String> {
  // todo pridat konstruktor a deterministicke samples

    public DatasetImpl(String dataSetName, AbstractMetricSpace<String> metricSpace, MetricSpacesStorageInterface metricSpacesStorage) {
        this.datasetName = "proteinChain";
        this.metricSpace = metricSpace;
        this.metricSpacesStorage = metricSpacesStorage;
    }
}
