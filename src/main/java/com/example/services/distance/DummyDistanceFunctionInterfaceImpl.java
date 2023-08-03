package com.example.services.distance;

import vm.metricSpace.distance.DistanceFunctionInterface;


public class DummyDistanceFunctionInterfaceImpl<T> extends DistanceFunctionInterface<String> {

    public DummyDistanceFunctionInterfaceImpl(Object ...args) {

    }
    @Override
    public float getDistance(String pivotGesamt, String proteinGesamt) {
        return (float) Math.random();
    }
}
