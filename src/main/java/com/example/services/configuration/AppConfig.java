package com.example.services.configuration;

import org.hibernate.boot.cfgxml.internal.ConfigLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    //todo maybe cli arg?
    private static final String PROPERTIES_FILE = "run.properties";
    private static final Properties properties = new Properties();

    static {
        try {
            // Try to load properties file from the same location as the JAR file
            File jarFile = new File(ConfigLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File propertiesFile = new File(jarFile.getParentFile(), PROPERTIES_FILE);

            if (propertiesFile.exists()) {
                InputStream inputStream = new FileInputStream(propertiesFile);
                properties.load(inputStream);
                inputStream.close();
            } else {
                // Fallback to loading properties file from the resources folder
                InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
                properties.load(inputStream);
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** COMMON **/
    public static boolean DRY_RUN;
    public static final String PDBE_BINARY_FILES_DIR = properties.getProperty("PDBE_BINARY_FILES_DIR");

    /** DISTANCE **/
    public static final float GESAMT_COMPUTATION_CUTOFF_THRESHOLD = Float.parseFloat(properties.getProperty("GESAMT_COMPUTATION_CUTOFF_THRESHOLD"));
    public static final int CONSUMER_BUFFER_SIZE = Integer.parseInt(properties.getProperty("CONSUMER_BUFFER_SIZE"));
    /*
    Following two variables are used to limit protein chain distance computation to limited set of ids.
    This allows running this program on multiple PCs, connected to same database, better parallelling the workload.
    Setting either to -1 disables the option.
    //todo move this to readme
     */
    public static final int COMPUTE_CHAIN_FROM = Integer.parseInt(properties.getProperty("COMPUTE_CHAIN_FROM"));
    public static final int COMPUTE_CHAIN_TO = Integer.parseInt(properties.getProperty("COMPUTE_CHAIN_TO"));

    /** DATABASE **/
    public static final String HIBERNATE_CONNECTION_URL = properties.getProperty("hibernate.connection.url");
    public static final String HIBERNATE_CONNECTION_USERNAME = properties.getProperty("hibernate.connection.username");
    public static final String HIBERNATE_CONNECTION_PASSWORD = properties.getProperty("hibernate.connection.password");
    public static final String FLYWAY_CONNECTION_URL = properties.getProperty("flyway.connection.url");
    public static final String FLYWAY_CONNECTION_SCHEMA = properties.getProperty("flyway.connection.schema");
    public static final String FLYWAY_CONNECTION_USERNAME = properties.getProperty("flyway.connection.username");
    public static final String FLYWAY_CONNECTION_PASSWORD = properties.getProperty("flyway.connection.password");

    /** DATASET UPDATE **/
    public static final String DATASET_REMOTE_URL = properties.getProperty("dataset.remote_url");
    public static final String DATASET_MIRROR_DIR = properties.getProperty("dataset.mirror_dir");
    public static final String DATASET_RAW_DIR = properties.getProperty("dataset.raw_dir");
    public static final String DATASET_BINARY_DIR = properties.getProperty("dataset.binary_dir");
    public static final String DATASET_UPDATE_SCRIPT_PATH = properties.getProperty("dataset.update_script_path");

    /** SUB CONFIGS **/
    public static final String SUBCONFIGS_PYTHON_INI_CONFIG_PATH = properties.getProperty("subconfigs.python_ini_config_path");

    /** SKETCH LEARNING **/
    //todo loose the learning part or make it obvious it applies to applying sketches as well
    // Use freely to find suitable correlations, Mic used 100 000.
    public static final int SKETCH_LEARNING_SAMPLE_SIZE = Integer.parseInt(properties.getProperty("SKETCH_LEARNING_SAMPLE_SIZE"));
    // Number of bits used for sketch. Mic used 64 and 1024. Determines how long the operations with sketches will take.
    public static final int SKETCH_LEARNING_SKETCH_LENGTH = Integer.parseInt(properties.getProperty("SKETCH_LEARNING_SKETCH_LENGTH"));
    // Number of pivots to find suitable pairs
    public static final int SKETCH_LEARNING_PIVOTS_COUNT = Integer.parseInt(properties.getProperty("SKETCH_LEARNING_PIVOTS_COUNT"));
    // What should be the ratio of 1s to 0s in one column of the matrix? Mic used 0.5 - meaning same count of 1s and 0s
    public static final float SKETCH_LEARNING_BALANCE = Float.parseFloat(properties.getProperty("SKETCH_LEARNING_BALANCE"));

    /** SECONDARY FILTERING **/
    public static final String SECONDARY_FILTERING_SKETCHES_DIR = properties.getProperty("SECONDARY_FILTERING_SKETCHES_DIR");
}
