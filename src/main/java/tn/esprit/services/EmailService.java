package tn.esprit.services;

import tn.esprit.entities.Commande;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {

    private static final String DEFAULT_SMTP_HOST = "smtp.gmail.com";
    private static final String DEFAULT_SMTP_PORT = "587";
    private static final String DEFAULT_FROM_EMAIL = "khalilboujemaa2@gmail.com";
    private static final Properties LOCAL_CONFIG = loadLocalConfig();

    public boolean sendPasswordResetEmail(String toEmail, String userName, String resetCode) {
        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; background-color: #0a0a0a; color: white; border-radius: 12px; overflow: hidden;'>"
                + "<div style='background-color: #e63939; padding: 20px; text-align: center;'>"
                + "  <h1 style='color: white; margin: 0;'>BLOODLINK</h1>"
                + "  <p style='color: white; font-size: 14px;'>Reinitialisation de mot de passe</p>"
                + "</div>"
                + "<div style='padding: 30px;'>"
                + "  <p style='color: white;'>Bonjour <strong>" + escapeHtml(userName) + "</strong>,</p>"
                + "  <p style='color: #cccccc;'>Nous avons recu une demande de reinitialisation de mot de passe pour votre compte BloodLink. Veuillez copier le code ci-dessous pour definir un nouveau mot de passe.</p>"
                + "  <div style='text-align: center; margin: 30px 0;'>"
                + "    <div style='background-color: #e63939; color: white; padding: 15px 30px; border-radius: 8px; display: inline-block; font-weight: bold; font-size: 24px; letter-spacing: 2px;'>"
                + escapeHtml(resetCode)
                + "    </div>"
                + "  </div>"
                + "  <div style='background-color: #1a1a1a; padding: 15px; border-radius: 8px; font-size: 13px; color: #aaaaaa;'>"
                + "    <strong>Expiration:</strong> Ce code expirera dans 1 heure."
                + "  </div>"
                + "  <p style='color: #cccccc; font-size: 13px; margin-top: 20px;'>Si vous n'avez pas demande cette reinitialisation, vous pouvez ignorer cet e-mail en toute securite. Votre mot de passe actuel restera inchange.</p>"
                + "</div>"
                + "</div>";

        return sendHtmlEmail(toEmail, "BloodLink - Reinitialisation de mot de passe", htmlContent,
                "CODE: " + resetCode);
    }

    public boolean sendCommandeCreatedEmail(String toEmail, String clientName, Commande commande, String banqueName) {
        String htmlContent = buildCommandeCreatedHtml(clientName, commande, banqueName);
        String debug = "COMMANDE: #" + commande.getId()
                + "\nREFERENCE: " + commande.getReference()
                + "\nTYPE SANG: " + commande.getTypeSang()
                + "\nQUANTITE: " + commande.getQuantite()
                + "\nBANQUE: " + banqueName;

        return sendHtmlEmail(toEmail, "BloodLink - Commande creee", htmlContent, debug);
    }

    private boolean sendHtmlEmail(String toEmail, String subject, String htmlContent, String debugDetails) {
        if (toEmail == null || toEmail.isBlank()) {
            System.err.println("Email could not be sent: empty recipient.");
            return false;
        }

        String smtpUser = getConfig("BLOODLINK_SMTP_USER", "bloodlink.smtp.user", getFromEmail());
        String smtpPassword = getConfig("BLOODLINK_SMTP_PASSWORD", "bloodlink.smtp.password", "");
        if (smtpPassword.isBlank()) {
            System.err.println("Email could not be sent: missing BLOODLINK_SMTP_PASSWORD or -Dbloodlink.smtp.password.");
            return false;
        }

        Session session = Session.getInstance(getSmtpProperties(), new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(getFromEmail()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email sent successfully to " + toEmail);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Email could not be sent.");
            return false;
        }
    }

    private Properties getSmtpProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", getConfig("BLOODLINK_SMTP_HOST", "bloodlink.smtp.host", DEFAULT_SMTP_HOST));
        props.put("mail.smtp.port", getConfig("BLOODLINK_SMTP_PORT", "bloodlink.smtp.port", DEFAULT_SMTP_PORT));
        props.put("mail.smtp.ssl.trust", getConfig("BLOODLINK_SMTP_HOST", "bloodlink.smtp.host", DEFAULT_SMTP_HOST));
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return props;
    }

    private String getFromEmail() {
        return getConfig("BLOODLINK_SMTP_FROM", "bloodlink.smtp.from", DEFAULT_FROM_EMAIL);
    }

    private String getConfig(String envName, String propertyName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        String localValue = LOCAL_CONFIG.getProperty(propertyName);
        if (localValue != null && !localValue.isBlank()) {
            return localValue.trim();
        }

        return defaultValue;
    }

    private static Properties loadLocalConfig() {
        Properties properties = new Properties();
        Path localPath = Path.of("email.properties");

        if (Files.exists(localPath)) {
            try (InputStream input = Files.newInputStream(localPath)) {
                properties.load(input);
                return properties;
            } catch (IOException e) {
                System.err.println("Could not read email.properties: " + e.getMessage());
            }
        }

        try (InputStream input = EmailService.class.getResourceAsStream("/email.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Could not read classpath email.properties: " + e.getMessage());
        }

        return properties;
    }

    private String buildCommandeCreatedHtml(String clientName, Commande commande, String banqueName) {
        String safeClientName = escapeHtml(clientName == null || clientName.isBlank() ? "Client" : clientName);
        String safeStatus = escapeHtml(commande.getStatus() == null || commande.getStatus().isBlank() ? "En attente" : commande.getStatus());
        String safeTypeSang = escapeHtml(commande.getTypeSang() == null || commande.getTypeSang().isBlank() ? "-" : commande.getTypeSang());
        String safeBanqueName = escapeHtml(banqueName == null || banqueName.isBlank() ? "-" : banqueName);

        return "<!doctype html>"
                + "<html lang='fr'><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<title>Commande creee</title></head>"
                + "<body style='margin:0;padding:0;background:#0b0f14;font-family:Arial,Helvetica,sans-serif;color:#ffffff;'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background:#0b0f14;padding:32px 12px;'><tr><td align='center'>"
                + "<table role='presentation' width='640' cellpadding='0' cellspacing='0' style='width:640px;max-width:640px;'>"
                + "<tr><td style='padding:0 0 14px 0;'>"
                + "<div style='font-weight:900;letter-spacing:-.5px;text-transform:uppercase;font-size:22px;line-height:1.2;'>Commande creee</div>"
                + "<div style='margin-top:6px;color:rgba(255,255,255,.65);font-size:14px;line-height:1.5;'>Votre demande a bien ete enregistree. Vous pouvez suivre son statut a tout moment.</div>"
                + "</td></tr>"
                + "<tr><td style='background:rgba(255,255,255,.04);border:1px solid rgba(255,255,255,.12);border-radius:18px;padding:22px;'>"
                + "<div style='display:inline-block;padding:7px 12px;border-radius:999px;background:rgba(230,57,57,.15);border:1px solid rgba(230,57,57,.45);font-weight:800;font-size:12px;letter-spacing:.06em;text-transform:uppercase;margin-bottom:16px;'>"
                + safeStatus + "</div>"
                + "<div style='font-size:16px;line-height:1.65;margin:0 0 12px 0;'>Bonjour <span style='font-weight:800;'>" + safeClientName + "</span>,</div>"
                + "<div style='font-size:14px;line-height:1.7;color:rgba(255,255,255,.85);margin-bottom:14px;'>Nous confirmons la creation de votre commande <span style='font-weight:800;'>#" + commande.getId() + "</span>.</div>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='border-collapse:separate;border-spacing:0 10px;'>"
                + infoRow("Type sang", safeTypeSang)
                + infoRow("Quantite", String.valueOf(commande.getQuantite()))
                + infoRow("Banque", safeBanqueName)
                + "</table>"
                + "<table role='presentation' cellpadding='0' cellspacing='0' style='margin-top:18px;'><tr><td>"
                + "<a href='#' style='display:inline-block;text-decoration:none;border-radius:999px;padding:12px 18px;background:rgba(230,57,57,.15);border:1px solid rgba(230,57,57,.45);color:#ffffff;font-weight:800;'>Suivre ma commande</a>"
                + "</td><td style='width:10px;'></td><td>"
                + "<a href='#' style='display:inline-block;text-decoration:none;border-radius:999px;padding:12px 18px;background:transparent;border:1px solid rgba(255,255,255,.18);color:#ffffff;font-weight:800;'>Support</a>"
                + "</td></tr></table>"
                + "</td></tr>"
                + "<tr><td style='padding:14px 2px 0 2px;color:rgba(255,255,255,.55);font-size:12px;line-height:1.6;'>"
                + "Si vous n'etes pas a l'origine de cette demande, ignorez cet email.<br>&copy; "
                + Year.now().getValue() + " CRTS - Notifications automatiques</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private String infoRow(String label, String value) {
        return "<tr><td style='width:38%;color:rgba(255,255,255,.65);font-size:12px;font-weight:800;letter-spacing:.08em;text-transform:uppercase;'>"
                + escapeHtml(label)
                + "</td><td style='font-size:14px;color:#fff;font-weight:700;'>"
                + value
                + "</td></tr>";
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
