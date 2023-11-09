package com.example.services.entrypoints.updateDataset;


import static com.example.services.utils.SystemUtils.exec;

public class UpdateDataset {

    public static void run(String remotePath, String mirrorPath, String rawPath, String binaryPath, String updateScript, String pythonConfigPath) {
        exec(new String[]{"rsync", "-rlptv", "--delete", "--info=progress2", remotePath + "/", mirrorPath});
        exec(new String[]{
                updateScript,
                "--config", pythonConfigPath,
                "--mirror-directory", mirrorPath,
                "--raw-directory", rawPath,
                "--binary-directory", binaryPath,
                "--workers", String.valueOf(Runtime.getRuntime().availableProcessors() - 1),
        });
    }
}
