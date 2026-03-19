package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.*;
import com.chrionline.server.repository.*;
import com.chrionline.server.service.*;
import com.google.gson.Gson;

import java.util.List;

public class AdminHandler {

    private final ProductService productService = new ProductService();
    private final UserService    userService    = new UserService();
    private final OrderService   orderService   = new OrderService();
    private final PaymentService paymentService = new PaymentService();
    private final GuideDAO       guideDAO       = new GuideDAO();
    private final SizeDAO        sizeDAO        = new SizeDAO();
    private final UserDAO        userDAO        = new UserDAO();
    private final Gson gson = new Gson();

    public Message handle(Message request) {
        if (!isAdmin(request)) {
            return Message.error("Accès refusé — rôle ADMIN requis");
        }

        return switch (request.getType()) {
            // Produits
            case Protocol.ADMIN_ADD_PRODUCT    -> handleAddProduct(request);
            case Protocol.ADMIN_UPDATE_PRODUCT -> handleUpdateProduct(request);
            case Protocol.ADMIN_DELETE_PRODUCT -> handleDeleteProduct(request);
            // Tailles
            case Protocol.ADMIN_ADD_SIZE       -> handleAddSize(request);
            case Protocol.ADMIN_DELETE_SIZE    -> handleDeleteSize(request);
            case Protocol.ADMIN_GET_SIZES      -> handleGetSizes(request);
            // Guide taille
            case Protocol.ADMIN_ADD_GUIDE      -> handleAddGuide(request);
            case Protocol.ADMIN_DELETE_GUIDE   -> handleDeleteGuide(request);
            case Protocol.ADMIN_GET_GUIDE      -> handleGetGuide(request);
            // Stock
            case Protocol.ADMIN_UPDATE_STOCK   -> handleUpdateStock(request);
            // Utilisateurs
            case Protocol.ADMIN_GET_USERS      -> handleGetUsers(request);
            case Protocol.ADMIN_SUSPEND_USER   -> handleSuspendUser(request);
            case Protocol.ADMIN_ACTIVATE_USER  -> handleActivateUser(request);
            case Protocol.ADMIN_UPDATE_USER    -> handleUpdateUser(request);
            // Commandes
            case Protocol.ADMIN_GET_ORDERS          -> handleGetOrders(request);
            case Protocol.ADMIN_GET_ORDER_DETAIL    -> handleGetOrderDetail(request);
            case Protocol.ADMIN_UPDATE_ORDER_STATUT -> handleUpdateOrderStatut(request);
            // Paiement
            case Protocol.ADMIN_GET_PAYMENT -> handleGetPayment(request);
            case Protocol.ADMIN_REMBOURSE   -> handleRembourse(request);
            default -> Message.error("Type non géré par AdminHandler");
        };
    }

    // ── Vérification admin ────────────────────────────
    private boolean isAdmin(Message req) {
        try {
            String payload = req.getPayload();
            if (payload == null || !payload.contains(":")) return false;
            int adminId = Integer.parseInt(payload.split(":")[0].trim());
            User user = userService.getUserById(adminId);
            return user != null && user.isAdmin();
        } catch (Exception e) {
            return false;
        }
    }

    private String getData(Message req) {
        String payload = req.getPayload();
        int idx = payload.indexOf(":");
        return idx >= 0 ? payload.substring(idx + 1) : "";
    }

    // ══ PRODUITS ══════════════════════════════════════

