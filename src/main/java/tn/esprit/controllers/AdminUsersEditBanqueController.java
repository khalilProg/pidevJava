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
import tn.esprit.entities.Banque;
import tn.esprit.entities.User;
import tn.esprit.services.BanqueService;
import tn.esprit.services.UserService;

import java.io.IOException;
import java.util.List;

public class AdminUsersEditBanqueController {

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

    // Banque fields
    @FXML
    private TextField nomBanqueF;
    @FXML
    private TextField adresseF;
    @FXML
    private TextField telephoneF;

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
    private Label nomBanqueError;
    @FXML
    private Label adresseError;
    @FXML
    private Label telephoneError;

    private User currentUser;
    private Banque currentBanque;

    public void initData(User user, Banque banqueData) {
        this.currentUser = user;
        this.currentBanque = banqueData;
        
        ObservableList<String> roles = FXCollections.observableArrayList(
            user != null && user.getRole() != null ? user.getRole().toLowerCase() : "banque"
        );
        roleCombo.setItems(roles);
        roleCombo.setValue(roles.get(0));

        if (user != null) {
            subtitleLabel.setText("Mettre à jour les coordonnées de l'établissement pour " + user.getPrenom() + " " + user.getNom() + ".");
            prenomF.setText(user.getPrenom() != null ? user.getPrenom() : "");
            nomF.setText(user.getNom() != null ? user.getNom() : "");
            emailF.setText(user.getEmail() != null ? user.getEmail() : "");
        }

        // If no banque data was passed, load it
        if (banqueData == null && user != null) {
            try {
                BanqueService bs = new BanqueService();
                List<Banque> banques = bs.recuperer();
                for (Banque b : banques) {
                    if (b.getUser() != null && b.getUser().getId() == user.getId()) {
                        this.currentBanque = b;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Pre-fill banque fields
        if (this.currentBanque != null) {
            if (this.currentBanque.getNom() != null) {
                nomBanqueF.setText(this.currentBanque.getNom());
            }
            if (this.currentBanque.getAdresse() != null) {
                adresseF.setText(this.currentBanque.getAdresse());
            }
            if (this.currentBanque.getTelephone() != null) {
                telephoneF.setText(this.currentBanque.getTelephone());
            }
        }
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        clearErrors();

        // Basic user info
        String prenom = prenomF.getText().trim();
        String nom = nomF.getText().trim();
        String email = emailF.getText().trim();
        String password = passF.getText();

        // Banque info
        String nomBanque = nomBanqueF.getText().trim();
        String adresse = adresseF.getText().trim();
        String telephone = telephoneF.getText().trim();

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

        // Banque validation
        if (nomBanque.isEmpty()) {
            showFieldError(nomBanqueError, "Le nom de la banque est obligatoire.");
            valid = false;
        }

        if (adresse.isEmpty()) {
            showFieldError(adresseError, "L'adresse est obligatoire.");
            valid = false;
        }

        if (telephone.isEmpty()) {
            showFieldError(telephoneError, "Le téléphone est obligatoire.");
            valid = false;
        } else {
            String digitsOnly = telephone.replaceAll("[^0-9]", "");
            if (digitsOnly.length() < 8) {
                showFieldError(telephoneError, "Le numéro doit contenir au moins 8 chiffres.");
                valid = false;
            }
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

        // Then update Banque info
        if (currentBanque != null) {
            currentBanque.setNom(nomBanque);
            currentBanque.setAdresse(adresse);
            currentBanque.setTelephone(telephone);

            BanqueService banqueService = new BanqueService();
            try {
                banqueService.modifier(currentBanque);
            } catch (Exception e) {
                e.printStackTrace();
                showFieldError(nomBanqueError, "Erreur modif Banque: " + e.getMessage());
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
        Label[] errors = { prenomError, nomError, emailError, passError, nomBanqueError, adresseError, telephoneError };
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
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
