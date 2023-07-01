package com.example.services.distance;

import com.example.model.SimpleProtein;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.distance.DistanceFunctionInterface;

public class AbstractMetricSpaceDBImpl extends AbstractMetricSpace<String> {
    private final DistanceFunctionInterface<String> distanceFunctionInstance;

    public AbstractMetricSpaceDBImpl(DistanceFunctionInterface<String> distanceFunctionInstance) {
        this.distanceFunctionInstance = distanceFunctionInstance;
    }

    public DistanceFunctionInterface<String> getDistanceFunctionForDataset(String datasetName, Object... params) {
        return distanceFunctionInstance;
    }

    public Object getIDOfMetricObject(Object o) {
        SimpleProtein proteinChain = (SimpleProtein) o;
        //todo due to incredible design of this whole thing it is implied that this is string
        // half of this codebase does not work if it is not string
        // yet it is not string but object
        // i have realized this after i put bunch of .toStrings everywhere
        return String.valueOf(proteinChain.getIntId());
    }

    public String getDataOfMetricObject(Object o) {
        SimpleProtein proteinChain = (SimpleProtein) o;
        return proteinChain.getGesamtId();
    }

    public Object createMetricObject(Object id, String data) {
        //todo maybe unused maybe implement
        return null;
    }
}
