package com.tss.FoodApp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(
                AppConfig.DB_URL,
                AppConfig.DB_USERNAME,
                AppConfig.DB_PASSWORD
        );
    }
}