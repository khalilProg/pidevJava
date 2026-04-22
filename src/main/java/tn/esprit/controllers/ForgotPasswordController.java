package tn.esprit.controllers;

import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.json.JSONObject;

import tn.esprit.entities.User;
import tn.esprit.services.EmailService;
import tn.esprit.services.PasswordResetService;
import tn.esprit.services.UserService;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForgotPasswordController {

    @FXML private TextField emailF;
    @FXML private Label statusLabel;
    @FXML private WebView recaptchaWebView;
    @FXML private Button sendBtn;
    
    @FXML private VBox requestContainer;
    @FXML private VBox tokenContainer;
    @FXML private TextField tokenF;
    
    private static final String RECAPTCHA_SITE_KEY = "6LfQS2QsAAAAABev0u8G8-im2VSnyxKCZnOAZFIS";
    private static final String RECAPTCHA_SECRET_KEY = "6LfQS2QsAAAAAFYcEssB5EldRw6pBVQRxS7yFSnw";
    
    private final UserService userService = new UserService();
    private final PasswordResetService resetService = new PasswordResetService();
    private final EmailService emailService = new EmailService();
    
    private WebEngine webEngine;
    private int currentUserId = -1;

    @FXML
    public void initialize() {
        webEngine = recaptchaWebView.getEngine();
        
        String recaptchaHtml = "<!DOCTYPE html><html><body style='margin:0;padding:0;background:transparent;'>" +
            "<script src='https://www.google.com/recaptcha/api.js'></script>" +
            "<div class='g-recaptcha' data-sitekey='" + RECAPTCHA_SITE_KEY + "' data-theme='dark'></div>" +
            "</body></html>";
            
        webEngine.loadContent(recaptchaHtml);
    }

    @FXML
    void handleSendLink(ActionEvent event) {
        String email = emailF.getText();
        statusLabel.setVisible(false);
        statusLabel.setManaged(true);
        
        if (email == null || email.trim().isEmpty()) {
            displayError("Veuillez saisir votre adresse e-mail.");
            return;
        }

        String recaptchaResponse = (String) webEngine.executeScript("document.getElementById('g-recaptcha-response').value");
        
        if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
            displayError("Veuillez valider le reCAPTCHA.");
            return;
        }

        sendBtn.setDisable(true);
        displayMessage("Vérification en cours...", "#aaaaaa");

        new Thread(() -> {
            boolean isHuman = verifyRecaptchaServerSide(recaptchaResponse);
            
            javafx.application.Platform.runLater(() -> {
                if (!isHuman) {
                    displayError("La validation reCAPTCHA a échoué.");
                    sendBtn.setDisable(false);
                    return;
                }
                processPasswordReset(email);
            });
        }).start();
    }
    
    private boolean verifyRecaptchaServerSide(String responseToken) {
        try {
            URL url = new URL("https://www.google.com/recaptcha/api/siteverify");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            
            String postParams = "secret=" + RECAPTCHA_SECRET_KEY + "&response=" + responseToken;
            
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            JSONObject json = new JSONObject(response.toString());
            return json.getBoolean("success");
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void processPasswordReset(String email) {
        try {
            User matchingUser = null;
            for (User u : userService.recuperer()) {
                if (u.getEmail().equals(email)) {
                    matchingUser = u;
                    break;
                }
            }
            
            if (matchingUser == null) {
                displayError("Aucun compte trouvé avec cet e-mail.");
                sendBtn.setDisable(false);
                return;
            }
            
            this.currentUserId = matchingUser.getId();
            
            String resetToken = resetService.createToken(currentUserId);
            
            boolean emailSent = emailService.sendPasswordResetEmail(matchingUser.getEmail(), matchingUser.getPrenom() + " " + matchingUser.getNom(), resetToken);
            
            if (emailSent) {
                requestContainer.setVisible(false);
                requestContainer.setManaged(false);
                tokenContainer.setVisible(true);
                tokenContainer.setManaged(true);
                displayMessage("E-mail envoyé avec succès !", "#4caf50");
            } else {
                displayError("Erreur lors de l'envoi de l'e-mail. Veuillez réessayer plus tard.");
                sendBtn.setDisable(false);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            displayError("Erreur système: " + e.getMessage());
            sendBtn.setDisable(false);
        }
    }
    
    @FXML
    void handleVerifyToken(ActionEvent event) {
        String token = tokenF.getText();
        if (token == null || token.trim().isEmpty()) {
            displayError("Veuillez saisir le code reçu.");
            return;
        }
        
        try {
            int tokenId = resetService.findValidToken(token);
            if (tokenId == -1) {
                displayError("Ce code est invalide, expiré ou a déjà été utilisé.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reset_password.fxml"));
            Parent root = loader.load();
            
            ResetPasswordController controller = loader.getController();
            controller.initData(currentUserId, tokenId);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            
        } catch (Exception e) {
            e.printStackTrace();
            displayError("Erreur de vérification: " + e.getMessage());
        }
    }

    @FXML
    void handleBackToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BloodLink - Connexion");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void displayError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #e63939; -fx-font-weight: bold; -fx-font-size: 13px;");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
    
    private void displayMessage(String message, String colorHex) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold; -fx-font-size: 13px;");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
}
