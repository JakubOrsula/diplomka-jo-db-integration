package com.example.services.entrypoints.flaskAppController;

import com.example.utils.UnrecoverableError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FlaskAppController {

    private Process flaskProcess;
    private Thread flaskThread;

    public void startFlaskApp(String flaskAppPath) {
        System.out.println("""
                ############################################################
                # START FLASK START
                ############################################################
                """);
        flaskThread = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder();
                pb.directory(new java.io.File(flaskAppPath));
                pb.redirectErrorStream(true);
                pb.command("source", "venv/bin/activate", "&&", "flask", "run");
                flaskProcess = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(flaskProcess.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("flask (bg): " + line);
                }
            } catch (IOException e) {
                throw new UnrecoverableError("Failed to start the Flask app.", e);
            }
        });
        flaskThread.start();
        System.out.println("""
                ############################################################
                # START FLASK END
                ############################################################
                """);
    }

    public void stopFlaskApp() {
        System.out.println("""
                ############################################################
                # STOP FLASK START
                ############################################################
                """);
        if (flaskThread == null || flaskProcess == null) {
            return;
        }
        flaskProcess.destroy();
        try {
            flaskThread.join(3000);
        } catch (InterruptedException e) {
            System.out.println("Failed to shutdown flask gracefully");
        }
        System.out.println("""
                ############################################################
                # STOP FLASK END
                ############################################################
                """);
    }
}