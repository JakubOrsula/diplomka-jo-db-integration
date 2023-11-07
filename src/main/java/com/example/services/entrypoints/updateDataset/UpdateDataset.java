package com.example.services.entrypoints.updateDataset;

import com.example.utils.UnrecoverableError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class UpdateDataset {

    private static void exec(String[] scriptPath) {
        ProcessBuilder pb = new ProcessBuilder(scriptPath);
        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }

            p.waitFor();

            int exitValue = p.exitValue();
            if (exitValue == 0) {
                System.out.println("Execution of " + scriptPath + " successful");
            } else {
                System.out.println("Execution of " + scriptPath + " failed");
            }

            in.close();

        } catch (IOException e) {
            throw new UnrecoverableError("Failed to execute " + Arrays.toString(scriptPath), e);
        } catch (InterruptedException e) {
            throw new UnrecoverableError("Script execution interrupted " + Arrays.toString(scriptPath), e);
        }
    }

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
