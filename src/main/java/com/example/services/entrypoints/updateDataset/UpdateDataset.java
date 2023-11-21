package com.example.services.entrypoints.updateDataset;


import com.example.services.configuration.AppConfig;
import static com.example.services.utils.SystemUtils.exec;

public class UpdateDataset {

    public static void updateFiles() {
        exec(new String[]{"rsync", "-rlptv", "--delete", AppConfig.DATASET_REMOTE_URL + "/", AppConfig.DATASET_MIRROR_DIR});
        exec(new String[]{
                AppConfig.UPDATE_TOOL_RUNNER_SCRIPT,
                "python3",
                AppConfig.DATASET_UPDATE_SCRIPT_PATH,
                "--config", AppConfig.SUBCONFIGS_PYTHON_INI_CONFIG_PATH,
                "--mirror-directory", AppConfig.DATASET_MIRROR_DIR,
                "--raw-directory", AppConfig.DATASET_RAW_DIR,
                "--binary-directory", AppConfig.DATASET_BINARY_DIR,
                "--workers", String.valueOf(Runtime.getRuntime().availableProcessors() - 1),
        });
    }
}
