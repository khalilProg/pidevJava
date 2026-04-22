package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

import org.mindrot.jbcrypt.BCrypt;

import tn.esprit.entities.User;
import tn.esprit.services.UserService;

public class login {

    @FXML
    private TextField emailF;

    @FXML
    private CheckBox rememberMeCheckbox;

    @FXML
    private PasswordField passF;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        Preferences prefs = Preferences.userNodeForPackage(login.class);
        String savedEmail = prefs.get("saved_email", "");
        if (!savedEmail.isEmpty()) {
            emailF.setText(savedEmail);
            rememberMeCheckbox.setSelected(true);
        }
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailF.getText();
        String password = passF.getText();
        errorLabel.setVisible(false);

        if (email.isEmpty() || password.isEmpty()) {
            displayError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            UserService userService = new UserService();
            List<User> users = userService.recuperer();
            boolean found = false;

            for (User u : users) {
                if (u.getEmail().equals(email)) {
                    String hash = u.getPassword();
                    boolean passMatch = false;

                    // Support Symfony $2y$ BCrypt format by converting the prefix for jBcrypt
                    if (hash != null && hash.startsWith("$2y$")) {
                        hash = "$2a$" + hash.substring(4);
                        passMatch = BCrypt.checkpw(password, hash);
                    } else if (hash != null && hash.startsWith("$2a$")) {
                        passMatch = BCrypt.checkpw(password, hash);
                    } else if (password.equals(hash)) {
                        passMatch = true; // Support pure plain-text registrations from the desktop app
                    }

                    if (passMatch) {
                        found = true;
                        
                        Preferences prefs = Preferences.userNodeForPackage(login.class);
                        if (rememberMeCheckbox.isSelected()) {
                            prefs.put("saved_email", email);
                        } else {
                            prefs.remove("saved_email");
                        }

                        // Check if the user is an admin
                        if ("admin".equalsIgnoreCase(u.getRole())) {
                            navigateToDashboard(event);
                        } else {
                            navigateToClientHome(event, u);
                        }
                        break;
                    }
                }
            }

            if (!found) {
                displayError("Email ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayError("Erreur réseau: " + e.getMessage());
        }
    }

    private void navigateToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BloodLink - Dashboard Administration");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToClientHome(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_home.fxml"));
            Parent root = loader.load();
            ClientHomeController controller = loader.getController();
            controller.initData(user);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BloodLink - Accueil");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    void handleGoToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/register.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
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
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleForgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/forgot_password.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
