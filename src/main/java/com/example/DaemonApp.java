package com.example;

import com.example.service.PivotPairsForXpSketchesService;
import com.example.services.configuration.AppConfig;
import com.example.services.entrypoints.applySketches.ApplySketches;
import com.example.services.entrypoints.distanceComputation.DistanceComputation;
import com.example.services.entrypoints.flaskAppController.FlaskAppController;
import com.example.services.entrypoints.generatePivotCsvs.GeneratePivotCsvs;
import com.example.services.entrypoints.generateSubConfigs.GenerateSubConfigs;
import com.example.services.entrypoints.learnSketches.LearnSketches;
import com.example.services.entrypoints.selfchecks.ConsistencyCheck;
import com.example.services.entrypoints.selfchecks.InstallationIntegrityCheck;
import com.example.services.entrypoints.updateDataset.UpdateDataset;
import com.example.services.utils.JavaUtils;
import com.example.services.utils.SystemUtils;
import com.example.services.utils.TimeUtils;
import com.example.utils.UnrecoverableError;
import org.hibernate.SessionFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static com.example.services.utils.DatabaseUtils.buildSessionFactory;
import static com.example.services.utils.DatabaseUtils.migrate;

public class DaemonApp {
    private final static FlaskAppController controller = new FlaskAppController();

