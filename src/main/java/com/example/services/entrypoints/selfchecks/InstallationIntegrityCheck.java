package com.example.services.entrypoints.selfchecks;

import com.example.services.configuration.AppConfig;

import static java.lang.Runtime.getRuntime;

public class InstallationIntegrityCheck {

    private static boolean checkParentDirectoriesSetup() {
        var checkParents = new String[]{
                AppConfig.DATASET_MIRROR_DIR,
                AppConfig.DATASET_RAW_DIR,
                AppConfig.DATASET_BINARY_DIR,
        };
        for (var checkParent :
                checkParents) {
            if (checkParent.endsWith("/")) {
                System.out.println("Installation integrity check failed: " + checkParent + " ends with /");
                System.out.println("For consistency, specify the paths without trailing slash (/)");
                return false;
            }
        }
        for (var checkParent :
                checkParents) {
            var checkParentFile = new java.io.File(checkParent);
            var parent = checkParentFile.getParentFile();
            if (!parent.exists()) {
                System.out.println("Installation integrity check failed: " + parent + " does not exist");
                System.out.println("Parent folders must be set up before running this program");
                return false;
            }
        }
        return true;
    }

    private static boolean checkAvailableCPUs() {
        var availableProcessors = Runtime.getRuntime().availableProcessors();
        if (availableProcessors < 2) {
            System.out.println("Installation integrity check failed: " + availableProcessors + " CPUs available");
            System.out.println("At least 2 CPUs are required to run this program");
            return false;
        }
        return true;
    }

    public static boolean run() {
        return checkParentDirectoriesSetup() && checkAvailableCPUs();
    }
}
