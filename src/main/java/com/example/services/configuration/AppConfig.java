package com.example.services.configuration;

import com.example.utils.UnrecoverableError;
import org.hibernate.boot.cfgxml.internal.ConfigLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

public class AppConfig {
    //todo maybe cli arg?
    public static final String WORKING_DIRECTORY;
    static {
        try {
            WORKING_DIRECTORY = new File(AppConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
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
            // qualify dependencies
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith("dependencies/")) {
                    properties.setProperty(key, WORKING_DIRECTORY + "/" + properties.getProperty(key));
                }
            }
        } catch (Exception e) {
            throw new UnrecoverableError("Failed to load run.properties file. Make sure properties file is next .jar you are trying to run", e);
        }
    }

    /** DAEMON **/
    public static final int DAEMON_UPDATE_TRIGGER_HOUR = Integer.parseInt(properties.getProperty("DAEMON_UPDATE_TRIGGER_HOUR"));
    public static boolean DRY_RUN;

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
    public static final String DATABASE_DRIVER = properties.getProperty("database.driver");
    public static final String DATABASE_ADDRESS = properties.getProperty("database.address");
    public static final String DATABASE_NAME = properties.getProperty("database.name");
    public static final String DATABASE_USERNAME = properties.getProperty("database.username");
    public static final String DATABASE_PASSWORD = properties.getProperty("database.password");
    public static final String FLYWAY_CONNECTION_USERNAME = properties.getProperty("flyway.connection.username");
    public static final String FLYWAY_CONNECTION_PASSWORD = properties.getProperty("flyway.connection.password");

    /** MESSIFF **/
    public static final String MESSIFF_SHORT_SKETCHES_PORT = properties.getProperty("messiff.short_sketches.port");
    public static final String MESSIFF_LONG_SKETCHES_PORT = properties.getProperty("messiff.long_sketches.port");
    public static final String MESSIFF_PPP_CODES_PORT = properties.getProperty("messiff.ppp_codes.port");
    public static final String MESSIFF_TMP_DIR = properties.getProperty("messiff.tmp_dir");
    public static final String MESSIFF_SHORT_SKETCHES_MANAGER_SCRIPT = properties.getProperty("messiff.short_sketches.manager_script");
    public static final String MESSIFF_LONG_SKETCHES_MANAGER_SCRIPT = properties.getProperty("messiff.long_sketches.manager_script");
    public static final String MESSIFF_PPP_CODES_MANAGER_SCRIPT = properties.getProperty("messiff.ppp_codes.manager_script");
    public static final String MESSIFF_SKETCHES_SHORT_CSV = properties.getProperty("messiff.sketches.short_csv");
    public static final String MESSIFF_SKETCHES_LONG_CSV = properties.getProperty("messiff.sketches.long_csv");
    public static final String MESSIFF_PPP_CODES_SHORT_CSV = properties.getProperty("messiff.ppp_codes.short_csv");
    public static final String MESSIFF_PPP_CODES_LONG_CSV = properties.getProperty("messiff.ppp_codes.long_csv");
    public static final String MESSIFF_PPP_CODES_BUILDER_SCRIPT = properties.getProperty("messiff.ppp_codes.builder_script");
    public static final String MESSIFF_SKETCHES_SHORT_BIN = properties.getProperty("messiff.sketches.short_bin");
    public static final String MESSIFF_SKETCHES_LONG_BIN = properties.getProperty("messiff.sketches.long_bin");
    public static final String MESSIFF_PPP_CODES_SHORT_BIN = properties.getProperty("messiff.ppp_codes.short_bin");
    public static final String MESSIFF_PPP_CODES_LONG_BIN = properties.getProperty("messiff.ppp_codes.long_bin");

    /** FLASK APP **/
    public static final String FLASK_LOCATION = properties.getProperty("flask.location");
    public static final String PROTEINS_JAR_LOCATION = properties.getProperty("proteins.jar_location");

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
    // Unfortunately they have to be mutable to compute two different sketch lengths in one run. VMMetricSpaceTechnique needs a singleton to hold configuration :(
    public static int SKETCH_LEARNING_SKETCH_LENGTH = Integer.parseInt(properties.getProperty("SKETCH_LEARNING_SKETCH_LENGTH"));
    // Number of pivots to find suitable pairs
    public static int SKETCH_LEARNING_PIVOTS_COUNT = Integer.parseInt(properties.getProperty("SKETCH_LEARNING_PIVOTS_COUNT"));
    // What should be the ratio of 1s to 0s in one column of the matrix? Mic used 0.5 - meaning same count of 1s and 0s
    public static final float SKETCH_LEARNING_BALANCE = Float.parseFloat(properties.getProperty("SKETCH_LEARNING_BALANCE"));

    /** SECONDARY FILTERING **/
    public static final String SECONDARY_FILTERING_SKETCHES_DIR = properties.getProperty("SECONDARY_FILTERING_SKETCHES_DIR");
}
