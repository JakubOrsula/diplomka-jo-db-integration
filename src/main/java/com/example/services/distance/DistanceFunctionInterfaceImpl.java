package com.example.services.distance;

import com.example.services.configuration.AppConfig;
import vm.metricSpace.distance.DistanceFunctionInterface;

public class DistanceFunctionInterfaceImpl<T> extends DistanceFunctionInterface<String> {

    public DistanceFunctionInterfaceImpl() {
        ProteinNativeQScoreDistance.initDistance(AppConfig.DATASET_BINARY_DIR);
    }

    @Override
    public float getDistance(String obj1, String obj2) {
        var ret = ProteinNativeQScoreDistance.getStatsFloats(obj1, obj2, AppConfig.GESAMT_COMPUTATION_CUTOFF_THRESHOLD);
        return 1-ret[0];
    }
}
