package com.example.services.utils;

import java.util.Map;

public class MapUtils {
    private static final double DELTA = 0.0001;

    //todo add verbosity flag
    public static boolean compareMaps(Map<String, Double> map1, Map<String, Double> map2) {
        var are_same = true;
        if (map1.size() != map2.size()) {
            System.out.println("Maps have different lengths.");
            are_same = false;
        }

        for (Map.Entry<String, Double> entry : map1.entrySet()) {
            String key = entry.getKey();
            Double value1 = entry.getValue();
            Double value2 = map2.get(key);

            if (value2 == null) {
                System.out.println("Key '" + key + "' is present in the first map but not the second map.");
                are_same = false;
            } else if (Math.abs(value1 - value2) > DELTA) {
                are_same = false;
                System.out.println("Value mismatch for key '" + key + "'. First map value: " + value1 + ", second map value: " + value2);
            }
        }

        for (String key : map2.keySet()) {
            if (!map1.containsKey(key)) {
                System.out.println("Key '" + key + "' is present in the second map but not the first map.");
                are_same = false;
            }
        }

        return are_same;
    }
}
