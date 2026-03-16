package com.chrionline.server.db;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
public class DatabaseManager {
    private static DatabaseManager instance;
    private String url, user, password;
    private DatabaseManager() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            Properties p = new Properties(); p.load(in);
            this.url = p.getProperty("db.url");
            this.user = p.getProperty("db.user");
            this.password = p.getProperty("db.password");
        } catch (Exception e) { throw new RuntimeException("Erreur db.properties", e); }
    }
    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager(); return instance;
    }
    public Connection getConnection() throws Exception {
        return DriverManager.getConnection(url, user, password);
    }
}
