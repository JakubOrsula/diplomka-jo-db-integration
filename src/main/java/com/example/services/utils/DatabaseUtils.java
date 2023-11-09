package com.example.services.utils;

import com.example.services.configuration.AppConfig;
import com.example.utils.UnrecoverableError;
import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class DatabaseUtils {
    public static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();

            configuration.setProperty("hibernate.connection.url", AppConfig.DATABASE_DRIVER + "://" + AppConfig.DATABASE_ADDRESS + "/" + AppConfig.DATABASE_NAME);
            configuration.setProperty("hibernate.connection.username", AppConfig.DATABASE_USERNAME);
            configuration.setProperty("hibernate.connection.password", AppConfig.DATABASE_PASSWORD);
            configuration.configure("hibernate.cfg.xml");

            return configuration.buildSessionFactory();
        } catch (Exception e) {
            throw new UnrecoverableError("Failed to build the SessionFactory.", e);
        }
    }

    public static void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(AppConfig.DATABASE_DRIVER + "://" + AppConfig.DATABASE_ADDRESS + "/",
                        AppConfig.FLYWAY_CONNECTION_USERNAME,
                        AppConfig.FLYWAY_CONNECTION_PASSWORD)
                .defaultSchema(AppConfig.DATABASE_NAME)
                .locations("classpath:/db/migration")
                .load();
        flyway.migrate();
    }
}
