package com.chrionline.server.db;

import com.chrionline.server.repository.CategoryDAO;
import com.chrionline.server.repository.ProductDAO;
import com.chrionline.server.repository.UserDAO;

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
        System.out.println("==============================");
        System.out.println("  TEST DatabaseManager + DAOs ");
        System.out.println("==============================");

        // Test connexion
        boolean ok = DatabaseManager.getInstance().testConnection();
        if (!ok) {
            System.err.println("Arrêt — connexion impossible.");
            return;
        }

        // Test UserDAO
        System.out.println("\n--- UserDAO ---");
        UserDAO userDAO = new UserDAO();
        System.out.println("Nombre d'utilisateurs : " + userDAO.findAll().size());
        userDAO.findAll().forEach(u ->
                System.out.println("  " + u));

        // Test ProductDAO
        System.out.println("\n--- ProductDAO ---");
        ProductDAO productDAO = new ProductDAO();
        System.out.println("Nombre de produits : " + productDAO.findAll().size());
        productDAO.findAll().forEach(p ->
                System.out.println("  " + p));

        // Test CategoryDAO
        System.out.println("\n--- CategoryDAO ---");
        CategoryDAO categoryDAO = new CategoryDAO();
        System.out.println("Nombre de catégories : " + categoryDAO.findAll().size());
        categoryDAO.findAll().forEach(c ->
                System.out.println("  " + c));

        System.out.println("\n==============================");
        System.out.println("  TESTS TERMINÉS AVEC SUCCÈS  ");
        System.out.println("==============================");
    }
}