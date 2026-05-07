package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import tn.esprit.entities.User;
import tn.esprit.services.EmailService;
import tn.esprit.services.PasswordResetService;
import tn.esprit.services.UserService;

import java.io.IOException;

public class ForgotPasswordController {

    @FXML private TextField emailF;
    @FXML private Label statusLabel;
    @FXML private Button sendBtn;
    
    @FXML private VBox requestContainer;
    @FXML private VBox tokenContainer;
    @FXML private TextField tokenF;
    
    private final UserService userService = new UserService();
    private final PasswordResetService resetService = new PasswordResetService();
    private final EmailService emailService = new EmailService();
    
    private int currentUserId = -1;

    @FXML
    public void initialize() {
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
        sendBtn.setDisable(true);
        processPasswordReset(email);
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
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            
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
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
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
