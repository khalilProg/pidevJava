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

public class ModifierEntiteAdmin {

    @FXML private TextField nomInput;
    @FXML private TextField adresseInput;
    @FXML private TextField villeInput;
    @FXML private TextField telephoneInput;
    @FXML private Label nomError;
    @FXML private Label adresseError;
    @FXML private Label villeError;
    @FXML private Label telephoneError;

    private EntiteCollecteService ecs = new EntiteCollecteService();
    private EntiteDeCollecte currentEntite;

    public void setEntiteToEdit(EntiteDeCollecte e) {
        this.currentEntite = e;
        nomInput.setText(e.getNom());
        adresseInput.setText(e.getAdresse());
        villeInput.setText(e.getVille());
        telephoneInput.setText(e.getTel());
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        String nom = nomInput.getText().trim();
        String adresse = adresseInput.getText().trim();
        String ville = villeInput.getText().trim();
        String tel = telephoneInput.getText().trim();
        
        boolean hasError = clearAndValidateEntite(nom, adresse, ville, tel);
        if (hasError) return;

        // Unicity loop verification
        try {
            for (EntiteDeCollecte existant : ecs.recuperer()) {
                if (existant.getId() != currentEntite.getId()) {
                    if (existant.getNom().equalsIgnoreCase(nom)) {
                        showFieldError(nomError, "Ce nom d'entité existe déjà.");
                        return;
                    }
                    if (existant.getTel().equals(tel)) {
                        showFieldError(telephoneError, "Ce numéro de téléphone est déjà pris.");
                        return;
                    }
                }
            }

            currentEntite.setNom(nom);
            currentEntite.setAdresse(adresse);
            currentEntite.setVille(ville);
            currentEntite.setTel(tel);

            ecs.modifier(currentEntite);
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
    private void handleRetour(ActionEvent event) {
        navigateTo(event, "/ListeEntitesAdmin.fxml");
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
