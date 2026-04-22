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
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
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
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
