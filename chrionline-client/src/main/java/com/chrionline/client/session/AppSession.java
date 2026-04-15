package com.chrionline.client.session;

import com.chrionline.client.model.CartDTO;
import com.chrionline.client.model.UserDTO;

public final class AppSession {
    private static UserDTO currentUser;
    private static CartDTO currentCart;
    private static String sessionToken;

    private AppSession() {
    }

    public static UserDTO getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(UserDTO currentUser) {
        AppSession.currentUser = currentUser;
        sessionToken = currentUser == null ? null : currentUser.getSessionToken();
    }

    public static CartDTO getCurrentCart() {
        return currentCart;
    }

    public static void setCurrentCart(CartDTO currentCart) {
        AppSession.currentCart = currentCart;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static String getSessionToken() {
        return sessionToken;
    }

    public static void setSessionToken(String sessionToken) {
        AppSession.sessionToken = sessionToken;
        if (currentUser != null) {
            currentUser.setSessionToken(sessionToken);
        }
    }

    public static void clear() {
        currentUser = null;
        currentCart = null;
        sessionToken = null;
    }
}
