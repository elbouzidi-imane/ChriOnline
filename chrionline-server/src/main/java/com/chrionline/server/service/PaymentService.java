package com.chrionline.server.service;

import com.chrionline.server.model.Order;
import com.chrionline.server.model.Payment;
import com.chrionline.server.model.User;
import com.chrionline.server.repository.OrderDAO;
import com.chrionline.server.repository.PaymentDAO;
import com.chrionline.server.repository.UserDAO;

public class PaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = EmailService.getInstance();
    private final NotificationService notificationService = new NotificationService();

    public Payment simulerPaiement(int commandeId, double montant, String modePaiement) {
        Payment payment = new Payment(commandeId, montant, modePaiement);
        payment.setStatut("VALIDE");
        payment.setReference("TXN-" + System.currentTimeMillis());
        return paymentDAO.save(payment);
    }

    public Payment getByCommande(int commandeId) {
        return paymentDAO.findByCommande(commandeId);
    }

    public java.util.List<Payment> getAll() {
        return paymentDAO.findAll();
    }

    public boolean rembourser(int commandeId) {
        Payment payment = paymentDAO.findByCommande(commandeId);
        if (payment == null) {
            return false;
        }
        return paymentDAO.updateStatut(commandeId, "REMBOURSE");
    }

    public boolean isPaye(int commandeId) {
        Payment payment = paymentDAO.findByCommande(commandeId);
        return payment != null && payment.isValide();
    }

    public boolean processRefundDecision(int commandeId, boolean approved, String detail, String estimatedDelay) {
        Payment payment = paymentDAO.findByCommande(commandeId);
        if (payment == null) {
            return false;
        }

        Order order = orderDAO.findById(commandeId);
        if (order == null) {
            return false;
        }

        User user = userDAO.findById(order.getUtilisateurId());
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return false;
        }

        boolean updated = paymentDAO.updateStatut(commandeId, approved ? "REMBOURSE" : "REFUSE");
        if (!updated) {
            return false;
        }

        String subject;
        String body;
        if (approved) {
            String delay = estimatedDelay == null || estimatedDelay.isBlank()
                    ? "3 a 5 jours ouvrables"
                    : estimatedDelay.trim();
            String extra = detail == null || detail.isBlank() ? "" : "\nMessage admin : " + detail.trim() + "\n";
            subject = "ChriOnline - Remboursement confirme";
            body = """
                    Bonjour %s,

                    Votre demande d'annulation pour la commande %s a ete acceptee.

                    Montant rembourse : %s DH
                    Delai estime : %s
                    %s
                    L'equipe ChriOnline
                    """.formatted(
                    user.getPrenom() == null || user.getPrenom().isBlank() ? "client" : user.getPrenom(),
                    order.getReference(),
                    String.format("%.2f", payment.getMontant()),
                    delay,
                    extra
            );
        } else {
            subject = "ChriOnline - Remboursement impossible";
            body = """
                    Bonjour %s,

                    Votre demande de remboursement pour la commande %s n'a pas pu etre traitee.

                    Motif : %s

                    Pour toute question, contactez l'administration ChriOnline.

                    L'equipe ChriOnline
                    """.formatted(
                    user.getPrenom() == null || user.getPrenom().isBlank() ? "client" : user.getPrenom(),
                    order.getReference(),
                    detail == null || detail.isBlank() ? "Motif non precise." : detail.trim()
            );
        }

        boolean sent = emailService.sendEmail(user.getEmail(), subject, body);
        notificationService.notifyClientAboutAdminAction(
                user.getId(),
                approved
                        ? "Votre remboursement pour la commande " + order.getReference() + " a ete valide."
                        : "Votre remboursement pour la commande " + order.getReference() + " a ete refuse."
        );
        return sent;
    }
}
