package com.example.services.distance;

import com.example.services.configuration.AppConfig;
import vm.metricSpace.distance.DistanceFunctionInterface;

public class DistanceFunctionInterfaceImpl<T> implements DistanceFunctionInterface<String> {

    public DistanceFunctionInterfaceImpl() {
        ProteinNativeQScoreDistance.initDistance(AppConfig.PDBE_BINARY_FILES_DIR);
    }

    @Override
    public float getDistance(String obj1, String obj2) {
        // todo this warning was placed here because we do not want to ever compute the distance on demand
        // due to how this code is set up it's unclear where is it called from
//        System.out.println("ALERT: distance not found in cache");
        var ret = ProteinNativeQScoreDistance.getStatsFloats(obj1, obj2, AppConfig.GESAMT_COMPUTATION_CUTOFF_THRESHOLD);
        return 1-ret[0];
    }
}
