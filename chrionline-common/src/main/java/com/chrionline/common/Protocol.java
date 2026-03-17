package com.chrionline.common;

public class Protocol {

    // ── Authentification ─────────────────────────────
    public static final String LOGIN    = "LOGIN";
    public static final String REGISTER = "REGISTER";
    public static final String LOGOUT   = "LOGOUT";

    // ── Produits ──────────────────────────────────────
    public static final String GET_PRODUCTS   = "GET_PRODUCTS";
    public static final String GET_PRODUCT    = "GET_PRODUCT";
    public static final String GET_CATEGORIES = "GET_CATEGORIES";

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

    // ── Admin ─────────────────────────────────────────
    public static final String ADMIN_ADD_PRODUCT    = "ADMIN_ADD_PRODUCT";
    public static final String ADMIN_UPDATE_PRODUCT = "ADMIN_UPDATE_PRODUCT";
    public static final String ADMIN_DELETE_PRODUCT = "ADMIN_DELETE_PRODUCT";
    public static final String ADMIN_GET_USERS      = "ADMIN_GET_USERS";
    public static final String ADMIN_GET_ORDERS     = "ADMIN_GET_ORDERS";
    public static final String ADMIN_UPDATE_STOCK   = "ADMIN_UPDATE_STOCK";
    public static final String ADMIN_SUSPEND_USER   = "ADMIN_SUSPEND_USER";
    public static final String ADMIN_ACTIVATE_USER  = "ADMIN_ACTIVATE_USER";

    // ── Réponses serveur ──────────────────────────────
    public static final String OK    = "OK";
    public static final String ERROR = "ERROR";
    public static final String NOTIF = "NOTIF";
}