package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.CancellationConfig;
import com.chrionline.server.model.Category;
import com.chrionline.server.model.GuideTaille;
import com.chrionline.server.model.Order;
import com.chrionline.server.model.Payment;
import com.chrionline.server.model.PromoCode;
import com.chrionline.server.model.PromoUsageStat;
import com.chrionline.server.model.Product;
import com.chrionline.server.model.ProductSize;
import com.chrionline.server.model.User;
import com.chrionline.server.network.ConnectedClientRegistry;
import com.chrionline.server.repository.CategoryDAO;
import com.chrionline.server.repository.GuideDAO;
import com.chrionline.server.repository.SizeDAO;
import com.chrionline.server.service.CancellationConfigService;
import com.chrionline.server.service.NotificationService;
import com.chrionline.server.service.OrderService;
import com.chrionline.server.service.PaymentService;
import com.chrionline.server.service.PromoService;
import com.chrionline.server.service.ProductService;
import com.chrionline.server.service.UserService;
import com.google.gson.Gson;

import java.util.List;

public class AdminHandler {

    private final ProductService productService = new ProductService();
    private final UserService userService = new UserService();
    private final OrderService orderService = new OrderService();
    private final PaymentService paymentService = new PaymentService();
    private final PromoService promoService = new PromoService();
    private final GuideDAO guideDAO = new GuideDAO();
    private final SizeDAO sizeDAO = new SizeDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final Gson gson = new Gson();
    private final NotificationService notificationService = new NotificationService();

    public Message handle(Message request) {
        if (!isAdmin(request)) {
            return Message.error("Acces refuse - role ADMIN requis");
        }

        return switch (request.getType()) {
            case Protocol.ADMIN_ADD_PRODUCT -> handleAddProduct(request);
            case Protocol.ADMIN_UPDATE_PRODUCT -> handleUpdateProduct(request);
            case Protocol.ADMIN_DELETE_PRODUCT -> handleDeleteProduct(request);
            case Protocol.ADMIN_ADD_CATEGORY -> handleAddCategory(request);
            case Protocol.ADMIN_GET_CATEGORIES -> handleGetCategories();

            case Protocol.ADMIN_ADD_SIZE -> handleAddSize(request);
            case Protocol.ADMIN_DELETE_SIZE -> handleDeleteSize(request);
            case Protocol.ADMIN_GET_SIZES -> handleGetSizes(request);

            case Protocol.ADMIN_ADD_GUIDE -> handleAddGuide(request);
            case Protocol.ADMIN_DELETE_GUIDE -> handleDeleteGuide(request);
            case Protocol.ADMIN_GET_GUIDE -> handleGetGuide(request);

            case Protocol.ADMIN_UPDATE_STOCK -> handleUpdateStock(request);

            case Protocol.ADMIN_GET_USERS -> handleGetUsers();
            case Protocol.ADMIN_SUSPEND_USER -> handleSuspendUser(request);
            case Protocol.ADMIN_ACTIVATE_USER -> handleActivateUser(request);
            case Protocol.ADMIN_UPDATE_USER -> handleUpdateUser(request);

            case Protocol.ADMIN_GET_ORDERS -> handleGetOrders();
            case Protocol.ADMIN_GET_ORDER_DETAIL -> handleGetOrderDetail(request);
            case Protocol.ADMIN_UPDATE_ORDER_STATUT -> handleUpdateOrderStatut(request);
            case Protocol.ADMIN_GET_CANCELLATION_CONFIG -> handleGetCancellationConfig();
            case Protocol.ADMIN_UPDATE_CANCELLATION_CONFIG -> handleUpdateCancellationConfig(request);

            case Protocol.ADMIN_GET_PAYMENT -> handleGetPayment(request);
            case Protocol.ADMIN_REMBOURSE -> handleRembourse(request);
            case Protocol.ADMIN_ADD_PROMO -> handleAddPromo(request);
            case Protocol.ADMIN_GET_PROMOS -> handleGetPromos();
            case Protocol.ADMIN_GET_PROMO_STATS -> handleGetPromoStats(request);
            case Protocol.ADMIN_TOGGLE_PROMO -> handleTogglePromo(request);

            default -> Message.error("Type non gere par AdminHandler");
        };
    }

