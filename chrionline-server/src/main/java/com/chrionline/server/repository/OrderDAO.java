package com.chrionline.server.repository;

import com.chrionline.server.db.DatabaseManager;
import com.chrionline.server.model.Order;
import com.chrionline.server.model.OrderLine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private Connection getConnection() throws Exception {
        return DatabaseManager.getInstance().getConnection();
    }

    // ── Créer une commande ────────────────────────────
    public Order save(Order order) {
        String sql = """
            INSERT INTO commande
                (reference, utilisateur_id, statut, montant_total, adresse_livraison)
            VALUES (?,?,?,?,?) RETURNING id, date_commande
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getReference());
            ps.setInt(2, order.getUtilisateurId());
            ps.setString(3, order.getStatut());
            ps.setDouble(4, order.getMontantTotal());
            ps.setString(5, order.getAdresseLivraison());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                order.setId(rs.getInt("id"));
                order.setDateCommande(rs.getDate("date_commande"));
            }
            return order;
        } catch (Exception e) {
            System.err.println("OrderDAO.save : " + e.getMessage());
            return null;
        }
    }

    // ── Ajouter une ligne commande ────────────────────
    public boolean saveOrderLine(OrderLine line) {
        String sql = """
            INSERT INTO ligne_commande
                (commande_id, produit_id, taille_produit_id, quantite, prix_unitaire)
            VALUES (?,?,?,?,?)
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, line.getCommandeId());
            ps.setInt(2, line.getProduitId());
            ps.setInt(3, line.getTailleId());
            ps.setInt(4, line.getQuantite());
            ps.setDouble(5, line.getPrixUnitaire());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("OrderDAO.saveOrderLine : " + e.getMessage());
            return false;
        }
    }

    // ── Commandes d'un utilisateur ────────────────────
    public List<Order> findByUser(int userId) {
        String sql = """
        SELECT * FROM commande
        WHERE utilisateur_id = ?
        ORDER BY date_commande DESC
        """;
        List<Order> orders = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order o = mapRow(rs);
                o.setLignes(findLines(o.getId())); // ← ajouter cette ligne
                orders.add(o);
            }
        } catch (Exception e) {
            System.err.println("OrderDAO.findByUser : " + e.getMessage());
        }
        return orders;
    }

    // ── Commande par id ───────────────────────────────
    public Order findById(int id) {
        String sql = "SELECT * FROM commande WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order o = mapRow(rs);
                o.setLignes(findLines(id));
                return o;
            }
        } catch (Exception e) {
            System.err.println("OrderDAO.findById : " + e.getMessage());
        }
        return null;
    }

    // ── Toutes les commandes (admin) ──────────────────
    public List<Order> findAll() {
        String sql = "SELECT * FROM commande ORDER BY date_commande DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Order o = mapRow(rs);
                o.setLignes(findLines(o.getId())); // ← ajouter cette ligne
                orders.add(o);
            }
        } catch (Exception e) {
            System.err.println("OrderDAO.findAll : " + e.getMessage());
        }
        return orders;
    }

    // ── Mettre à jour le statut ───────────────────────
    public boolean updateStatut(int id, String statut) {
        String sql = "UPDATE commande SET statut = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("OrderDAO.updateStatut : " + e.getMessage());
            return false;
        }
    }

    public boolean updateCancellationInfo(int id, String statut, String motifAnnulation) {
        String sql = "UPDATE commande SET statut = ?, motif_annulation = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setString(2, motifAnnulation == null || motifAnnulation.isBlank() ? null : motifAnnulation.trim());
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("OrderDAO.updateCancellationInfo : " + e.getMessage());
            return false;
        }
    }

    // ── Lignes d'une commande ─────────────────────────
    public List<OrderLine> findLines(int commandeId) {
        String sql = "SELECT * FROM ligne_commande WHERE commande_id = ?";
        List<OrderLine> lines = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderLine l = new OrderLine();
                l.setId(rs.getInt("id"));
                l.setCommandeId(rs.getInt("commande_id"));
                l.setProduitId(rs.getInt("produit_id"));
                l.setTailleId(rs.getInt("taille_produit_id"));
                l.setQuantite(rs.getInt("quantite"));
                l.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                lines.add(l);
            }
        } catch (Exception e) {
            System.err.println("OrderDAO.findLines : " + e.getMessage());
        }
        return lines;
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setReference(rs.getString("reference"));
        o.setUtilisateurId(rs.getInt("utilisateur_id"));
        o.setDateCommande(rs.getDate("date_commande"));
        o.setStatut(rs.getString("statut"));
        o.setMontantTotal(rs.getDouble("montant_total"));
        o.setAdresseLivraison(rs.getString("adresse_livraison"));
        o.setMotifAnnulation(rs.getString("motif_annulation"));
        return o;
    }
}
