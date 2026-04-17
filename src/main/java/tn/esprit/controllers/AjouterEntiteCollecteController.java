package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.EntiteCollecteService;

public class AjouterEntiteCollecteController {

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

    private EntiteCollecteService service = new EntiteCollecteService();

    @FXML
    void handleEnregistrer(ActionEvent event) {
        String nom = txtNom.getText();
        String type = txtType.getText();
        String telephone = txtTelephone.getText();
        String adresse = txtAdresse.getText();
        String ville = txtVille.getText();

        if (nom.isEmpty() || type.isEmpty() || telephone.isEmpty() || adresse.isEmpty() || ville.isEmpty()) {
            afficherErreur("Erreur de saisie", "Veuillez remplir tous les champs !");
            return;
        }

        if (!telephone.matches("^\\d{8}$")) {
            afficherErreur("Erreur Numéro", "Le numéro de téléphone doit contenir exactement 8 chiffres !");
            return;
        }

        if (adresse.length() < 5) {
            afficherErreur("Erreur Adresse", "L'adresse doit faire au moins 5 caractères !");
            return;
        }

        EntiteDeCollecte e = new EntiteDeCollecte(0, nom, telephone, type, adresse, ville);

        try {
            service.ajouter(e);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Entité ajoutée avec succès !");
            alert.showAndWait();
            fermerFenetre();
        } catch (IllegalArgumentException ex) {
            afficherErreur("Erreur d'Unicité", ex.getMessage());
        } catch (Exception ex) {
            afficherErreur("Erreur BDD", "Une erreur s'est produite : " + ex.getMessage());
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

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