    public static void bootstrap(SessionFactory sessionFactory) throws IOException, InterruptedException {
        System.out.println("""
                ############################################################
                # BOOTSTRAP START
                ############################################################
                """);
        //installation integrity check
        if (!InstallationIntegrityCheck.run()) {
            throw new UnrecoverableError("Installation integrity check failed");
        }

        //update subconfigs
        GenerateSubConfigs.run(AppConfig.SUBCONFIGS_PYTHON_INI_CONFIG_PATH);

        // update pivot csvs
        // new update is needed only when new pivotset is picked
        GeneratePivotCsvs.run(sessionFactory, AppConfig.MESSIFF_SKETCHES_SHORT_CSV, PivotPairsForXpSketchesService.tableNameBasedOnPivotCount(64));
        GeneratePivotCsvs.run(sessionFactory, AppConfig.MESSIFF_SKETCHES_LONG_CSV, PivotPairsForXpSketchesService.tableNameBasedOnPivotCount(512));
        Files.copy(new File(AppConfig.MESSIFF_SKETCHES_SHORT_CSV).toPath(), new File(AppConfig.MESSIFF_PPP_CODES_SHORT_CSV).toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(new File(AppConfig.MESSIFF_SKETCHES_LONG_CSV).toPath(), new File(AppConfig.MESSIFF_PPP_CODES_LONG_CSV).toPath(), StandardCopyOption.REPLACE_EXISTING);
        createMessiffDatasets();
        System.out.println("""
                ############################################################
                # BOOTSTRAP END
                ############################################################
                """);
    }

    public static void restartMessiffs() throws InterruptedException {
        System.out.println("""
                ############################################################
                # MESSIFF RESTART START
                ############################################################
                """);
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_PPP_CODES_MANAGER_SCRIPT, "stop"});
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_SHORT_SKETCHES_MANAGER_SCRIPT, "stop"});
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_LONG_SKETCHES_MANAGER_SCRIPT, "stop"});
        Thread.sleep(3*1000);
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_PPP_CODES_MANAGER_SCRIPT, "murder"});
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_SHORT_SKETCHES_MANAGER_SCRIPT, "murder"});
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_LONG_SKETCHES_MANAGER_SCRIPT, "murder"});
        Thread.sleep(3*1000);
        SystemUtils.deletePidsDir(AppConfig.MESSIFF_PPP_CODES_MANAGER_SCRIPT);
        SystemUtils.deletePidsDir(AppConfig.MESSIFF_SHORT_SKETCHES_MANAGER_SCRIPT);
        Thread.sleep(3*1000);
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_PPP_CODES_MANAGER_SCRIPT, "start"});
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_SHORT_SKETCHES_MANAGER_SCRIPT, "start"});
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_LONG_SKETCHES_MANAGER_SCRIPT, "start"});
        Thread.sleep(3*1000);
        System.out.println("""
                ############################################################
                # MESSIFF RESTART END
                ############################################################
                """);
    }

    public static void restartSolution(SessionFactory sessionFactory) throws InterruptedException {
        //stop flask
        controller.stopFlaskApp();

        //restart the messiffs
        restartMessiffs();

        //start flask
        SystemUtils.exec(new String[]{"killall", "flask"});
        controller.startFlaskApp(AppConfig.FLASK_LOCATION);
    }

    private static void updateDataset(SessionFactory sessionFactory) throws IOException, InterruptedException {
        System.out.println("Update dataset: Going to fetch remote files");
        UpdateDataset.updateFiles(AppConfig.DATASET_REMOTE_URL, AppConfig.DATASET_MIRROR_DIR, AppConfig.DATASET_RAW_DIR, AppConfig.DATASET_BINARY_DIR, AppConfig.DATASET_UPDATE_SCRIPT_PATH, AppConfig.SUBCONFIGS_PYTHON_INI_CONFIG_PATH);
        System.out.println("Update dataset: Remote file fetched, unzipped and inserted into database");

        System.out.println("Update dataset: Going to compute distances");
        ConsistencyCheck.CheckComputedDistances(sessionFactory);
        DistanceComputation.computeDistances(sessionFactory);
        System.out.println("Update dataset: Distances computed");

        System.out.println("Update dataset: Going to learn short sketches");
        AppConfig.SKETCH_LEARNING_PIVOTS_COUNT = 64;
        AppConfig.SKETCH_LEARNING_SKETCH_LENGTH = 192;
        LearnSketches.run(sessionFactory);
        System.out.println("Update dataset: Short sketches learned");

        System.out.println("Update dataset: Going to learn long sketches");
        AppConfig.SKETCH_LEARNING_PIVOTS_COUNT = 512;
        AppConfig.SKETCH_LEARNING_SKETCH_LENGTH = 1024;
        LearnSketches.run(sessionFactory);
        System.out.println("Update dataset: Long sketches learned");

        System.out.println("Update dataset: Going to apply short sketches");
        AppConfig.SKETCH_LEARNING_PIVOTS_COUNT = 64;
        AppConfig.SKETCH_LEARNING_SKETCH_LENGTH = 192;
        ApplySketches.run(sessionFactory, AppConfig.SKETCH_LEARNING_SKETCH_LENGTH);
        System.out.println("Update dataset: Short sketches applied");

        System.out.println("Update dataset: Going to apply long sketches");
        AppConfig.SKETCH_LEARNING_PIVOTS_COUNT = 512;
        AppConfig.SKETCH_LEARNING_SKETCH_LENGTH = 1024;
        ApplySketches.run(sessionFactory, AppConfig.SKETCH_LEARNING_SKETCH_LENGTH);
        System.out.println("Update dataset: Long sketches applied");

        createMessiffDatasets();

//        System.out.println("Update dataset: Going to generate secondary filtering csvs");
//        LearnSecondaryFilteringWithGHPSketches.start(sessionFactory, 1024);
//        System.out.println("Update dataset: Secondary filtering csvs generated");
    }

    private static void createMessiffDatasets() throws InterruptedException {
        System.out.println("Update dataset: Create datasets for messiff");
        SystemUtils.exec(new String[]{
                "java",
                "--add-opens",
                "java.base/java.lang.invoke=ALL-UNNAMED",
                "-jar",
                AppConfig.PROTEINS_JAR_LOCATION,
                AppConfig.MESSIFF_SKETCHES_SHORT_BIN,
                AppConfig.MESSIFF_SKETCHES_LONG_BIN,
                AppConfig.SUBCONFIGS_PYTHON_INI_CONFIG_PATH
        });
        System.out.println("Update dataset: Datasets for messiff created");

        System.out.println("Update dataset: Going to generate ppp codes");
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_PPP_CODES_BUILDER_SCRIPT, "murder"});
        Thread.sleep(3 * 1000);
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_PPP_CODES_BUILDER_SCRIPT, "start"});
        Thread.sleep(10 * 1000);
        SystemUtils.execInParent(new String[]{AppConfig.MESSIFF_PPP_CODES_BUILDER_SCRIPT, "stop"});
        System.out.println("Update dataset: PPP codes generated");
    }

    public static void main(String[] args) {
        System.out.println("Daemon started");

        try {
            JavaUtils.validateNonNullProperties(AppConfig.class);
        } catch (IllegalAccessException e) {
            throw new UnrecoverableError("You have properties missing in the run.properties file. Check run.properties.example or AppConfig", e);
        }

        if (!new File(AppConfig.WORKING_DIRECTORY).equals(new File(System.getProperty("user.dir")))) {
            System.out.println("Please set the working directory to the location of the jar file.");
            System.exit(1);
        }

        migrate();
        var sessionFactory = buildSessionFactory();

        try {
            bootstrap(sessionFactory);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // start the solution
        try {
            restartSolution(sessionFactory);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // run the update loop
        while (!AppConfig.DRY_RUN) {
            try {
                System.out.println("Daemon: Dataset update in " + TimeUtils.millisTillNextUpdate(AppConfig.DAEMON_UPDATE_TRIGGER_HOUR) / 1000 / 60 / 60 + " hours");
                Thread.sleep(TimeUtils.millisTillNextUpdate(AppConfig.DAEMON_UPDATE_TRIGGER_HOUR));
                updateDataset(sessionFactory);
                restartSolution(sessionFactory);
            } catch (InterruptedException e) {
                throw new UnrecoverableError("Unexpected interrupt", e);
            } catch (IOException e) {
                throw new UnrecoverableError("Unexpected IO exception", e);
            }
        }
    }
}
