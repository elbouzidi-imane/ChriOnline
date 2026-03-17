package com.chrionline.server.service;

import com.chrionline.server.model.User;
import com.chrionline.server.repository.UserDAO;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    // ── Inscription ───────────────────────────────────
    public User register(String nom, String prenom, String email,
                         String motDePasse, String telephone, String adresse) {

        // Vérifier que l'email n'existe pas déjà
        if (userDAO.emailExists(email)) {
            return null; // email déjà utilisé
        }

        // Hasher le mot de passe
        String hash = BCrypt.hashpw(motDePasse, BCrypt.gensalt());

        // Créer et sauvegarder l'utilisateur
        User user = new User(nom, prenom, email, hash, telephone, adresse);
        User saved = userDAO.save(user);

        // Ne jamais renvoyer le hash au client
        if (saved != null) saved.setMotDePasse(null);
        return saved;
    }

    // ── Connexion ─────────────────────────────────────
    public User login(String email, String motDePasse) {

        // Trouver l'utilisateur par email
        User user = userDAO.findByEmail(email);

        // Email inexistant
        if (user == null) return null;

        // Compte suspendu
        if (!user.isActif()) return null;

        // Vérifier le mot de passe avec BCrypt
        if (!BCrypt.checkpw(motDePasse, user.getMotDePasse())) return null;

        // Ne jamais renvoyer le hash au client
        user.setMotDePasse(null);
        return user;
    }

    // ── Récupérer un utilisateur par id ───────────────
    public User getUserById(int id) {
        User user = userDAO.findById(id);
        if (user != null) user.setMotDePasse(null);
        return user;
    }

    // ── Modifier le profil ────────────────────────────
    public boolean updateProfil(User user) {
        return userDAO.updateProfil(user);
    }

    // ── Changer le mot de passe ───────────────────────
    public boolean updatePassword(int id, String ancienMdp, String nouveauMdp) {
        User user = userDAO.findById(id);
        if (user == null) return false;
        if (!BCrypt.checkpw(ancienMdp, user.getMotDePasse())) return false;
        String newHash = BCrypt.hashpw(nouveauMdp, BCrypt.gensalt());
        return userDAO.updatePassword(id, newHash);
    }

    // ── Admin : tous les utilisateurs ─────────────────
    public java.util.List<User> getAllUsers() {
        java.util.List<User> users = userDAO.findAll();
        users.forEach(u -> u.setMotDePasse(null));
        return users;
    }

    // ── Admin : suspendre un utilisateur ─────────────
    public boolean suspendUser(int id) {
        return userDAO.updateStatut(id, "SUSPENDU");
    }

    // ── Admin : activer un utilisateur ───────────────
    public boolean activateUser(int id) {
        return userDAO.updateStatut(id, "ACTIF");
    }
}