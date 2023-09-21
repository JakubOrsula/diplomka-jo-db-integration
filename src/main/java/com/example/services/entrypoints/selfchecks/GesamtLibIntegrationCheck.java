package com.example.services.entrypoints.selfchecks;

import com.example.services.configuration.AppConfig;
import com.example.services.distance.ProteinNativeQScoreDistance;

public class GesamtLibIntegrationCheck {
    public static void checkLibraryExists() {
        ProteinNativeQScoreDistance.initDistance(AppConfig.PDBE_BINARY_FILES_DIR);
    }
}
