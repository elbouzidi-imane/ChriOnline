package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.OtpCode;

import java.sql.*;

public class OtpDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    // ── Sauvegarder un OTP ────────────────────────────
    public OtpCode save(OtpCode otp) {
        // Invalider les anciens OTP du même email + type
        invalidateOld(otp.getEmail(), otp.getType());

        String sql = """
            INSERT INTO otp_code (email, code, type, expire_at)
            VALUES (?,?,?,?) RETURNING id, created_at
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, otp.getEmail());
            ps.setString(2, otp.getCode());
            ps.setString(3, otp.getType());
            ps.setTimestamp(4, new Timestamp(otp.getExpireAt().getTime()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                otp.setId(rs.getInt("id"));
                otp.setCreatedAt(rs.getTimestamp("created_at"));
            }
            return otp;
        } catch (Exception e) {
            System.err.println("OtpDAO.save : " + e.getMessage());
            return null;
        }
    }

    // ── Trouver un OTP valide ─────────────────────────
    public OtpCode findValid(String email, String code, String type) {
        String sql = """
            SELECT * FROM otp_code
            WHERE email=? AND code=? AND type=?
            AND utilise=false AND expire_at > NOW()
            ORDER BY created_at DESC LIMIT 1
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, code);
            ps.setString(3, type);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            System.err.println("OtpDAO.findValid : " + e.getMessage());
        }
        return null;
    }

    // ── Marquer un OTP comme utilisé ──────────────────
    public boolean markAsUsed(int id) {
        String sql = "UPDATE otp_code SET utilise=true WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("OtpDAO.markAsUsed : " + e.getMessage());
            return false;
        }
    }

    // ── Invalider les anciens OTP ─────────────────────
    private void invalidateOld(String email, String type) {
        String sql = """
            UPDATE otp_code SET utilise=true
            WHERE email=? AND type=? AND utilise=false
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, type);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("OtpDAO.invalidateOld : " + e.getMessage());
        }
    }

    private OtpCode mapRow(ResultSet rs) throws SQLException {
        OtpCode o = new OtpCode();
        o.setId(rs.getInt("id"));
        o.setEmail(rs.getString("email"));
        o.setCode(rs.getString("code"));
        o.setType(rs.getString("type"));
        o.setExpireAt(rs.getTimestamp("expire_at"));
        o.setUtilise(rs.getBoolean("utilise"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        return o;
    }
}