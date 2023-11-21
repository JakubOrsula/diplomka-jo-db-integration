package com.example.services.entrypoints.updateDataset;


import static com.example.services.utils.SystemUtils.exec;

public class UpdateDataset {

    public static void updateFiles(String remotePath, String mirrorPath, String rawPath, String binaryPath, String updateScript, String pythonConfigPath) {
        exec(new String[]{"rsync", "-rlptv", "--delete", remotePath + "/", mirrorPath});
        exec(new String[]{
                ".",
                "venv/bin/activate",
                "&&",
                updateScript,
                "--config", pythonConfigPath,
                "--mirror-directory", mirrorPath,
                "--raw-directory", rawPath,
                "--binary-directory", binaryPath,
                "--workers", String.valueOf(Runtime.getRuntime().availableProcessors() - 1),
        });
    }

    public static void run(String remotePath, String mirrorPath, String rawPath, String binaryPath, String updateScript, String pythonConfigPath) {
        updateFiles(remotePath, mirrorPath, rawPath, binaryPath, updateScript, pythonConfigPath);
    }
}
