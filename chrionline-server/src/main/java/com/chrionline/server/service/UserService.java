package com.chrionline.server.service;

import com.chrionline.server.model.User;
import com.chrionline.server.repository.UserDAO;
import org.mindrot.jbcrypt.BCrypt;
import com.chrionline.server.model.OtpCode;
import com.chrionline.server.repository.OtpDAO;
import java.util.Date;
public class UserService {

    private final UserDAO userDAO = new UserDAO();
    private final OtpDAO otpDAO = new OtpDAO();
    private final EmailService emailService = EmailService.getInstance();

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

    public User getUserByEmail(String email) {
        User user = userDAO.findByEmail(email);
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





    // ── Inscription avec OTP ──────────────────────────
    public String registerWithOtp(String nom, String prenom, String email,
                                  String motDePasse, String telephone,
                                  String adresse, String dateNaissance) {
        if (userDAO.emailExists(email)) {
            return "EMAIL_EXISTS";
        }

        // Stocker temporairement les infos en session OTP
        // Pour simplifier : on crée le compte avec statut INACTIF
        String hash = BCrypt.hashpw(motDePasse, BCrypt.gensalt());
        User user = new User(nom, prenom, email, hash, telephone, adresse);
        user.setStatut("INACTIF"); // inactif jusqu'à confirmation
        User saved = userDAO.save(user);
        if (saved == null) return "ERREUR";

        // Mettre à jour date naissance si fournie
        if (dateNaissance != null && !dateNaissance.isEmpty()) {
            try {
                java.sql.Date dn = java.sql.Date.valueOf(dateNaissance);
                userDAO.updateDateNaissance(saved.getId(), dn);
            } catch (Exception e) {
                System.err.println("Date naissance invalide : " + e.getMessage());
            }
        }

        // Générer et envoyer OTP
        String code = emailService.generateOtp();
        Date expireAt = new Date(System.currentTimeMillis() + 10 * 60 * 1000); // 10 min
        OtpCode otp = new OtpCode(email, code, "INSCRIPTION", expireAt);
        otpDAO.save(otp);
        emailService.sendConfirmationEmail(email, code);

        return "OTP_SENT";
    }

    // ── Vérifier OTP inscription ──────────────────────
    public boolean verifyEmail(String email, String code) {
        OtpCode otp = otpDAO.findValid(email, code, "INSCRIPTION");
        if (otp == null) return false;
        otpDAO.markAsUsed(otp.getId());
        // Activer le compte
        return userDAO.updateStatut(
                userDAO.findByEmail(email).getId(), "ACTIF");
    }

    // ── Renvoyer OTP ──────────────────────────────────
    public boolean resendOtp(String email, String type) {
        User user = userDAO.findByEmail(email);
        if (user == null) return false;
        String code = emailService.generateOtp();
        Date expireAt = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
        OtpCode otp = new OtpCode(email, code, type, expireAt);
        otpDAO.save(otp);
        if ("INSCRIPTION".equals(type)) {
            return emailService.sendConfirmationEmail(email, code);
        } else {
            return emailService.sendResetPasswordEmail(email, code);
        }
    }

    // ── Mot de passe oublié ───────────────────────────
    public boolean forgotPassword(String email) {
        User user = userDAO.findByEmail(email);
        if (user == null) return false;
        String code = emailService.generateOtp();
        Date expireAt = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
        OtpCode otp = new OtpCode(email, code, "MOT_DE_PASSE", expireAt);
        otpDAO.save(otp);
        return emailService.sendResetPasswordEmail(email, code);
    }

    // ── Vérifier OTP reset password ───────────────────
    public boolean verifyResetOtp(String email, String code) {
        OtpCode otp = otpDAO.findValid(email, code, "MOT_DE_PASSE");
        if (otp == null) return false;
        otpDAO.markAsUsed(otp.getId());
        return true;
    }

    // ── Réinitialiser le mot de passe ─────────────────
    public boolean resetPassword(String email, String nouveauMdp) {
        User user = userDAO.findByEmail(email);
        if (user == null) return false;
        String hash = BCrypt.hashpw(nouveauMdp, BCrypt.gensalt());
        return userDAO.updatePassword(user.getId(), hash);
    }

    // ── Modifier le profil complet ────────────────────
    public boolean updateProfile(int id, String nom, String prenom,
                                 String telephone, String adresse,
                                 String dateNaissance) {
        User user = new User();
        user.setId(id);
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setTelephone(telephone);
        user.setAdresse(adresse);
        boolean ok = userDAO.updateProfil(user);
        if (ok && dateNaissance != null && !dateNaissance.isEmpty()) {
            try {
                java.sql.Date dn = java.sql.Date.valueOf(dateNaissance);
                userDAO.updateDateNaissance(id, dn);
            } catch (Exception e) {
                System.err.println("Date naissance invalide : " + e.getMessage());
            }
        }
        return ok;
    }

    public boolean updateNotificationPreference(int id, boolean notificationsActivees) {
        return userDAO.updateNotificationPreference(id, notificationsActivees);
    }

    // ── Désactiver le compte (soft delete) ────────────
    public boolean deactivateAccount(int id) {
        return userDAO.updateStatut(id, "SUSPENDU");
    }

    // ── Supprimer définitivement le compte ────────────
    public boolean deleteAccount(int id) {
        return userDAO.delete(id);
    }
}
