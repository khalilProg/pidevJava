package tn.esprit.services;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {

    // Dummy email, the user needs to put their app password
    private static final String FROM_EMAIL = "khalilboujemaa2@gmail.com";
    private static final String APP_PASSWORD = "PUT_YOUR_APP_PASSWORD_HERE";

    public boolean sendPasswordResetEmail(String toEmail, String userName, String resetCode) {
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("BloodLink - Réinitialisation de mot de passe");

            // Calculate expiration specific to string rendering
            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; background-color: #0a0a0a; color: white; border-radius: 12px; overflow: hidden;'>"
                    + "<div style='background-color: #e63939; padding: 20px; text-align: center;'>"
                    + "  <h1 style='color: white; margin: 0;'>🩸 BLOODLINK</h1>"
                    + "  <p style='color: white; font-size: 14px;'>Réinitialisation de mot de passe</p>"
                    + "</div>"
                    + "<div style='padding: 30px;'>"
                    + "  <p style='color: white;'>Bonjour <strong>" + userName + "</strong>,</p>"
                    + "  <p style='color: #cccccc;'>Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte BloodLink. Veuillez copier le code ci-dessous pour définir un nouveau mot de passe.</p>"
                    + "  <div style='text-align: center; margin: 30px 0;'>"
                    + "    <div style='background-color: #e63939; color: white; padding: 15px 30px; border-radius: 8px; display: inline-block; font-weight: bold; font-size: 24px; letter-spacing: 2px;'>"
                    + resetCode
                    + "    </div>"
                    + "  </div>"
                    + "  <div style='background-color: #1a1a1a; padding: 15px; border-radius: 8px; font-size: 13px; color: #aaaaaa;'>"
                    + "    ⏳ <strong>Expiration:</strong> Ce code expirera dans 1 heure."
                    + "  </div>"
                    + "  <p style='color: #cccccc; font-size: 13px; margin-top: 20px;'>Si vous n'avez pas demandé cette réinitialisation, vous pouvez ignorer cet e-mail en toute sécurité. Votre mot de passe actuel restera inchangé.</p>"
                    + "</div>"
                    + "</div>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Normally this will fail without the actual APP_PASSWORD.
            if ("PUT_YOUR_APP_PASSWORD_HERE".equals(APP_PASSWORD)) {
                System.out.println("WARNING: Dummy Email Mode. The email below WOULD have been sent:");
                System.out.println("TO: " + toEmail);
                System.out.println("CODE: " + resetCode);
                // Return true to avoid blocking flow for the user while they test the UI
                return true;
            }

            Transport.send(message);
            System.out.println("Email sent successfully to " + toEmail);
            return true;
            
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Email could not be sent.");
            return false;
        }
    }
}
