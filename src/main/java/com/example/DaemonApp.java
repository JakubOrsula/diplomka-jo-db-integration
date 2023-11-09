package com.example;

import com.example.services.configuration.AppConfig;
import com.example.services.entrypoints.selfchecks.InstallationIntegrityCheck;

import static com.example.services.utils.DatabaseUtils.buildSessionFactory;
import static com.example.services.utils.DatabaseUtils.migrate;

public class DaemonApp {
    public static void main(String[] args) {
        System.out.println("Daemon started");
        // todo maybe redirect sout to /var/log?

        AppConfig.DRY_RUN = false;


        migrate();
        var sessionFactory = buildSessionFactory();

        //installation integrity check
        InstallationIntegrityCheck.run();

        //start the messiffs


        //start flask

        //start the update loop

    }
}
