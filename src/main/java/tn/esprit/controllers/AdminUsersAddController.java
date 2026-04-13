package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import org.mindrot.jbcrypt.BCrypt;

public class AdminUsersAddController implements Initializable {

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
    private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> roles = FXCollections.observableArrayList(
            "admin", "client", "docteur", "agent banque", "agent cnts"
        );
        roleCombo.setItems(roles);
    }

    @FXML
    void handleAddUser(ActionEvent event) {
        errorLabel.setVisible(false);

        String prenom = prenomF.getText();
        String nom = nomF.getText();
        String email = emailF.getText();
        String password = passF.getText();
        String role = roleCombo.getValue();

        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty() || password.isEmpty() || role == null || role.isEmpty()) {
            displayError("Veuillez remplir tous les champs et sélectionner un rôle.");
            return;
        }

        // Use Symfony standard encryption or generic standard via BCrypt if requested
        // Wait, normally we encrypt newly created users by desktop,
        // Actually, we'll hash them so they work via desktop, or keep it generic string.
        // I will hash it to BCrypt pure string: "$2a$10$" which php validates perfectly since logic is bound.
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));

        // Create new user with null phone string if irrelevant.
        User newUser = new User(email, nom, prenom, hashed, role, ""); 
        UserService userService = new UserService();
        
        try {
            userService.ajouter(newUser);
            // On success, return to user list automatically!
            navigateTo(event, "/admin_users.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            displayError("Erreur d'ajout réseau: " + e.getMessage());
        }
    }

    private void displayError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
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
    void handleNavigateUsers(ActionEvent event) {
        navigateTo(event, "/admin_users.fxml");
    }

    private void navigateTo(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
