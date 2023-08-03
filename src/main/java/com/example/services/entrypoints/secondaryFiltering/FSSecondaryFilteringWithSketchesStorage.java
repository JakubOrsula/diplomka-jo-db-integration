package com.example.services.entrypoints.secondaryFiltering;

import com.example.services.configuration.AppConfig;
import vm.datatools.Tools;
import vm.metricSpace.distance.bounding.nopivot.learning.LearningSecondaryFilteringWithSketches;
import vm.metricSpace.distance.bounding.nopivot.storeLearned.SecondaryFilteringWithSketchesStoreInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FSSecondaryFilteringWithSketchesStorage implements SecondaryFilteringWithSketchesStoreInterface {

    @Override
    public void store(Map<Double, Integer> learntMapping, float thresholdPcum, String fullDatasetName, String sketchesDatasetName, int iDimSketchesSampleCount, int iDimDistComps, float distIntervalForPX) {
        File resultFile = getFileForMapping(sketchesDatasetName, distIntervalForPX, thresholdPcum, true);
        try {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile, false))) {
                for (Map.Entry<Double, Integer> entry : learntMapping.entrySet()) {
                    String fullSpaceDist = entry.getKey().toString();
                    String hammingDist = entry.getValue().toString();
                    bw.write(fullSpaceDist + ";" + hammingDist + "\n");
                }
                bw.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(FSSecondaryFilteringWithSketchesStorage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public SortedMap<Double, Integer> loadMapping(float thresholdPcum, String fullDatasetName, String sketchesDatasetName, int iDimSketchesSampleCount, int iDimDistComps, float distIntervalForPX) {
        File file = getFileForMapping(sketchesDatasetName, distIntervalForPX, thresholdPcum, false);
        List<String>[] parseCsv = Tools.parseCsv(file.getAbsolutePath(), 2, true);
        SortedMap<Double, Integer> ret = new TreeMap<>();
        List<String> keys = parseCsv[0];
        List<String> values = parseCsv[1];
        for (int i = 0; i < keys.size(); i++) {
            Double key = Double.valueOf(keys.get(i));
            Integer value = Integer.valueOf(values.get(i));
            ret.put(key, value);
        }
        return ret;
    }

    public File getFileForMapping(String sketchesDatasetName, float distIntervalForPX, float pCum, boolean willBeDeleted) {
        File file = new File(AppConfig.SECONDARY_FILTERING_SKETCHES_DIR, sketchesDatasetName + "_" + distIntervalForPX + "distInterval_"
                + LearningSecondaryFilteringWithSketches.DISTS_COMPS_FOR_SK_IDIM_AND_PX + "distsForIDIM_PX_" + pCum + "pCum.csv"
        );
        if (file.exists() && willBeDeleted) {
            file.delete();
        }
        return file;
    }
}
