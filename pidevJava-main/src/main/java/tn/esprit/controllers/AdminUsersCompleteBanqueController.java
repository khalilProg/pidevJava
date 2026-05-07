package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.Banque;
import tn.esprit.entities.User;
import tn.esprit.services.BanqueService;

import java.io.IOException;

public class AdminUsersCompleteBanqueController {

    @FXML
    private Label subtitleLabel;
    @FXML
    private TextField nomBanqueF;
    @FXML
    private TextField adresseF;
    @FXML
    private TextField telephoneF;

    @FXML
    private Label nomBanqueError;
    @FXML
    private Label adresseError;
    @FXML
    private Label telephoneError;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        if (this.currentUser != null) {
            subtitleLabel.setText("Détails de l'établissement pour le compte #" + this.currentUser.getId() + ".");
        }
    }

    @FXML
    void handleConfirm(ActionEvent event) {
        clearErrors();

        String nomBanque = nomBanqueF.getText().trim();
        String adresse = adresseF.getText().trim();
        String telephone = telephoneF.getText().trim();

        boolean valid = true;

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
            showFieldError(nomBanqueError, "Erreur fatale: utilisateur non trouvé.");
            return;
        }

        Banque nouvelleBanque = new Banque(nomBanque, adresse, telephone, currentUser);
        BanqueService banqueService = new BanqueService();

        try {
            banqueService.ajouter(nouvelleBanque);
            navigateTo(event, "/admin_users.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showFieldError(nomBanqueError, "Erreur d'ajout: " + e.getMessage());
        }
    }

    private void showFieldError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearErrors() {
        Label[] errors = { nomBanqueError, adresseError, telephoneError };
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
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
