package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import tn.esprit.entities.User;
import tn.esprit.services.UserService;

public class register {

    @FXML
    private TextField prenomF;
    @FXML
    private TextField nomF;
    @FXML
    private TextField emailF;
    @FXML
    private TextField telF;
    @FXML
    private PasswordField passF;

    @FXML
    private Label prenomError;
    @FXML
    private Label nomError;
    @FXML
    private Label emailError;
    @FXML
    private Label telError;
    @FXML
    private Label passError;

    @FXML
    private Label googleLoadingLabel;

    @FXML
    void handleRegister(ActionEvent event) {
        clearErrors();

        String prenom = prenomF.getText().trim();
        String nom = nomF.getText().trim();
        String email = emailF.getText().trim();
        String tel = telF.getText().trim();
        String password = passF.getText();

        boolean valid = true;

        if (prenom.isEmpty()) {
            showFieldError(prenomError, "Le prénom est obligatoire.");
            valid = false;
        }

        if (nom.isEmpty()) {
            showFieldError(nomError, "Le nom est obligatoire.");
            valid = false;
        }

        if (email.isEmpty()) {
            showFieldError(emailError, "L'email est obligatoire.");
            valid = false;
        } else if (!email.contains("@") || !email.contains(".")) {
            showFieldError(emailError, "Format d'email invalide.");
            valid = false;
        }

        if (!tel.isEmpty()) {
            String digitsOnly = tel.replaceAll("[^0-9]", "");
            if (digitsOnly.length() < 8) {
                showFieldError(telError, "Le numéro doit contenir au moins 8 chiffres.");
                valid = false;
            }
        }

        if (password.isEmpty()) {
            showFieldError(passError, "Le mot de passe est obligatoire.");
            valid = false;
        } else if (password.length() < 6) {
            showFieldError(passError, "Le mot de passe doit contenir au moins 6 caractères.");
            valid = false;
        }

        if (!valid) {
            return;
        }

        User newUser = new User(email, nom, prenom, password, "client", tel);
        UserService userService = new UserService();

        try {
            userService.ajouter(newUser);
            handleGoToLogin(event);
        } catch (Exception e) {
            e.printStackTrace();
            showFieldError(emailError, "Erreur lors de l'inscription: " + e.getMessage());
        }
    }

    private void showFieldError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearErrors() {
        Label[] errors = { prenomError, nomError, emailError, telError, passError };
        for (Label lbl : errors) {
            lbl.setText("");
            lbl.setVisible(false);
            lbl.setManaged(false);
        }
    }

    @FXML
    void handleGoToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleMenuToggle(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/custom_menu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BloodLink - Menu");
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleGoogleSignUp(ActionEvent event) {
        clearErrors();
        googleLoadingLabel.setVisible(true);
        googleLoadingLabel.setManaged(true);
        
        // Run OAuth flow in a background thread to prevent freezing the UI
        new Thread(() -> {
            try {
                tn.esprit.services.GoogleOAuthService oauthService = new tn.esprit.services.GoogleOAuthService();
                tn.esprit.services.GoogleOAuthService.GoogleUserInfo userInfo = oauthService.authenticate();
                
                // Switch back to JavaFX Application Thread for UI/DB updates
                javafx.application.Platform.runLater(() -> {
                    try {
                        UserService userService = new UserService();
                        User u = userService.findByEmail(userInfo.email);
                        
                        if (u == null) {
                            // User doesn't exist, create a new one
                            String randomPassword = java.util.UUID.randomUUID().toString();
                            u = new User(userInfo.email, userInfo.familyName, userInfo.givenName, randomPassword, "client", "");
                            userService.ajouter(u);
                            System.out.println("Google Auth: Created new user " + userInfo.email);
                        }
                        
                        // Proceed to login
                        tn.esprit.tools.SessionManager.setCurrentUser(u);
                        
                        // Always go to Client Home for new sign-ups or if they already have an account, route them
                        if ("admin".equalsIgnoreCase(u.getRole())) {
                            navigateTo("/admin_dashboard.fxml", event);
                        } else if (u.getRole() != null && u.getRole().toLowerCase().contains("cnts")) {
                            navigateTo("/cnts_agent_home.fxml", event);
                        } else if (u.getRole() != null && u.getRole().toLowerCase().contains("banque")) {
                            navigateTo("/AgentBanqueBase.fxml", event);
                        } else {
                            navigateTo("/client_home.fxml", event);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showFieldError(emailError, "Erreur base de données: " + e.getMessage());
                    } finally {
                        googleLoadingLabel.setVisible(false);
                        googleLoadingLabel.setManaged(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    googleLoadingLabel.setVisible(false);
                    googleLoadingLabel.setManaged(false);
                    showFieldError(emailError, "Erreur Google SignUp: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void navigateTo(String fxml, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
