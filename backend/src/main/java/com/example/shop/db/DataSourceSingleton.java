package com.example.shop.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Простая реализация Singleton для управления соединениями с БД
public class DataSourceSingleton {
    private static DataSourceSingleton instance;

    private final String url;
    private final String user;
    private final String pass;

    private DataSourceSingleton() {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "5432");
        String db = System.getenv().getOrDefault("DB_NAME", "shopdb");
        this.user = System.getenv().getOrDefault("DB_USER", "shopuser");
        this.pass = System.getenv().getOrDefault("DB_PASS", "shoppass");
        this.url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
    }

    public static synchronized DataSourceSingleton getInstance() {
        if (instance == null) {
            instance = new DataSourceSingleton();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }
}
