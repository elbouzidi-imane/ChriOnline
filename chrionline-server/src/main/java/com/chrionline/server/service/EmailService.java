package com.chrionline.server.service;

import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

    private String from;
    private String password;
    private String host;
    private int    port;

    private static EmailService instance;

    private EmailService() {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("mail.properties")) {
            if (in == null)
                throw new RuntimeException("mail.properties introuvable !");
            Properties p = new Properties();
            p.load(in);
            this.from     = p.getProperty("mail.from");
            this.password = p.getProperty("mail.password");
            this.host     = p.getProperty("mail.host");
            this.port     = Integer.parseInt(p.getProperty("mail.port"));
            System.out.println("EmailService initialisé : " + from);
        } catch (Exception e) {
            throw new RuntimeException("Erreur mail.properties : " + e.getMessage());
        }
    }

    public static EmailService getInstance() {
        if (instance == null) instance = new EmailService();
        return instance;
    }

    // ── Générer un code OTP à 6 chiffres ─────────────
    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    // ── Envoyer un email ──────────────────────────────
    public boolean sendEmail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            System.out.println("Email envoyé à : " + to);
            return true;
        } catch (Exception e) {
            System.err.println("EmailService.sendEmail : " + e.getMessage());
            return false;
        }
    }

    // ── Email confirmation inscription ────────────────
    public boolean sendConfirmationEmail(String to, String code) {
        String subject = "ChriOnline — Confirmez votre inscription";
        String body = """
                Bonjour,
                
                Merci de vous être inscrit sur ChriOnline !
                
                Votre code de confirmation est : %s
                
                Ce code est valable pendant 10 minutes.
                
                Si vous n'avez pas créé de compte, ignorez cet email.
                
                L'équipe ChriOnline
                """.formatted(code);
        return sendEmail(to, subject, body);
    }

    // ── Email réinitialisation mot de passe ───────────
    public boolean sendResetPasswordEmail(String to, String code) {
        String subject = "ChriOnline — Réinitialisation de votre mot de passe";
        String body = """
                Bonjour,
                
                Vous avez demandé à réinitialiser votre mot de passe.
                
                Votre code de vérification est : %s
                
                Ce code est valable pendant 10 minutes.
                
                Si vous n'avez pas fait cette demande, ignorez cet email.
                
                L'équipe ChriOnline
                """.formatted(code);
        return sendEmail(to, subject, body);
    }
}