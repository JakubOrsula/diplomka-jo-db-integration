package com.example.services.entrypoints.selfChecks;

import com.example.services.configuration.AppConfig;

public class InstallationIntegrityCheck {

    private static boolean checkDatasetDirectoriesSetup() {
        var checkParents = new String[]{
                AppConfig.DATASET_MIRROR_DIR,
                AppConfig.DATASET_RAW_DIR,
                AppConfig.DATASET_BINARY_DIR,
        };
        for (var checkParent: checkParents) {
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
                System.out.println("Parent folders for datasets must be set up before running this program");
                System.out.println("Hint: you need hundreds of GBs of free space for these dirs");
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

    private static boolean checkDependenciesPresence() {
        var base_path = AppConfig.WORKING_DIRECTORY + "/" + "dependencies";
        var paths = new String[]{
                base_path,
                base_path + "/" + "gesamt_distance",
                base_path + "/" + "tbb",
                base_path + "/" + "mics-proteins",
                base_path + "/" + "mics-proteins" + "/" + "ppp_codes",
                base_path + "/" + "mics-proteins" + "/" + "sequential_sketches",
                base_path + "/" + "mics-proteins" + "/" + "sequential_sketches" + "/" + "algs",
                base_path + "/" + "mics-proteins" + "/" + "sequential_sketches" + "/" + "jars",
                base_path + "/" + "mics-proteins" + "/" + "sequential_sketches" + "/" + "Threshold_tables",
                base_path + "/" + "mics-proteins" + "/" + "sequential_sketches" + "/" + "http-api.cf",
                base_path + "/" + "mics-proteins" + "/" + "sequential_sketches" + "/" + "http_64pivots.defaults",
                base_path + "/" + "mics-proteins" + "/" + "sequential_sketches" + "/" + "http_64pivots.sh",
                base_path + "/" + "mics-proteins" + "/" + "sequential_sketches" + "/" + "http_512pivots.defaults",
                base_path + "/" + "mics-proteins" + "/" + "sequential_sketches" + "/" + "http_512pivots.sh",
                base_path + "/" + "mics-proteins" + "/" + "sequential_sketches" + "/" + "rebuildPPPCodes.sh",
                base_path + "/" + "mics-proteins" + "/" + "ppp_codes" + "/" + "algs",
                base_path + "/" + "mics-proteins" + "/" + "ppp_codes" + "/" + "jars",
                base_path + "/" + "mics-proteins" + "/" + "ppp_codes" + "/" + "http.defaults",
                base_path + "/" + "mics-proteins" + "/" + "ppp_codes" + "/" + "http.sh",
                base_path + "/" + "mics-proteins" + "/" + "ppp_codes" + "/" + "http-api.cf",
                base_path + "/" + "mics-proteins" + "/" + "ppp_codes" + "/" + "manager-pppcodes.cf",
                base_path + "/" + "mics-proteins" + "/" + "ppp_codes" + "/" + "mindex.cf",
                base_path + "/" + "mics-proteins" + "/" + "ppp_codes" + "/" + "pppcodes.cf",
                base_path + "/" + "mics-proteins" + "/" + "ppp_codes" + "/" + "pppcodes.defaults",
                base_path + "/" + "mics-proteins" + "/" + "ppp_codes" + "/" + "pppcodes.sh",
                base_path + "/" + "ProteinSearch",
                base_path + "/" + "ProteinSearch" + "/" + "venv" + "/" + "bin" + "/" + "activate",
        };

        for (var path: paths) {
            if (!new java.io.File(path).exists()) {
                System.out.println("Installation integrity check failed: " + path + " does not exist");
                System.out.println("Dependencies folder must be set up. See readme for more info");
                return false;
            }
        }
        return true;
    }

    private static boolean checkCppDependenciesPresence() {
        var path = AppConfig.WORKING_DIRECTORY + "/dependencies/gesamt_distance/build/distance/libProteinDistance.so";
        if (!new java.io.File(path).exists()) {
            System.out.println("Installation integrity check failed: " + path + " does not exist");
            System.out.println("The gesamt library failed to build on jo-integration branch. See the install.sh script" +
                    " and try to install gesamt library manually");
            return false;
        }

        path = "/usr/local/lib/libProteinDistance.so";
        if (!new java.io.File(path).exists()) {
            System.out.println("Installation integrity check failed: " + path + " does not exist");
            System.out.println("The gesamt library failed to build on jo-integration branch. See the install.sh script" +
                    " and try to install gesamt library manually");
            return false;
        }
        return true;
    }


    public static boolean run() {
        if (checkDatasetDirectoriesSetup() && checkAvailableCPUs() && checkDependenciesPresence() && checkCppDependenciesPresence()) {
            System.out.println("Installation integrity check passed");
            return true;
        }
        System.out.println("Installation integrity check failed");
        return false;
    }
}
