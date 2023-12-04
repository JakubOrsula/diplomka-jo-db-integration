package com.example.services.entrypoints.selfChecks;

import com.example.services.configuration.AppConfig;
import com.example.services.distance.ProteinNativeQScoreDistance;

public class GesamtLibIntegrationCheck {
    public static void checkLibraryExists() {
        ProteinNativeQScoreDistance.initDistance(AppConfig.DATASET_BINARY_DIR);
    }
}
