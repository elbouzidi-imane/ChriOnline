package com.chrionline.common;

public class Protocol {

    // ── Authentification ─────────────────────────────
    public static final String LOGIN    = "LOGIN";
    public static final String REGISTER = "REGISTER";
    public static final String LOGOUT   = "LOGOUT";
    // ── Authentification avancée ──────────────────────
    public static final String VERIFY_EMAIL       = "VERIFY_EMAIL";
    public static final String RESEND_OTP         = "RESEND_OTP";
    public static final String FORGOT_PASSWORD    = "FORGOT_PASSWORD";
    public static final String VERIFY_RESET_OTP   = "VERIFY_RESET_OTP";
    public static final String RESET_PASSWORD     = "RESET_PASSWORD";
    public static final String UPDATE_PROFILE     = "UPDATE_PROFILE";
    public static final String DEACTIVATE_ACCOUNT = "DEACTIVATE_ACCOUNT";
    public static final String DELETE_ACCOUNT     = "DELETE_ACCOUNT";
    // ── Produits ──────────────────────────────────────
    public static final String GET_PRODUCTS              = "GET_PRODUCTS";
    public static final String GET_PRODUCT               = "GET_PRODUCT";
    public static final String GET_CATEGORIES            = "GET_CATEGORIES";
    public static final String GET_PRODUCTS_BY_CATEGORIE = "GET_PRODUCTS_BY_CATEGORIE";

    // ── Panier ────────────────────────────────────────
    public static final String GET_CART         = "GET_CART";
    public static final String ADD_TO_CART      = "ADD_TO_CART";
    public static final String REMOVE_FROM_CART = "REMOVE_FROM_CART";
    public static final String UPDATE_CART      = "UPDATE_CART";
    public static final String CLEAR_CART       = "CLEAR_CART";

    // ── Commandes ─────────────────────────────────────
    public static final String PLACE_ORDER = "PLACE_ORDER";
    public static final String GET_ORDERS  = "GET_ORDERS";
    public static final String GET_ORDER   = "GET_ORDER";

    // ── Paiement ──────────────────────────────────────
    public static final String PAY = "PAY";

    // ── Admin — Produits ──────────────────────────────
    public static final String ADMIN_ADD_PRODUCT    = "ADMIN_ADD_PRODUCT";
    public static final String ADMIN_UPDATE_PRODUCT = "ADMIN_UPDATE_PRODUCT";
    public static final String ADMIN_DELETE_PRODUCT = "ADMIN_DELETE_PRODUCT";

    // ── Admin — Tailles produit ───────────────────────
    public static final String ADMIN_ADD_SIZE    = "ADMIN_ADD_SIZE";
    public static final String ADMIN_DELETE_SIZE = "ADMIN_DELETE_SIZE";
    public static final String ADMIN_GET_SIZES   = "ADMIN_GET_SIZES";

    // ── Admin — Guide taille ──────────────────────────
    public static final String ADMIN_ADD_GUIDE    = "ADMIN_ADD_GUIDE";
    public static final String ADMIN_DELETE_GUIDE = "ADMIN_DELETE_GUIDE";
    public static final String ADMIN_GET_GUIDE    = "ADMIN_GET_GUIDE";

    // ── Admin — Stock ─────────────────────────────────
    public static final String ADMIN_UPDATE_STOCK = "ADMIN_UPDATE_STOCK";

    // ── Admin — Utilisateurs ──────────────────────────
    public static final String ADMIN_GET_USERS     = "ADMIN_GET_USERS";
    public static final String ADMIN_SUSPEND_USER  = "ADMIN_SUSPEND_USER";
    public static final String ADMIN_ACTIVATE_USER = "ADMIN_ACTIVATE_USER";
    public static final String ADMIN_UPDATE_USER   = "ADMIN_UPDATE_USER";

    // ── Admin — Commandes ─────────────────────────────
    public static final String ADMIN_GET_ORDERS          = "ADMIN_GET_ORDERS";
    public static final String ADMIN_GET_ORDER_DETAIL    = "ADMIN_GET_ORDER_DETAIL";
    public static final String ADMIN_UPDATE_ORDER_STATUT = "ADMIN_UPDATE_ORDER_STATUT";

    // ── Admin — Paiement ──────────────────────────────
    public static final String ADMIN_GET_PAYMENT = "ADMIN_GET_PAYMENT";
    public static final String ADMIN_REMBOURSE   = "ADMIN_REMBOURSE";

    // ── Réponses serveur ──────────────────────────────
    public static final String OK    = "OK";
    public static final String ERROR = "ERROR";
    public static final String NOTIF = "NOTIF";
}