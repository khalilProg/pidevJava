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

        SESSION = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });
    }

    // Build HTML template for email
    private String buildTemplate(String patientName, String campagne, LocalDateTime dateTime, String entite) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return "<html><body>"
                + "<h2>Bonjour " + patientName + "</h2>"
                + "<p>Votre rendez-vous pour la campagne <b>" + campagne + "</b> a été confirmé.</p>"
                + "<p>Date & Heure : " + dateTime.format(formatter) + "</p>"
                + "<p>Entité de collecte : " + entite + "</p>"
                + "<br/><p>Merci pour votre participation !</p>"
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