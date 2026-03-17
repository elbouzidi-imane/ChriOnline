package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.Product;
import com.chrionline.server.model.User;
import com.chrionline.server.service.ProductService;
import com.chrionline.server.service.UserService;
import com.chrionline.server.service.OrderService;
import com.google.gson.Gson;

import java.util.List;

public class AdminHandler {

    private final ProductService productService = new ProductService();
    private final UserService    userService    = new UserService();
    private final OrderService   orderService   = new OrderService();
    private final Gson gson = new Gson();

    public Message handle(Message request) {

        // Vérification rôle ADMIN
        if (!isAdmin(request)) {
            return Message.error("Accès refusé — rôle ADMIN requis");
        }

        return switch (request.getType()) {
            case Protocol.ADMIN_ADD_PRODUCT    -> handleAddProduct(request);
            case Protocol.ADMIN_UPDATE_PRODUCT -> handleUpdateProduct(request);
            case Protocol.ADMIN_DELETE_PRODUCT -> handleDeleteProduct(request);
            case Protocol.ADMIN_GET_USERS      -> handleGetUsers(request);
            case Protocol.ADMIN_GET_ORDERS     -> handleGetOrders(request);
            case Protocol.ADMIN_UPDATE_STOCK   -> handleUpdateStock(request);
            case Protocol.ADMIN_SUSPEND_USER   -> handleSuspendUser(request);
            case Protocol.ADMIN_ACTIVATE_USER  -> handleActivateUser(request);
            default -> Message.error("Type non géré par AdminHandler");
        };
    }

    // ────────────────────────────────────────────────
    // Vérification ADMIN
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

    // Extraire données après "adminId:"
    private String getData(Message req) {
        String payload = req.getPayload();
        int idx = payload.indexOf(":");
        return idx >= 0 ? payload.substring(idx + 1) : "";
    }

    // ────────────────────────────────────────────────
    // ADD PRODUCT
    private Message handleAddProduct(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);

            if (parts.length < 6) {
                return Message.error("Format invalide pour ajout produit");
            }

            Product product = new Product();

            product.setCategorieId(Integer.parseInt(parts[0].trim()));
            product.setNom(parts[1].trim());
            product.setDescription(parts[2].trim());
            product.setMatiere(parts[3].trim());
            product.setCouleur(parts[4].trim());
            product.setPrixOriginal(Double.parseDouble(parts[5].trim()));

            product.setPrixReduit(
                    parts.length > 6 && !parts[6].trim().isEmpty()
                            ? Double.parseDouble(parts[6].trim())
                            : 0
            );

            product.setImageUrl(
                    parts.length > 7 ? parts[7].trim() : ""
            );

            // ✅ CORRECTION BUG IMPORTANT
            product.setStatut("ACTIF");

            Product saved = productService.addProduct(product);

            if (saved == null) {
                return Message.error("Erreur ajout produit");
            }

            return Message.ok(Protocol.ADMIN_ADD_PRODUCT, gson.toJson(saved));

        } catch (Exception e) {
            return Message.error("Erreur ajout produit : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
    // UPDATE PRODUCT
    private Message handleUpdateProduct(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);

            if (parts.length < 7) {
                return Message.error("Format invalide pour modification produit");
            }

            Product product = new Product();

            product.setId(Integer.parseInt(parts[0].trim()));
            product.setNom(parts[1].trim());
            product.setDescription(parts[2].trim());
            product.setMatiere(parts[3].trim());
            product.setCouleur(parts[4].trim());
            product.setPrixOriginal(Double.parseDouble(parts[5].trim()));

            product.setPrixReduit(
                    parts.length > 6 && !parts[6].trim().isEmpty()
                            ? Double.parseDouble(parts[6].trim())
                            : 0
            );

            product.setImageUrl(
                    parts.length > 7 ? parts[7].trim() : ""
            );

            boolean ok = productService.updateProduct(product);

            if (!ok) {
                return Message.error("Produit introuvable");
            }

            return Message.ok(Protocol.ADMIN_UPDATE_PRODUCT, "Produit mis à jour");

        } catch (Exception e) {
            return Message.error("Erreur modification produit : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
    // DELETE PRODUCT
    private Message handleDeleteProduct(Message req) {
        try {
            int produitId = Integer.parseInt(getData(req).trim());

            boolean ok = productService.deleteProduct(produitId);

            if (!ok) {
                return Message.error("Produit introuvable");
            }

            return Message.ok(
                    Protocol.ADMIN_DELETE_PRODUCT,
                    "Produit désactivé avec succès"
            );

        } catch (Exception e) {
            return Message.error("Erreur suppression produit : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
    // GET USERS
    private Message handleGetUsers(Message req) {
        try {
            List<User> users = userService.getAllUsers();
            return Message.ok(Protocol.ADMIN_GET_USERS, gson.toJson(users));

        } catch (Exception e) {
            return Message.error("Erreur récupération utilisateurs : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
    // GET ORDERS
    private Message handleGetOrders(Message req) {
        try {
            var orders = orderService.getAllOrders();
            return Message.ok(Protocol.ADMIN_GET_ORDERS, gson.toJson(orders));

        } catch (Exception e) {
            return Message.error("Erreur récupération commandes : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
    // UPDATE STOCK
    private Message handleUpdateStock(Message req) {
        try {
            String[] parts = getData(req).split("\\|");

            int tailleId = Integer.parseInt(parts[0].trim());
            int newStock = Integer.parseInt(parts[1].trim());

            boolean ok = productService.updateStock(tailleId, newStock);

            if (!ok) {
                return Message.error("Taille introuvable");
            }

            return Message.ok(
                    Protocol.ADMIN_UPDATE_STOCK,
                    "Stock mis à jour : " + newStock
            );

        } catch (Exception e) {
            return Message.error("Erreur mise à jour stock : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
    // SUSPEND USER
    private Message handleSuspendUser(Message req) {
        try {
            int userId = Integer.parseInt(getData(req).trim());

            boolean ok = userService.suspendUser(userId);

            if (!ok) {
                return Message.error("Utilisateur introuvable");
            }

            return Message.ok(
                    Protocol.ADMIN_SUSPEND_USER,
                    "Utilisateur suspendu"
            );

        } catch (Exception e) {
            return Message.error("Erreur suspension : " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────────
    // ACTIVATE USER
    private Message handleActivateUser(Message req) {
        try {
            int userId = Integer.parseInt(getData(req).trim());

            boolean ok = userService.activateUser(userId);

            if (!ok) {
                return Message.error("Utilisateur introuvable");
            }

            return Message.ok(
                    Protocol.ADMIN_ACTIVATE_USER,
                    "Utilisateur activé"
            );

        } catch (Exception e) {
            return Message.error("Erreur activation : " + e.getMessage());
        }
    }

}