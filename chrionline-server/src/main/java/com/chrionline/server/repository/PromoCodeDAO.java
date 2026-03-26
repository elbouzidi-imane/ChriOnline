package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.PromoCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PromoCodeDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    public PromoCode save(PromoCode promo) {
        String sql = """
                INSERT INTO promo_code
                    (code, reduction_type, reduction_value, minimum_order_amount,
                     start_date, end_date, max_uses, max_usage_per_user, active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindPromo(ps, promo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                promo.setId(rs.getInt("id"));
            }
            return promo;
        } catch (Exception e) {
            System.err.println("PromoCodeDAO.save : " + e.getMessage());
            return null;
        }
    }

    public List<PromoCode> findAll() {
        String sql = "SELECT * FROM promo_code ORDER BY id DESC";
        List<PromoCode> promos = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                promos.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.err.println("PromoCodeDAO.findAll : " + e.getMessage());
        }
        return promos;
    }

    public PromoCode findByCode(String code) {
        String sql = "SELECT * FROM promo_code WHERE UPPER(code) = UPPER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (Exception e) {
            System.err.println("PromoCodeDAO.findByCode : " + e.getMessage());
        }
        return null;
    }

    public boolean existsByCode(String code) {
        return findByCode(code) != null;
    }

    public boolean updateActive(int promoId, boolean active) {
        String sql = "UPDATE promo_code SET active = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, promoId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("PromoCodeDAO.updateActive : " + e.getMessage());
            return false;
        }
    }

    private void bindPromo(PreparedStatement ps, PromoCode promo) throws Exception {
        ps.setString(1, promo.getCode());
        ps.setString(2, promo.getReductionType());
        ps.setDouble(3, promo.getReductionValue());
        ps.setDouble(4, promo.getMinimumOrderAmount());
        ps.setDate(5, new java.sql.Date(promo.getStartDate().getTime()));
        ps.setDate(6, new java.sql.Date(promo.getEndDate().getTime()));
        ps.setInt(7, promo.getMaxUses());
        ps.setInt(8, promo.getMaxUsagePerUser());
        ps.setBoolean(9, promo.isActive());
    }

    private PromoCode mapRow(ResultSet rs) throws Exception {
        PromoCode promo = new PromoCode();
        promo.setId(rs.getInt("id"));
        promo.setCode(rs.getString("code"));
        promo.setReductionType(rs.getString("reduction_type"));
        promo.setReductionValue(rs.getDouble("reduction_value"));
        promo.setMinimumOrderAmount(rs.getDouble("minimum_order_amount"));
        promo.setStartDate(rs.getDate("start_date"));
        promo.setEndDate(rs.getDate("end_date"));
        promo.setMaxUses(rs.getInt("max_uses"));
        promo.setMaxUsagePerUser(rs.getInt("max_usage_per_user"));
        promo.setActive(rs.getBoolean("active"));
        return promo;
    }
}
