package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.EntiteCollecteService;

public class ModifierEntiteCollecteController {

    private EntiteDeCollecte entiteActive;
    private EntiteCollecteService service = new EntiteCollecteService();

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtTelephone;

    @FXML
    private TextField txtType;

    @FXML
    private TextField txtAdresse;

    @FXML
    private TextField txtVille;

    @FXML
    private Button btnEnregistrer;

    @FXML
    private Button btnAnnuler;

    public void setEntite(EntiteDeCollecte e) {
        this.entiteActive = e;
        txtNom.setText(e.getNom());
        txtTelephone.setText(e.getTel());
        txtType.setText(e.getType());
        txtAdresse.setText(e.getAdresse());
        txtVille.setText(e.getVille());
    }

    @FXML
    void handleEnregistrer(ActionEvent event) {
        String nom = txtNom.getText();
        String telephone = txtTelephone.getText();
        String type = txtType.getText();
        String adresse = txtAdresse.getText();
        String ville = txtVille.getText();

        if (nom.isEmpty() || type.isEmpty() || telephone.isEmpty() || adresse.isEmpty() || ville.isEmpty()) {
            afficherErreur("Erreur", "Tous les champs sont obligatoires.");
            return;
        }
        
        if (!telephone.matches("^\\d{8}$")) {
            afficherErreur("Numéro invalide", "Le numéro doit comporter exactement 8 chiffres.");
            return;
        }
        
        if (adresse.length() < 5) {
            afficherErreur("Adresse trop courte", "L'adresse doit faire au moins 5 caractères.");
            return;
        }

        entiteActive.setNom(nom);
        entiteActive.setTel(telephone);
        entiteActive.setType(type);
        entiteActive.setAdresse(adresse);
        entiteActive.setVille(ville);

        try {
            service.modifier(entiteActive);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Entité modifiée avec succès !");
            alert.showAndWait();
            fermerFenetre();
        } catch (IllegalArgumentException ex) {
            afficherErreur("Unicité non validée", ex.getMessage());
        } catch (Exception ex) {
            afficherErreur("Erreur Serveur", ex.getMessage());
        }
    }

    @FXML
    void handleAnnuler(ActionEvent event) {
        fermerFenetre();
    }
    
    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
