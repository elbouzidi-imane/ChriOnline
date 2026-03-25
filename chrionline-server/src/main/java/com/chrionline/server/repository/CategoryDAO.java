package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    public List<Category> findAll() {
        String sql = "SELECT * FROM categorie ORDER BY nom";
        List<Category> categories = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Category c = new Category();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setDescription(rs.getString("description"));
                categories.add(c);
            }
        } catch (Exception e) {
            System.err.println("CategoryDAO.findAll : " + e.getMessage());
        }
        return categories;
    }

    public Category findById(int id) {
        String sql = "SELECT * FROM categorie WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Category c = new Category();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setDescription(rs.getString("description"));
                return c;
            }
        } catch (Exception e) {
            System.err.println("CategoryDAO.findById : " + e.getMessage());
        }
        return null;
    }
    public boolean existsByName(String nom) {
        String sql = "SELECT COUNT(*) FROM categorie WHERE LOWER(nom) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Category save(Category category) {
        String sql = "INSERT INTO categorie(nom, description) VALUES(?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, category.getNom());
            ps.setString(2, category.getDescription());

            int affected = ps.executeUpdate();

            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    category.setId(rs.getInt(1));
                }
                return category;
            }

        } catch (Exception e) {
            System.err.println("CategoryDAO.save : " + e.getMessage());
        }

        return null;

    }
}