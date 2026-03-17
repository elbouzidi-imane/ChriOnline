package com.chrionline.server.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseManager {

    private static DatabaseManager instance;
    private String url;
    private String user;
    private String password;

    private DatabaseManager() {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null)
                throw new RuntimeException("db.properties introuvable !");
            Properties p = new Properties();
            p.load(in);
            this.url      = p.getProperty("db.url");
            this.user     = p.getProperty("db.user");
            this.password = p.getProperty("db.password");
            System.out.println("DatabaseManager initialisé : " + url);
        } catch (Exception e) {
            throw new RuntimeException("Erreur chargement db.properties : "
                    + e.getMessage());
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    public Connection getConnection() throws Exception {
        return DriverManager.getConnection(url, user, password);
    }

    // Test rapide de connexion
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Connexion PostgreSQL OK !");
            return true;
        } catch (Exception e) {
            System.err.println("Connexion PostgreSQL ECHOUEE : " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        DatabaseManager.getInstance().testConnection();
    }

}