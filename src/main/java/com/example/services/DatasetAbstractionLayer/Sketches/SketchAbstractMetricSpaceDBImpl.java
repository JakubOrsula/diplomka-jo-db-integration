package com.example.services.DatasetAbstractionLayer.Sketches;

import com.example.model.SimpleProtein;
import com.example.model.SimpleSketch;
import vm.metricSpace.AbstractMetricSpace;
import vm.metricSpace.distance.DistanceFunctionInterface;

public class SketchAbstractMetricSpaceDBImpl extends AbstractMetricSpace<long[]> {
    private final DistanceFunctionInterface<long[]> distanceFunctionInstance;

    public SketchAbstractMetricSpaceDBImpl(DistanceFunctionInterface<long[]> distanceFunctionInstance) {
        this.distanceFunctionInstance = distanceFunctionInstance;
    }

    public DistanceFunctionInterface<long[]> getDistanceFunctionForDataset(String datasetName, Object... params) {
        return distanceFunctionInstance;
    }

    public Object getIDOfMetricObject(Object o) {
        SimpleProtein sketch = (SimpleProtein) o;
        //todo due to incredible design of this whole thing it is implied that this is string
        // half of this codebase does not work if it is not string
        // yet it is not string but object
        // i have realized this after i put bunch of .toStrings everywhere
        return String.valueOf(sketch.getIntId());
    }

    public long[] getDataOfMetricObject(Object o) {
        SimpleProtein proteinChain = (SimpleProtein) o;
        return proteinChain.getBigSketch();
    }

    @Override
    public Object createMetricObject(Object id, long[] data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
