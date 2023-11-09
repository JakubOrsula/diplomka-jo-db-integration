package com.example.services.utils;

import com.example.utils.UnrecoverableError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class SystemUtils {
    public static void exec(String[] scriptPath) {
        ProcessBuilder pb = new ProcessBuilder(scriptPath);
        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(scriptPath[0] + ": " + line);
            }

            p.waitFor();

            int exitValue = p.exitValue();
            if (exitValue == 0) {
                System.out.println(scriptPath[0] + ": " + "Execution of " + scriptPath + " successful");
            } else {
                System.out.println(scriptPath[0] + ": " + "Execution of " + scriptPath + " failed");
            }

            in.close();

        } catch (IOException e) {
            throw new UnrecoverableError(scriptPath[0] + ": " + "Failed to execute " + Arrays.toString(scriptPath), e);
        } catch (InterruptedException e) {
            throw new UnrecoverableError(scriptPath[0] + ": " + "Script execution interrupted " + Arrays.toString(scriptPath), e);
        }
    }
}
