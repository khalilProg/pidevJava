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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.entities.User;
import tn.esprit.entities.client;
import tn.esprit.services.ClientService;
import tn.esprit.services.UserService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class AdminUsersEditClientController {

    @FXML
    private Label subtitleLabel;
    
    // User fields
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

    // client fields
    @FXML
    private ComboBox<String> groupeSangCombo;
    @FXML
    private DatePicker dernierDonPicker;

    // Error labels
    @FXML
    private Label prenomError;
    @FXML
    private Label nomError;
    @FXML
    private Label emailError;
    @FXML
    private Label passError;
    @FXML
    private Label groupeSangError;
    @FXML
    private Label dernierDonError;

    private User currentUser;
    private client currentClient;

    public void initData(User user, client clientData) {
        this.currentUser = user;
        this.currentClient = clientData;

        // Initialize combos
        ObservableList<String> bloodTypes = FXCollections.observableArrayList(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        );
        groupeSangCombo.setItems(bloodTypes);
        
        ObservableList<String> roles = FXCollections.observableArrayList("client");
        roleCombo.setItems(roles);
        roleCombo.setValue("client");

        if (user != null) {
            subtitleLabel.setText("Mettre à jour les informations de " + user.getPrenom() + " " + user.getNom() + ".");
            prenomF.setText(user.getPrenom() != null ? user.getPrenom() : "");
            nomF.setText(user.getNom() != null ? user.getNom() : "");
            emailF.setText(user.getEmail() != null ? user.getEmail() : "");
        }

        // If no client data was passed, load it
        if (clientData == null && user != null) {
            try {
                ClientService cs = new ClientService();
                List<client> clients = cs.recuperer();
                for (client c : clients) {
                    if (c.getUser() != null && c.getUser().getId() == user.getId()) {
                        this.currentClient = c;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Pre-fill client fields
        if (this.currentClient != null) {
            if (this.currentClient.getTypeSang() != null) {
                groupeSangCombo.setValue(this.currentClient.getTypeSang());
            }
            if (this.currentClient.getDernierDon() != null) {
                dernierDonPicker.setValue(this.currentClient.getDernierDon());
            }
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        clearErrors();

        String prenom = prenomF.getText().trim();
        String nom = nomF.getText().trim();
        String email = emailF.getText().trim();
        String password = passF.getText();

        String groupeSanguin = groupeSangCombo.getValue();
        LocalDate dernierDon = dernierDonPicker.getValue();

        boolean valid = true;

        // User validation
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

        // client validation
        if (groupeSanguin == null || groupeSanguin.isEmpty()) {
            showFieldError(groupeSangError, "Veuillez sélectionner un groupe sanguin.");
            valid = false;
        }

        if (dernierDon == null) {
            showFieldError(dernierDonError, "Veuillez sélectionner une date.");
            valid = false;
        } else if (dernierDon.isAfter(LocalDate.now())) {
            showFieldError(dernierDonError, "La date ne peut pas être dans le futur.");
            valid = false;
        }

        if (!valid) {
            return;
        }
        
        if (currentUser == null) {
            showFieldError(prenomError, "Erreur système: user introuvable.");
            return;
        }

        // Update User info First
        currentUser.setPrenom(prenom);
        currentUser.setNom(nom);
        currentUser.setEmail(email);
        if (password != null && !password.isEmpty()) {
            currentUser.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        }

        UserService userService = new UserService();
        try {
            userService.modifier(currentUser);
        } catch (Exception e) {
            e.printStackTrace();
            showFieldError(emailError, "Erreur modif User: " + e.getMessage());
            return;
        }

        // Then update client info
        if (currentClient != null) {
            currentClient.setTypeSang(groupeSanguin);
            currentClient.setDernierDon(dernierDon);

            ClientService clientService = new ClientService();
            try {
                clientService.modifier(currentClient);
            } catch (Exception e) {
                e.printStackTrace();
                showFieldError(groupeSangError, "Erreur modif client: " + e.getMessage());
                return;
            }
        }
        
        navigateTo(event, "/admin_users.fxml");
    }

    private void showFieldError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearErrors() {
        Label[] errors = { prenomError, nomError, emailError, passError, groupeSangError, dernierDonError };
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
