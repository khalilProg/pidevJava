package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import org.mindrot.jbcrypt.BCrypt;

import tn.esprit.entities.User;
import tn.esprit.services.PasswordResetService;
import tn.esprit.services.UserService;

import java.io.IOException;
import java.util.List;

public class ResetPasswordController {

    @FXML private PasswordField passF;
    @FXML private PasswordField confirmPassF;
    @FXML private Label errorLabel;

    private int userId;
    private int tokenId;
    
    private final UserService userService = new UserService();
    private final PasswordResetService resetService = new PasswordResetService();

    // Data passed from ForgotPasswordController
    public void initData(int userId, int tokenId) {
        this.userId = userId;
        this.tokenId = tokenId;
    }

    @FXML
    void handleResetPassword(ActionEvent event) {
        String pass = passF.getText();
        String confirmPass = confirmPassF.getText();
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        if (pass == null || pass.trim().isEmpty() || confirmPass == null || confirmPass.trim().isEmpty()) {
            displayError("Veuillez remplir tous les champs.");
            return;
        }

        if (pass.length() < 6) {
            displayError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!pass.equals(confirmPass)) {
            displayError("Les mots de passe ne correspondent pas.");
            return;
        }

        try {
            // Find the user
            List<User> users = userService.recuperer();
            User matchingUser = null;
            for (User u : users) {
                if (u.getId() == this.userId) {
                    matchingUser = u;
                    break;
                }
            }

            if (matchingUser != null) {
                // Update password with BCrypt
                // Symfony typically uses $2y$, but jBCrypt outputs $2a$. We generate $2a$ and manual sync to $2y$ 
                // However, since login.java was already modified to handle $2a$ seamlessly, we can just save $2a$ directly.
                String hashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt());
                matchingUser.setPassword(hashedPassword);
                
                userService.modifier(matchingUser);
                
                // Mark token as used
                resetService.markTokenUsed(this.tokenId);

                // Redirect to login
                navigateToLogin(event);
            } else {
                displayError("Erreur : l'utilisateur n'existe plus.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayError("Erreur lors de la réinitialisation : " + e.getMessage());
        }
    }

    private void displayError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e63939; -fx-font-weight: bold; -fx-font-size: 13px;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void navigateToLogin(ActionEvent event) {
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
}
