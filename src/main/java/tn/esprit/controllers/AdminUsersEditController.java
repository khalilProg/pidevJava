package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;

import java.io.IOException;

public class AdminUsersEditController {

    @FXML
    private Label subtitleLabel;
    @FXML
    private TextField prenomF;
    @FXML
    private TextField nomF;
    @FXML
    private TextField emailF;
    @FXML
    private PasswordField passF;
    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private Label prenomError;
    @FXML
    private Label nomError;
    @FXML
    private Label emailError;
    @FXML
    private Label passError;
    @FXML
    private Label roleError;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        if (user != null) {
            subtitleLabel.setText("Mettre à jour les informations du compte #" + user.getId() + ".");
            prenomF.setText(user.getPrenom() != null ? user.getPrenom() : "");
            nomF.setText(user.getNom() != null ? user.getNom() : "");
            emailF.setText(user.getEmail() != null ? user.getEmail() : "");
            
            ObservableList<String> roles = FXCollections.observableArrayList(
                "admin", "client", "docteur", "agent banque", "agent cnts"
            );
            roleCombo.setItems(roles);
            roleCombo.setValue(user.getRole() != null ? user.getRole().toLowerCase() : "client");
        }
    }

    @FXML
    void handleUpdateUser(ActionEvent event) {
        clearErrors();

        String prenom = prenomF.getText().trim();
        String nom = nomF.getText().trim();
        String email = emailF.getText().trim();
        String password = passF.getText();
        String role = roleCombo.getValue();

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

        if (password != null && !password.isEmpty() && password.length() < 6) {
            showFieldError(passError, "Le mot de passe doit contenir au moins 6 caractères.");
            valid = false;
        }

        if (role == null || role.isEmpty()) {
            showFieldError(roleError, "Veuillez sélectionner un rôle.");
            valid = false;
        }

        if (!valid) {
            return;
        }

        currentUser.setPrenom(prenom);
        currentUser.setNom(nom);
        currentUser.setEmail(email);
        currentUser.setRole(role);
        
        if (password != null && !password.isEmpty()) {
            currentUser.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        }

        UserService userService = new UserService();
        try {
            userService.modifier(currentUser);
            navigateTo(event, "/admin_users.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showFieldError(emailError, "Erreur lors de la modification: " + e.getMessage());
        }
    }

    private void showFieldError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearErrors() {
        Label[] errors = { prenomError, nomError, emailError, passError, roleError };
        for (Label lbl : errors) {
            lbl.setText("");
            lbl.setVisible(false);
            lbl.setManaged(false);
        }
    }

    @FXML
    void handleNavigateUsers(ActionEvent event) {
        navigateTo(event, "/admin_users.fxml");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        navigateTo(event, "/login.fxml");
    }

    @FXML
    void handleNavigateDashboard(ActionEvent event) {
        navigateTo(event, "/admin_dashboard.fxml");
    }

    @FXML
    void handleNavigateDemandes(ActionEvent event) {
        navigateTo(event, "/DemandeBackView.fxml");
    }

    @FXML
    void handleNavigateTransferts(ActionEvent event) {
        navigateTo(event, "/TransfertBackView.fxml");
    }

    private void navigateTo(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
