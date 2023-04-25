package com.example.services.distance;

import com.example.model.ProteinChain;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.distance.DistanceFunctionInterface;

public class AbstractMetricSpaceDBImpl extends AbstractMetricSpace<String> {
    public DistanceFunctionInterface<String> getDistanceFunctionForDataset(String datasetName, Object... params) {
        return null;
    }

    public Object getIDOfMetricObject(Object o) {
        ProteinChain proteinChain = (ProteinChain) o;
        return proteinChain.getIntId();
    }

    public String getDataOfMetricObject(Object o) {
        ProteinChain proteinChain = (ProteinChain) o;
        return proteinChain.getGesamtId();
    }

    public Object createMetricObject(Object id, String data) {
        //todo maybe unused maybe implement
        return null;
    }
}
