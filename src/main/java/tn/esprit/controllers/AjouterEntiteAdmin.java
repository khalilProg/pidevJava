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
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.EntiteCollecteService;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterEntiteAdmin {

    @FXML private TextField nomInput;
    @FXML private TextField adresseInput;
    @FXML private TextField villeInput;
    @FXML private TextField telephoneInput;
    @FXML private Label nomError;
    @FXML private Label adresseError;
    @FXML private Label villeError;
    @FXML private Label telephoneError;

    private EntiteCollecteService ecs = new EntiteCollecteService();

    @FXML
    private void handleCreer(ActionEvent event) {
        String nom = nomInput.getText().trim();
        String adresse = adresseInput.getText().trim();
        String ville = villeInput.getText().trim();
        String tel = telephoneInput.getText().trim();
        
        boolean hasError = clearAndValidateEntite(nom, adresse, ville, tel);
        if (hasError) return;

        EntiteDeCollecte e = new EntiteDeCollecte(0, nom, tel, "Hôpital", adresse, ville);

        try {
            // Unicité handler
            ecs.ajouter(e);
            navigateTo(event, "/ListeEntitesAdmin.fxml");
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage().toLowerCase();
            if (msg.contains("téléphone") || msg.contains("tel")) {
                showFieldError(telephoneError, ex.getMessage());
            } else {
                showFieldError(nomError, ex.getMessage());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private boolean clearAndValidateEntite(String nom, String adresse, String ville, String tel) {
        // Reset styles to default info styling
        resetErrorLabel(nomError, "Le nom officiel de l'entité de collecte (ex: Hôpital X).");
        resetErrorLabel(adresseError, "Adresse complète de l'entité.");
        resetErrorLabel(villeError, "Ville de l'entité.");
        resetErrorLabel(telephoneError, "Exactement 8 chiffres.");

        boolean hasError = false;

        if (nom.isEmpty()) { showFieldError(nomError, "Ce champ est obligatoire."); hasError = true; }
        if (adresse.isEmpty()) { showFieldError(adresseError, "Ce champ est obligatoire."); hasError = true; }
        if (ville.isEmpty()) { showFieldError(villeError, "Ce champ est obligatoire."); hasError = true; }
        if (tel.isEmpty()) { 
            showFieldError(telephoneError, "Ce champ est obligatoire."); hasError = true; 
        } else if (!tel.matches("\\d{8}")) {
            showFieldError(telephoneError, "Le numéro de téléphone doit contenir exactement 8 chiffres."); hasError = true;
        }

        return hasError;
    }

    private void showFieldError(Label label, String message) {
        label.setText("⚠ " + message);
        label.setStyle("-fx-text-fill: #ff4d4d; -fx-font-size: 11px;");
    }

    private void resetErrorLabel(Label label, String defaultMessage) {
        label.setText(defaultMessage);
        label.setStyle("-fx-text-fill: -muted; -fx-font-size: 11px;");
    }

    @FXML
    void handleRetour(ActionEvent event) {
        navigateTo(event, "/ListeEntitesAdmin.fxml");
    }

    // ── Navigation Handlers ──

    @FXML void handleLogout(ActionEvent event) { navigateTo(event, "/login.fxml"); }
    @FXML void handleNavigateDashboard(ActionEvent event) { navigateTo(event, "/admin_dashboard.fxml"); }
    @FXML void handleNavigateUsers(ActionEvent event) { navigateTo(event, "/admin_users.fxml"); }
    @FXML void handleNavigateDemandes(ActionEvent event) { navigateTo(event, "/DemandeBackView.fxml"); }
    @FXML void handleNavigateTransferts(ActionEvent event) { navigateTo(event, "/TransfertBackView.fxml"); }
    @FXML void handleNavigateQuestionnaires(ActionEvent event) { navigateTo(event, "/ListeQuestAdmin.fxml"); }
    @FXML void handleNavigateRendezVous(ActionEvent event) { navigateTo(event, "/ListeRdvAdmin.fxml"); }
    @FXML void handleNavigateCampagnes(ActionEvent event) { navigateTo(event, "/ListeCampagnesAdmin.fxml"); }
    @FXML void handleNavigateCollectes(ActionEvent event) { navigateTo(event, "/ListeEntitesAdmin.fxml"); }

    private void navigateTo(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
