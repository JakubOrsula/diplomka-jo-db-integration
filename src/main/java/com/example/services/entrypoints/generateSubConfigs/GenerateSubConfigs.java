package com.example.services.entrypoints.generateSubConfigs;

import com.example.services.configuration.AppConfig;
import com.example.utils.UnrecoverableError;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class GenerateSubConfigs {
    public static void adjustMessiffBinaryDirConfigs() {
        // Define file paths
        Path sketchConfigPath = Paths.get(AppConfig.MESSIFF_LONG_SKETCHES_DEFAULT_CONFIG);
        Path pppCodesConfigPath = Paths.get(AppConfig.MESSIFF_PPP_CODES_DEFAULT_CONFIG);

        // Update files
        adjustMessiffBinaryDirConfig(sketchConfigPath);
        adjustMessiffBinaryDirConfig(pppCodesConfigPath);
    }

    private static void adjustMessiffBinaryDirConfig(Path filePath) {
        try {
            String content = Files.readString(filePath);
            String updatedContent = content.replaceAll(
                    "GESAMTLIBPATH=\\$\\{GESAMTLIBPATH:-MISSING_VALUE\\}",
                    "GESAMTLIBPATH=${GESAMTLIBPATH:-" + AppConfig.DATASET_BINARY_DIR + "}"
            );
            Files.writeString(filePath, updatedContent);
            System.out.println("Updated " + filePath);
        } catch (IOException e) {
            throw new UnrecoverableError("Failed to set new binary config location file " + filePath, e);
        }
    }

    private static void createIniFile(String iniConfigLocation) {
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
            System.out.println(iniConfigLocation + " written");
        } catch(Exception e) {
            throw new UnrecoverableError("Failed to create ini file " + iniConfigLocation, e);
        }
    }

    public static void run(String iniConfigLocation) {
        System.out.println("GenerateSubConfigs.run");

        createIniFile(iniConfigLocation);

        System.out.println("GenerateSubConfigs.run adjustMessifConfigs");
        adjustMessiffBinaryDirConfigs();
        System.out.println("GenerateSubConfigs.run adjustMessifConfigs done");

        System.out.println("GenerateSubConfigs.run done");
    }
}
