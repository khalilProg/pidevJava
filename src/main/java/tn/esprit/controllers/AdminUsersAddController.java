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
    private Label prenomError;
    @FXML
    private Label nomError;
    @FXML
    private Label emailError;
    @FXML
    private Label passError;
    @FXML
    private Label roleError;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> roles = FXCollections.observableArrayList(
            "admin", "client", "docteur", "agent banque", "agent cnts"
        );
        roleCombo.setItems(roles);
    }

    @FXML
    void handleAddUser(ActionEvent event) {
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

        if (password.isEmpty()) {
            showFieldError(passError, "Le mot de passe est obligatoire.");
            valid = false;
        } else if (password.length() < 6) {
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

        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));
        User newUser = new User(email, nom, prenom, hashed, role, "");
        UserService userService = new UserService();

        try {
            userService.ajouter(newUser);
            if ("client".equalsIgnoreCase(role)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_users_complete_client.fxml"));
                Parent root = loader.load();
                AdminUsersCompleteClientController controller = loader.getController();
                controller.initData(newUser);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else if ("agent banque".equalsIgnoreCase(role)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_users_complete_banque.fxml"));
                Parent root = loader.load();
                AdminUsersCompleteBanqueController controller = loader.getController();
                controller.initData(newUser);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                navigateTo(event, "/admin_users.fxml");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showFieldError(emailError, "Erreur d'ajout: " + e.getMessage());
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
