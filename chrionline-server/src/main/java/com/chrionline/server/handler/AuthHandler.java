package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.User;
import com.chrionline.server.service.UserService;
import com.google.gson.Gson;

public class AuthHandler {

    private final UserService userService = new UserService();
    private final Gson gson = new Gson();

    public Message handle(Message request) {
        return switch (request.getType()) {
            case Protocol.LOGIN    -> handleLogin(request);
            case Protocol.REGISTER -> handleRegister(request);
            case Protocol.LOGOUT   -> handleLogout(request);
            default -> Message.error("Type non géré par AuthHandler");
        };
    }

    // ── LOGIN ─────────────────────────────────────────
    private Message handleLogin(Message req) {
        // payload format : "email:motdepasse"
        String payload = req.getPayload();
        if (payload == null || !payload.contains(":")) {
            return Message.error("Format invalide. Attendu : email:motdepasse");
        }

        int separateur = payload.indexOf(":");
        String email   = payload.substring(0, separateur).trim();
        String mdp     = payload.substring(separateur + 1).trim();

        if (email.isEmpty() || mdp.isEmpty()) {
            return Message.error("Email et mot de passe obligatoires");
        }

        User user = userService.login(email, mdp);

        if (user == null) {
            return Message.error("Identifiants incorrects ou compte suspendu");
        }

        // Retourner les infos essentielles en JSON
        String json = gson.toJson(user);
        return Message.ok(Protocol.LOGIN, json);
    }

    // ── REGISTER ──────────────────────────────────────
    private Message handleRegister(Message req) {
        // payload format : "nom|prenom|email|motdepasse|telephone|adresse"
        String payload = req.getPayload();
        if (payload == null) {
            return Message.error("Données d'inscription manquantes");
        }

        String[] parts = payload.split("\\|", -1);
        if (parts.length < 4) {
            return Message.error("Format invalide. Attendu : nom|prenom|email|mdp|tel|adresse");
        }

        String nom       = parts[0].trim();
        String prenom    = parts[1].trim();
        String email     = parts[2].trim();
        String mdp       = parts[3].trim();
        String telephone = parts.length > 4 ? parts[4].trim() : "";
        String adresse   = parts.length > 5 ? parts[5].trim() : "";

        // Validations basiques
        if (nom.isEmpty() || prenom.isEmpty()) {
            return Message.error("Nom et prénom obligatoires");
        }
        if (email.isEmpty() || !email.contains("@")) {
            return Message.error("Email invalide");
        }
        if (mdp.length() < 4) {
            return Message.error("Mot de passe trop court (minimum 4 caractères)");
        }

        User user = userService.register(nom, prenom, email, mdp, telephone, adresse);

        if (user == null) {
            return Message.error("Email déjà utilisé");
        }

        String json = gson.toJson(user);
        return Message.ok(Protocol.REGISTER, json);
    }

    // ── LOGOUT ────────────────────────────────────────
    private Message handleLogout(Message req) {
        // Pas de session côté serveur à invalider dans ce projet
        // Le client efface simplement ses données locales
        return Message.ok(Protocol.LOGOUT, "Déconnexion réussie");
    }
    
}