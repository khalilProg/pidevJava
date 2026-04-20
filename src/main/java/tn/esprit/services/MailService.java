package tn.esprit.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class MailService {

    private static final String EMAIL    = tn.esprit.controllers.ConfigLoader.get("mail.sender.email");
    private static final String PASSWORD = tn.esprit.controllers.ConfigLoader.get("mail.sender.password");
    private final Session SESSION;

    public MailService() {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        SESSION = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });
    }

    // Build HTML template for email
    private String buildTemplate(String patientName, String campagne, LocalDateTime dateTime, String entite) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return "<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Confirmation BloodLink</title></head>"
                + "<body style='margin:0;padding:0;background:#F5F5F5;font-family:Arial,sans-serif;'>"

                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#F5F5F5;padding:32px 0;'><tr><td align='center'>"
                + "<table width='560' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e5e7eb;'>"

                // Header
                + "<tr><td style='background:#C0242A;padding:32px 32px 24px;text-align:center;'>"
                + "<table cellpadding='0' cellspacing='0' style='margin:0 auto 6px;'><tr>"
                + "<td style='padding-right:10px;vertical-align:middle;'>"
                + "<svg width='26' height='26' viewBox='0 0 24 24' xmlns='http://www.w3.org/2000/svg'>"
                + "<path fill='white' d='M12 2C12 2 4 9.5 4 14.5C4 18.64 7.58 22 12 22C16.42 22 20 18.64 20 14.5C20 9.5 12 2Z'/>"
                + "</svg></td>"
                + "<td style='color:white;font-size:22px;font-weight:600;letter-spacing:0.5px;vertical-align:middle;'>BloodLink</td>"
                + "</tr></table>"
                + "<p style='color:rgba(255,255,255,0.75);font-size:13px;margin:0;'>Plateforme nationale de don de sang</p>"
                + "</td></tr>"

                // Body
                + "<tr><td style='padding:32px;'>"
                + "<p style='font-size:15px;color:#111827;margin:0 0 6px;'>Bonjour <strong>" + patientName + "</strong>,</p>"
                + "<p style='font-size:14px;color:#6B7280;margin:0 0 24px;line-height:1.6;'>Votre rendez-vous a été confirmé. Retrouvez les détails ci-dessous.</p>"

                // Details card
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#F9FAFB;border-radius:8px;border-left:3px solid #C0242A;margin-bottom:20px;'>"
                + "<tr><td style='padding:20px;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0'>"

                + "<tr><td style='font-size:13px;color:#6B7280;padding-bottom:10px;border-bottom:1px solid #E5E7EB;'>Campagne</td>"
                + "<td style='font-size:13px;font-weight:600;color:#111827;text-align:right;padding-bottom:10px;border-bottom:1px solid #E5E7EB;'>" + campagne + "</td></tr>"

                + "<tr><td style='font-size:13px;color:#6B7280;padding:10px 0;border-bottom:1px solid #E5E7EB;'>Date &amp; heure</td>"
                + "<td style='font-size:13px;font-weight:600;color:#111827;text-align:right;padding:10px 0;border-bottom:1px solid #E5E7EB;'>" + dateTime.format(formatter) + "</td></tr>"

                + "<tr><td style='font-size:13px;color:#6B7280;padding-top:10px;'>Entité de collecte</td>"
                + "<td style='font-size:13px;font-weight:600;color:#111827;text-align:right;padding-top:10px;'>" + entite + "</td></tr>"

                + "</table></td></tr></table>"

                // Reminder box
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background:#FEF2F2;border-radius:8px;border:1px solid #FECACA;margin-bottom:24px;'>"
                + "<tr><td style='padding:16px 20px;font-size:13px;color:#991B1B;line-height:1.6;'>"
                + "<strong>À apporter :</strong> votre carte d'identité."
                + "</td></tr></table>"

                + "<p style='font-size:14px;color:#6B7280;line-height:1.7;margin:0;'>"
                + "Merci pour votre générosité. Chaque don peut sauver jusqu'à <strong style='color:#111827;'>trois vies</strong>.</p>"
                + "</td></tr>"

                // Footer
                + "<tr><td style='border-top:1px solid #E5E7EB;padding:20px 32px;text-align:center;'>"
                + "<p style='font-size:12px;color:#9CA3AF;margin:0;line-height:1.6;'>"
                + "Vous recevez cet email car vous êtes inscrit sur <strong>BloodLink</strong>.<br>"
                + "&copy; 2026 BloodLink — Tous droits réservés."
                + "</p></td></tr>"

                + "</table></td></tr></table>"
                + "</body></html>";
    }

    // Send confirmation email
    public boolean sendConfirmation(String toEmail, String patientName, String campagne,
                                    LocalDateTime dateTime, String entiteDeCollecte) {
        try {
            Message msg = new MimeMessage(SESSION);
            msg.setFrom(new InternetAddress(EMAIL, "BloodLink"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("Confirmation de votre Rendez-Vous — " + campagne);
            msg.setContent(buildTemplate(patientName, campagne, dateTime, entiteDeCollecte),
                    "text/html; charset=utf-8");

            Transport.send(msg);
            System.out.println("Email sent to " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
            return false;
        }
    }
}