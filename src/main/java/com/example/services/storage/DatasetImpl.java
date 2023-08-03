package com.example.services.storage;

import org.hibernate.cfg.NotYetImplementedException;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.Dataset;
import vm.metricSpace.AbstractMetricSpacesStorage;

import java.util.Map;

public class DatasetImpl<T> extends Dataset<T> {
  // deterministicke samples -- sort

    public DatasetImpl(String dataSetName, AbstractMetricSpace<T> metricSpace, AbstractMetricSpacesStorage metricSpacesStorage) {
        this.datasetName = dataSetName;
        this.metricSpace = metricSpace;
        this.metricSpacesStorage = metricSpacesStorage;
    }

    @Override
    public Map<Object, Object> getKeyValueStorage() {
        throw new NotYetImplementedException();
    }
}
