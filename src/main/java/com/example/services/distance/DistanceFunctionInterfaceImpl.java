package com.example.services.distance;

import vm.metricSpace.distance.DistanceFunctionInterface;

public class DistanceFunctionInterfaceImpl<T> implements DistanceFunctionInterface<String> {

    public DistanceFunctionInterfaceImpl() {
        ProteinNativeQScoreDistance.initDistance("/home/jakub/Documents/pdbe_data/PDBe_binary"); //todo to conf file
    }

    @Override
    public float getDistance(String obj1, String obj2) {
        var ret = ProteinNativeQScoreDistance.getStatsFloats(obj1, obj2, 0.6F);  //todo to some conf file
        return ret[0];
    }
}
