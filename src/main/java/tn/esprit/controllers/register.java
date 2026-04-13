package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import javafx.scene.control.Label;

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
    private Label errorLabel;

    @FXML
    void handleRegister(ActionEvent event) {
        String nom = nomF.getText();
        String prenom = prenomF.getText();
        String email = emailF.getText();
        String tel = telF.getText();
        String password = passF.getText();
        errorLabel.setVisible(false);

        if(nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            displayError("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Default constraints for newly created users matching web architecture
        User newUser = new User(email, nom, prenom, password, "client", tel);
        UserService userService = new UserService();
        
        try {
            userService.ajouter(newUser);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            handleGoToLogin(event); // Navigate to login
        } catch (Exception e) {
            e.printStackTrace();
            displayError("Erreur lors de l'inscription: " + e.getMessage());
        }
    }
    
    private void displayError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    @FXML
    void handleGoToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
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
}