    private boolean isAdmin(Message req) {
        try {
            String payload = req.getPayload();
            if (payload == null || !payload.contains(":")) {
                return false;
            }
            int adminId = Integer.parseInt(payload.substring(0, payload.indexOf(':')).trim());
            User user = userService.getUserById(adminId);
            return user != null && user.isAdmin();
        } catch (Exception e) {
            return false;
        }
    }

    private String getData(Message req) {
        String payload = req.getPayload();
        int idx = payload.indexOf(':');
        return idx >= 0 ? payload.substring(idx + 1) : "";
    }

    private Message handleAddProduct(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            if (parts.length < 6) {
                return Message.error("Format invalide");
            }

            Product product = new Product();
            product.setCategorieId(Integer.parseInt(parts[0].trim()));
            product.setNom(parts[1].trim());
            product.setDescription(parts[2].trim());
            product.setMatiere(parts[3].trim());
            product.setCouleur(parts[4].trim());
            product.setPrixOriginal(Double.parseDouble(parts[5].trim()));
            product.setPrixReduit(parts.length > 6 && !parts[6].trim().isEmpty()
                    ? Double.parseDouble(parts[6].trim()) : 0);
            product.setImageUrl(parts.length > 7 ? parts[7].trim() : "");

            Product saved = productService.addProduct(product);
            if (saved == null) {
                return Message.error("Erreur ajout produit");
            }
            return Message.ok(Protocol.ADMIN_ADD_PRODUCT, gson.toJson(saved));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleUpdateProduct(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            if (parts.length < 7) {
                return Message.error("Format invalide");
            }

            Product product = new Product();
            product.setId(Integer.parseInt(parts[0].trim()));
            product.setNom(parts[1].trim());
            product.setDescription(parts[2].trim());
            product.setMatiere(parts[3].trim());
            product.setCouleur(parts[4].trim());
            product.setPrixOriginal(Double.parseDouble(parts[5].trim()));
            product.setPrixReduit(parts.length > 6 && !parts[6].trim().isEmpty()
                    ? Double.parseDouble(parts[6].trim()) : 0);
            product.setImageUrl(parts.length > 7 ? parts[7].trim() : "");

            boolean ok = productService.updateProduct(product);
            if (!ok) {
                return Message.error("Produit introuvable");
            }
            return Message.ok(Protocol.ADMIN_UPDATE_PRODUCT, "Produit mis a jour");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleDeleteProduct(Message req) {
        try {
            int id = Integer.parseInt(getData(req).trim());
            boolean ok = productService.deleteProduct(id);
            if (!ok) {
                return Message.error("Produit introuvable");
            }
            return Message.ok(Protocol.ADMIN_DELETE_PRODUCT, "Produit desactive");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleAddCategory(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            if (parts.length < 1 || parts[0].trim().isEmpty()) {
                return Message.error("Nom de categorie obligatoire");
            }

            Category category = new Category();
            category.setNom(parts[0].trim());
            category.setDescription(parts.length > 1 ? parts[1].trim() : "");

            if (categoryDAO.existsByName(category.getNom())) {
                return Message.error("Categorie deja existante");
            }

            Category saved = categoryDAO.save(category);
            if (saved == null) {
                return Message.error("Erreur ajout categorie");
            }

            return Message.ok(Protocol.ADMIN_ADD_CATEGORY, gson.toJson(saved));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleGetCategories() {
        try {
            return Message.ok(Protocol.ADMIN_GET_CATEGORIES, gson.toJson(categoryDAO.findAll()));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleAddSize(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            ProductSize size = new ProductSize();
            size.setProduitId(Integer.parseInt(parts[0].trim()));
            size.setValeur(parts[1].trim());
            size.setStock(Integer.parseInt(parts[2].trim()));

            ProductSize saved = sizeDAO.save(size);
            if (saved == null) {
                return Message.error("Erreur ajout taille");
            }
            return Message.ok(Protocol.ADMIN_ADD_SIZE, gson.toJson(saved));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleDeleteSize(Message req) {
        try {
            int tailleId = Integer.parseInt(getData(req).trim());
            boolean ok = sizeDAO.delete(tailleId);
            if (!ok) {
                return Message.error("Taille introuvable");
            }
            return Message.ok(Protocol.ADMIN_DELETE_SIZE, "Taille supprimee");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleGetSizes(Message req) {
        try {
            int produitId = Integer.parseInt(getData(req).trim());
            return Message.ok(Protocol.ADMIN_GET_SIZES, gson.toJson(sizeDAO.findByProduit(produitId)));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

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
            if (saved == null) {
                return Message.error("Erreur ajout guide");
            }
            return Message.ok(Protocol.ADMIN_ADD_GUIDE, gson.toJson(saved));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleDeleteGuide(Message req) {
        try {
            int guideId = Integer.parseInt(getData(req).trim());
            boolean ok = guideDAO.delete(guideId);
            if (!ok) {
                return Message.error("Guide introuvable");
            }
            return Message.ok(Protocol.ADMIN_DELETE_GUIDE, "Guide supprime");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleGetGuide(Message req) {
        try {
            int produitId = Integer.parseInt(getData(req).trim());
            return Message.ok(Protocol.ADMIN_GET_GUIDE, gson.toJson(guideDAO.findByProduit(produitId)));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleUpdateStock(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            int tailleId = Integer.parseInt(parts[0].trim());
            int newStock = Integer.parseInt(parts[1].trim());
            boolean ok = productService.updateStock(tailleId, newStock);
            if (!ok) {
                return Message.error("Taille introuvable");
            }
            notifyStockAlert(tailleId, newStock);
            return Message.ok(Protocol.ADMIN_UPDATE_STOCK, "Stock mis a jour : " + newStock);
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleGetUsers() {
        try {
            return Message.ok(Protocol.ADMIN_GET_USERS, gson.toJson(userService.getAllUsers()));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleSuspendUser(Message req) {
        try {
            int userId = Integer.parseInt(getData(req).trim());
            boolean ok = userService.suspendUser(userId);
            if (!ok) {
                return Message.error("Utilisateur introuvable");
            }
            notificationService.notifyClientAboutAdminAction(
                    userId,
                    "Votre compte a ete suspendu par l'administration."
            );
            return Message.ok(Protocol.ADMIN_SUSPEND_USER, "Utilisateur suspendu");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleActivateUser(Message req) {
        try {
            int userId = Integer.parseInt(getData(req).trim());
            boolean ok = userService.activateUser(userId);
            if (!ok) {
                return Message.error("Utilisateur introuvable");
            }
            notificationService.notifyClientAboutAdminAction(
                    userId,
                    "Votre compte a ete reactive par l'administration."
            );
            return Message.ok(Protocol.ADMIN_ACTIVATE_USER, "Utilisateur active");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

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
            if (!ok) {
                return Message.error("Utilisateur introuvable");
            }
            notificationService.notifyClientAboutAdminAction(
                    user.getId(),
                    "Vos informations de profil ont ete mises a jour par l'administration."
            );
            return Message.ok(Protocol.ADMIN_UPDATE_USER, "Utilisateur mis a jour");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleGetOrders() {
        try {
            return Message.ok(Protocol.ADMIN_GET_ORDERS, gson.toJson(orderService.getAllOrders()));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleGetOrderDetail(Message req) {
        try {
            int orderId = Integer.parseInt(getData(req).trim());
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return Message.error("Commande introuvable");
            }
            return Message.ok(Protocol.ADMIN_GET_ORDER_DETAIL, gson.toJson(order));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleUpdateOrderStatut(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            int orderId = Integer.parseInt(parts[0].trim());
            String newStatus = parts[1].trim();
            List<String> allowed = List.of("EN_ATTENTE", "VALIDEE", "EXPEDIEE", "LIVREE", "ANNULEE");
            if (!allowed.contains(newStatus)) {
                return Message.error("Statut invalide : " + newStatus);
            }

            boolean ok = orderService.updateStatut(orderId, newStatus);
            if (!ok) {
                return Message.error("Commande introuvable");
            }
            return Message.ok(Protocol.ADMIN_UPDATE_ORDER_STATUT, "Statut mis a jour : " + newStatus);
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleGetCancellationConfig() {
        try {
            return Message.ok(
                    Protocol.ADMIN_GET_CANCELLATION_CONFIG,
                    gson.toJson(CancellationConfigService.getConfig())
            );
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleUpdateCancellationConfig(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            if (parts.length < 4) {
                return Message.error("Format invalide");
            }

            String cancellableStatus = parts[0].trim();
            boolean reasonRequired = Boolean.parseBoolean(parts[1].trim());
            boolean automaticRefund = Boolean.parseBoolean(parts[2].trim());
            String estimatedDelay = parts[3].trim();

            CancellationConfig updated = CancellationConfigService.updateConfig(
                    cancellableStatus,
                    reasonRequired,
                    automaticRefund,
                    estimatedDelay
            );

            return Message.ok(Protocol.ADMIN_UPDATE_CANCELLATION_CONFIG, gson.toJson(updated));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleGetPayment(Message req) {
        try {
            int commandeId = Integer.parseInt(getData(req).trim());
            Payment payment = paymentService.getByCommande(commandeId);
            if (payment == null) {
                return Message.error("Paiement introuvable");
            }
            return Message.ok(Protocol.ADMIN_GET_PAYMENT, gson.toJson(payment));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleRembourse(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            int commandeId = Integer.parseInt(parts[0].trim());

            if (parts.length == 1) {
                boolean ok = paymentService.rembourser(commandeId);
                if (!ok) {
                    return Message.error("Paiement introuvable");
                }
                return Message.ok(Protocol.ADMIN_REMBOURSE, "Paiement rembourse");
            }

            String decision = parts[1].trim();
            boolean approved = "APPROVE".equalsIgnoreCase(decision);
            String detail = parts.length > 2 ? parts[2].trim() : "";
            String estimatedDelay = parts.length > 3 ? parts[3].trim() : "";

            boolean ok = paymentService.processRefundDecision(commandeId, approved, detail, estimatedDelay);
            if (!ok) {
                return Message.error("Impossible de traiter le remboursement");
            }
            return Message.ok(
                    Protocol.ADMIN_REMBOURSE,
                    approved ? "Remboursement confirme et email envoye" : "Remboursement refuse et email envoye"
            );
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleAddPromo(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            if (parts.length < 8) {
                return Message.error("Format invalide");
            }

            PromoCode promo = new PromoCode();
            promo.setCode(parts[0].trim().toUpperCase());
            promo.setReductionType(parts[1].trim());
            promo.setReductionValue(Double.parseDouble(parts[2].trim()));
            promo.setMinimumOrderAmount(Double.parseDouble(parts[3].trim()));
            promo.setStartDate(java.sql.Date.valueOf(parts[4].trim()));
            promo.setEndDate(java.sql.Date.valueOf(parts[5].trim()));
            promo.setMaxUses(Integer.parseInt(parts[6].trim()));
            promo.setMaxUsagePerUser(Integer.parseInt(parts[7].trim()));
            promo.setActive(true);

            if (promoService.existsByCode(promo.getCode())) {
                return Message.error("Ce code promo existe deja.");
            }
            if (promo.getEndDate().before(promo.getStartDate())) {
                return Message.error("La date d'expiration doit etre apres la date de debut.");
            }

            PromoCode saved = promoService.addPromo(promo);
            if (saved == null) {
                return Message.error("Impossible d'ajouter le code promo. Verifiez le code, les dates ou l'unicite.");
            }
            return Message.ok(Protocol.ADMIN_ADD_PROMO, gson.toJson(saved));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleGetPromos() {
        try {
            return Message.ok(Protocol.ADMIN_GET_PROMOS, gson.toJson(promoService.getPromos()));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleGetPromoStats(Message req) {
        try {
            PromoUsageStat stat = promoService.getPromoStats(getData(req).trim());
            if (stat == null) {
                return Message.error("Code promo introuvable");
            }
            return Message.ok(Protocol.ADMIN_GET_PROMO_STATS, gson.toJson(stat));
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private Message handleTogglePromo(Message req) {
        try {
            String[] parts = getData(req).split("\\|", -1);
            int promoId = Integer.parseInt(parts[0].trim());
            boolean active = Boolean.parseBoolean(parts[1].trim());
            boolean ok = promoService.togglePromo(promoId, active);
            if (!ok) {
                return Message.error("Code promo introuvable");
            }
            return Message.ok(Protocol.ADMIN_TOGGLE_PROMO, "Statut du code promo mis a jour");
        } catch (Exception e) {
            return Message.error("Erreur : " + e.getMessage());
        }
    }

    private void notifyStockAlert(int tailleId, int newStock) {
        if (newStock > 5) {
            return;
        }
        for (Product product : productService.getAllProducts()) {
            for (ProductSize size : product.getTailles()) {
                if (size.getId() == tailleId) {
                    String level = newStock == 0 ? "Rupture" : "Stock faible";
                    notificationService.notifyAdmins(
                            level + " sur " + product.getNom() + " (taille " + size.getValeur()
                                    + ") : reste " + newStock + " unite(s)."
                    );
                    return;
                }
            }
        }
    }
}
