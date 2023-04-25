package com.example.services.distance;

import vm.metricSpace.distance.DistanceFunctionInterface;

public class DistanceFunctionInterfaceImpl<T> implements DistanceFunctionInterface<String> {
    @Override
    public float getDistance(String obj1, String obj2) {
        var ret = ProteinNativeQScoreDistance.getStatsFloats(obj1, obj2, 0.6F);  //todo to some conf file
        return ret[0];
    }
}
