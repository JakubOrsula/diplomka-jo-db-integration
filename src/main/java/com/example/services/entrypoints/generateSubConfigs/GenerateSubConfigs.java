package com.example.services.entrypoints.generateSubConfigs;

import com.example.services.configuration.AppConfig;
import com.example.utils.UnrecoverableError;
import org.ini4j.Wini;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class GenerateSubConfigs {
    public static void run(String iniConfigLocation) {
        System.out.println("GenerateSubConfigs.run");
        try{
            Files.write(Path.of(iniConfigLocation), new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Wini ini = new Wini(new File(iniConfigLocation));

            // DB block
            ini.put("db", "ip", AppConfig.DATABASE_ADDRESS);
            ini.put("db", "database", AppConfig.DATABASE_NAME);
            ini.put("db", "user", AppConfig.DATABASE_USERNAME);
            ini.put("db", "password", AppConfig.DATABASE_PASSWORD);

            // Ports block
            ini.put("ports", "sketches_small", AppConfig.MESSIFF_SHORT_SKETCHES_PORT);
            ini.put("ports", "sketches_large", AppConfig.MESSIFF_LONG_SKETCHES_PORT);
            ini.put("ports", "full", AppConfig.MESSIFF_PPP_CODES_PORT);

            // Dirs block
            ini.put("dirs", "computations", AppConfig.MESSIFF_TMP_DIR);

            System.out.println(ini);
            ini.store();
        } catch(Exception e) {
            throw new UnrecoverableError("Failed to create ini file " + iniConfigLocation, e);
        }
    }
}