    // payload : "adminId:categorieId|nom|description|matiere|couleur|prixOriginal|prixReduit|imageUrl"
    private Message handleAddProduct(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            if (parts.length < 6) return Message.error("Format invalide");
            Product p = new Product();
            p.setCategorieId(Integer.parseInt(parts[0].trim()));
            p.setNom(parts[1].trim());
            p.setDescription(parts[2].trim());
            p.setMatiere(parts[3].trim());
            p.setCouleur(parts[4].trim());
            p.setPrixOriginal(Double.parseDouble(parts[5].trim()));
            p.setPrixReduit(parts.length > 6 && !parts[6].trim().isEmpty()
                    ? Double.parseDouble(parts[6].trim()) : 0);
            p.setImageUrl(parts.length > 7 ? parts[7].trim() : "");
            Product saved = productService.addProduct(p);
            if (saved == null) return Message.error("Erreur ajout produit");
            return Message.ok(Protocol.ADMIN_ADD_PRODUCT, gson.toJson(saved));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // payload : "adminId:id|nom|description|matiere|couleur|prixOriginal|prixReduit|imageUrl"
    private Message handleUpdateProduct(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            if (parts.length < 7) return Message.error("Format invalide");
            Product p = new Product();
            p.setId(Integer.parseInt(parts[0].trim()));
            p.setNom(parts[1].trim());
            p.setDescription(parts[2].trim());
            p.setMatiere(parts[3].trim());
            p.setCouleur(parts[4].trim());
            p.setPrixOriginal(Double.parseDouble(parts[5].trim()));
            p.setPrixReduit(parts.length > 6 && !parts[6].trim().isEmpty()
                    ? Double.parseDouble(parts[6].trim()) : 0);
            p.setImageUrl(parts.length > 7 ? parts[7].trim() : "");
            boolean ok = productService.updateProduct(p);
            if (!ok) return Message.error("Produit introuvable");
            return Message.ok(Protocol.ADMIN_UPDATE_PRODUCT, "Produit mis à jour");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // payload : "adminId:produitId"
    private Message handleDeleteProduct(Message req) {
        try {
            int id = Integer.parseInt(getData(req).trim());
            boolean ok = productService.deleteProduct(id);
            if (!ok) return Message.error("Produit introuvable");
            return Message.ok(Protocol.ADMIN_DELETE_PRODUCT, "Produit désactivé");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // ══ TAILLES ═══════════════════════════════════════

    // payload : "adminId:produitId|valeur|stock"
    private Message handleAddSize(Message req) {
        try {
            String[] parts = getData(req).split("\\|");
            ProductSize size = new ProductSize();
            size.setProduitId(Integer.parseInt(parts[0].trim()));
            size.setValeur(parts[1].trim());
            size.setStock(Integer.parseInt(parts[2].trim()));
            ProductSize saved = sizeDAO.save(size);
            if (saved == null) return Message.error("Erreur ajout taille");
            return Message.ok(Protocol.ADMIN_ADD_SIZE, gson.toJson(saved));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // payload : "adminId:tailleId"
    private Message handleDeleteSize(Message req) {
        try {
            int tailleId = Integer.parseInt(getData(req).trim());
            boolean ok = sizeDAO.delete(tailleId);
            if (!ok) return Message.error("Taille introuvable");
            return Message.ok(Protocol.ADMIN_DELETE_SIZE, "Taille supprimée");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // payload : "adminId:produitId"
    private Message handleGetSizes(Message req) {
        try {
            int produitId = Integer.parseInt(getData(req).trim());
            List<ProductSize> sizes = sizeDAO.findByProduit(produitId);
            return Message.ok(Protocol.ADMIN_GET_SIZES, gson.toJson(sizes));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // ══ GUIDE TAILLE ══════════════════════════════════

    // payload : "adminId:produitId|taille|poitrine|tailleCm|hanches"
    private Message handleAddGuide(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            GuideTaille guide = new GuideTaille();
            guide.setProduitId(Integer.parseInt(parts[0].trim()));
            guide.setTaille(parts[1].trim());
            guide.setPoitrine(parts.length > 2 ? parts[2].trim() : "");
            guide.setTailleCm(parts.length > 3 ? parts[3].trim() : "");
            guide.setHanches(parts.length > 4 ? parts[4].trim() : "");
            GuideTaille saved = guideDAO.save(guide);
            if (saved == null) return Message.error("Erreur ajout guide");
            return Message.ok(Protocol.ADMIN_ADD_GUIDE, gson.toJson(saved));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // payload : "adminId:guideId"
    private Message handleDeleteGuide(Message req) {
        try {
            int guideId = Integer.parseInt(getData(req).trim());
            boolean ok = guideDAO.delete(guideId);
            if (!ok) return Message.error("Guide introuvable");
            return Message.ok(Protocol.ADMIN_DELETE_GUIDE, "Guide supprimé");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // payload : "adminId:produitId"
    private Message handleGetGuide(Message req) {
        try {
            int produitId = Integer.parseInt(getData(req).trim());
            List<GuideTaille> guides = guideDAO.findByProduit(produitId);
            return Message.ok(Protocol.ADMIN_GET_GUIDE, gson.toJson(guides));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // ══ STOCK ═════════════════════════════════════════

    // payload : "adminId:tailleId|newStock"
    private Message handleUpdateStock(Message req) {
        try {
            String[] parts = getData(req).split("\\|");
            int tailleId = Integer.parseInt(parts[0].trim());
            int newStock = Integer.parseInt(parts[1].trim());
            boolean ok = productService.updateStock(tailleId, newStock);
            if (!ok) return Message.error("Taille introuvable");
            return Message.ok(Protocol.ADMIN_UPDATE_STOCK, "Stock mis à jour : " + newStock);
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // ══ UTILISATEURS ══════════════════════════════════

    private Message handleGetUsers(Message req) {
        try {
            List<User> users = userService.getAllUsers();
            return Message.ok(Protocol.ADMIN_GET_USERS, gson.toJson(users));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // payload : "adminId:userId"
    private Message handleSuspendUser(Message req) {
        try {
            int userId = Integer.parseInt(getData(req).trim());
            boolean ok = userService.suspendUser(userId);
            if (!ok) return Message.error("Utilisateur introuvable");
            return Message.ok(Protocol.ADMIN_SUSPEND_USER, "Utilisateur suspendu");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // payload : "adminId:userId"
    private Message handleActivateUser(Message req) {
        try {
            int userId = Integer.parseInt(getData(req).trim());
            boolean ok = userService.activateUser(userId);
            if (!ok) return Message.error("Utilisateur introuvable");
            return Message.ok(Protocol.ADMIN_ACTIVATE_USER, "Utilisateur activé");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // payload : "adminId:userId|nom|prenom|telephone|adresse"
    private Message handleUpdateUser(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            User user = new User();
            user.setId(Integer.parseInt(parts[0].trim()));
            user.setNom(parts[1].trim());
            user.setPrenom(parts[2].trim());
            user.setTelephone(parts.length > 3 ? parts[3].trim() : "");
            user.setAdresse(parts.length > 4 ? parts[4].trim() : "");
            boolean ok = userService.updateProfil(user);
            if (!ok) return Message.error("Utilisateur introuvable");
            return Message.ok(Protocol.ADMIN_UPDATE_USER, "Utilisateur mis à jour");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // ══ COMMANDES ═════════════════════════════════════

    private Message handleGetOrders(Message req) {
        try {
            var orders = orderService.getAllOrders();
            return Message.ok(Protocol.ADMIN_GET_ORDERS, gson.toJson(orders));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // payload : "adminId:orderId"
    private Message handleGetOrderDetail(Message req) {
        try {
            int orderId = Integer.parseInt(getData(req).trim());
            Order order = orderService.getOrderById(orderId);
            if (order == null) return Message.error("Commande introuvable");
            return Message.ok(Protocol.ADMIN_GET_ORDER_DETAIL, gson.toJson(order));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // payload : "adminId:orderId|newStatut"
    // statuts : EN_ATTENTE, VALIDEE, EXPEDIEE, LIVREE, ANNULEE
    private Message handleUpdateOrderStatut(Message req) {
        try {
            String[] parts = getData(req).split("\\|");
            int    orderId   = Integer.parseInt(parts[0].trim());
            String newStatut = parts[1].trim();

            // Vérifier que le statut est valide
            List<String> statuts = List.of(
                    "EN_ATTENTE","VALIDEE","EXPEDIEE","LIVREE","ANNULEE");
            if (!statuts.contains(newStatut)) {
                return Message.error("Statut invalide : " + newStatut);
            }

            boolean ok = orderService.updateStatut(orderId, newStatut);
            if (!ok) return Message.error("Commande introuvable");
            return Message.ok(Protocol.ADMIN_UPDATE_ORDER_STATUT,
                    "Statut mis à jour : " + newStatut);
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // ══ PAIEMENT ══════════════════════════════════════

    // payload : "adminId:commandeId"
    private Message handleGetPayment(Message req) {
        try {
            int commandeId = Integer.parseInt(getData(req).trim());
            Payment payment = paymentService.getByCommande(commandeId);
            if (payment == null) return Message.error("Paiement introuvable");
            return Message.ok(Protocol.ADMIN_GET_PAYMENT, gson.toJson(payment));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    // payload : "adminId:commandeId"
    private Message handleRembourse(Message req) {
        try {
            int commandeId = Integer.parseInt(getData(req).trim());
            boolean ok = paymentService.rembourser(commandeId);
            if (!ok) return Message.error("Paiement introuvable");
            return Message.ok(Protocol.ADMIN_REMBOURSE, "Paiement remboursé");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }
    
}