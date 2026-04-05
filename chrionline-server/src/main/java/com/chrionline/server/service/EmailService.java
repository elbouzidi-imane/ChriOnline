package com.chrionline.server.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {

    private String from;
    private String password;
    private String host;
    private int port;

    private static EmailService instance;

    private EmailService() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("mail.properties")) {
            if (in == null) {
                throw new RuntimeException("mail.properties introuvable");
            }
            Properties p = new Properties();
            p.load(in);
            this.from = p.getProperty("mail.from");
            this.password = p.getProperty("mail.password");
            this.host = p.getProperty("mail.host");
            this.port = Integer.parseInt(p.getProperty("mail.port"));
            System.out.println("EmailService initialise : " + from);
        } catch (Exception e) {
            throw new RuntimeException("Erreur mail.properties : " + e.getMessage());
        }
    }

    public static EmailService getInstance() {
        if (instance == null) instance = new EmailService();
        return instance;
    }

    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public boolean sendEmail(String to, String subject, String body) {
        return sendTextEmail(to, subject, body);
    }

    public boolean sendTextEmail(String to, String subject, String body) {
        try {
            MimeMessage message = createMessage(to, subject);
            message.setText(body, StandardCharsets.UTF_8.name());
            Transport.send(message);
            System.out.println("Email texte envoye a : " + to + " | sujet=" + subject);
            return true;
        } catch (Exception e) {
            System.err.println("EmailService.sendTextEmail : " + e.getMessage());
            return false;
        }
    }

    public boolean sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = createMessage(to, subject);
            message.setContent(htmlBody, "text/html; charset=UTF-8");
            Transport.send(message);
            System.out.println("Email HTML envoye a : " + to + " | sujet=" + subject);
            return true;
        } catch (Exception e) {
            System.err.println("EmailService.sendHtmlEmail : " + e.getMessage());
            return false;
        }
    }

    public boolean sendConfirmationEmail(String to, String code) {
        String subject = "ChriOnline - Confirmez votre inscription";
        String body = """
                Bonjour,

                Merci de vous etre inscrit sur ChriOnline.

                Votre code de confirmation est : %s

                Ce code est valable pendant 10 minutes.

                Si vous n'avez pas cree de compte, ignorez cet email.

                L'equipe ChriOnline
                """.formatted(code);
        return sendTextEmail(to, subject, body);
    }

    public boolean sendResetPasswordEmail(String to, String code) {
        String subject = "ChriOnline - Reinitialisation de votre mot de passe";
        String body = """
                Bonjour,

                Vous avez demande a reinitialiser votre mot de passe.

                Votre code de verification est : %s

                Ce code est valable pendant 10 minutes.

                Si vous n'avez pas fait cette demande, ignorez cet email.

                L'equipe ChriOnline
                """.formatted(code);
        return sendTextEmail(to, subject, body);
    }

    public boolean sendLoginOtpEmail(String to, String code) {
        String subject = "ChriOnline - Code de connexion";
        String body = """
                Bonjour,

                Une tentative de connexion a ete validee avec votre mot de passe.

                Votre code OTP de connexion est : %s

                Ce code est valable pendant 10 minutes.

                Si vous n'etes pas a l'origine de cette connexion, changez votre mot de passe.

                L'equipe ChriOnline
                """.formatted(code);
        return sendTextEmail(to, subject, body);
    }

    public boolean sendOrderConfirmationEmail(String to, String nom,
                                              String reference, double total,
                                              boolean notificationsActivees) {
        String subject = "ChriOnline - Confirmation de votre commande";
        String statusBadge = notificationsActivees ? "Activees" : "Desactivees";
        String helperText = notificationsActivees
                ? "Vous recevrez un email a chaque changement de statut de votre commande."
                : "Activez les notifications depuis votre profil pour recevoir les mises a jour de statut.";

        String html = buildEmailLayout(
                "Paiement confirme",
                "Votre commande a bien ete enregistree et votre paiement est valide.",
                """
                <tr>
                  <td style="padding:0 0 14px 0;color:#3a4a44;font-size:15px;">Bonjour %s,</td>
                </tr>
                <tr>
                  <td style="padding:0 0 18px 0;color:#52606d;font-size:14px;line-height:1.7;">
                    Merci pour votre commande sur ChriOnline. Nous preparons maintenant votre colis.
                  </td>
                </tr>
                <tr>
                  <td style="padding:0 0 18px 0;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f8f3ec;border-radius:18px;">
                      <tr>
                        <td style="padding:16px 18px;color:#12372e;font-size:14px;">
                          <div style="padding-bottom:8px;"><strong>Reference :</strong> %s</div>
                          <div style="padding-bottom:8px;"><strong>Montant :</strong> %.2f DH</div>
                          <div><strong>Notifications :</strong> %s</div>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
                <tr>
                  <td style="padding:0 0 18px 0;color:#52606d;font-size:14px;line-height:1.7;">
                    %s
                  </td>
                </tr>
                """.formatted(escapeHtml(nom), escapeHtml(reference), total, escapeHtml(statusBadge), escapeHtml(helperText))
        );
        return sendHtmlEmail(to, subject, html);
    }

    public boolean sendOrderStatusEmail(String to, String nom,
                                        String reference, String statut) {
        String subject = "ChriOnline - Mise a jour de votre commande";
        String html = buildEmailLayout(
                "Statut de commande mis a jour",
                "Une action a ete effectuee sur votre commande.",
                """
                <tr>
                  <td style="padding:0 0 14px 0;color:#3a4a44;font-size:15px;">Bonjour %s,</td>
                </tr>
                <tr>
                  <td style="padding:0 0 18px 0;color:#52606d;font-size:14px;line-height:1.7;">
                    Le statut de votre commande a ete modifie par notre equipe.
                  </td>
                </tr>
                <tr>
                  <td style="padding:0 0 20px 0;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;background:#f8f3ec;border-radius:18px;">
                      <tr>
                        <td style="padding:16px 18px;color:#12372e;font-size:14px;">
                          <div style="padding-bottom:8px;"><strong>Reference :</strong> %s</div>
                          <div><strong>Nouveau statut :</strong> <span style="display:inline-block;background:#12372e;color:#ffffff;padding:6px 12px;border-radius:999px;font-size:12px;font-weight:bold;">%s</span></div>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
                <tr>
                  <td style="color:#52606d;font-size:14px;line-height:1.7;">
                    Vous pouvez consulter votre historique de commande dans l'application.
                  </td>
                </tr>
                """.formatted(escapeHtml(nom), escapeHtml(reference), escapeHtml(statut))
        );
        return sendHtmlEmail(to, subject, html);
    }

    private MimeMessage createMessage(String to, String subject) throws Exception {
        Session session = Session.getInstance(createSmtpProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from, "ChriOnline"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        return message;
    }

    private Properties createSmtpProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return props;
    }

    private String buildEmailLayout(String title, String subtitle, String content) {
        return """
                <!DOCTYPE html>
                <html lang="fr">
                <body style="margin:0;padding:0;background:#f6efe8;font-family:Segoe UI,Arial,sans-serif;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f6efe8;padding:24px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:640px;background:#fffaf5;border-radius:28px;overflow:hidden;border:1px solid #ecd8c5;">
                          <tr>
                            <td style="padding:28px 32px;background:linear-gradient(135deg,#12372e,#cf6f1e);color:#ffffff;">
                              <div style="font-size:12px;letter-spacing:1.8px;text-transform:uppercase;opacity:0.85;">ChriOnline</div>
                              <div style="padding-top:10px;font-size:28px;font-weight:700;line-height:1.2;">%s</div>
                              <div style="padding-top:8px;font-size:14px;line-height:1.6;opacity:0.92;">%s</div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:28px 32px;">%s</td>
                          </tr>
                          <tr>
                            <td style="padding:0 32px 28px 32px;color:#7b6d62;font-size:12px;line-height:1.7;">
                              Cet email a ete envoye automatiquement par ChriOnline.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(escapeHtml(title), escapeHtml(subtitle), content);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
