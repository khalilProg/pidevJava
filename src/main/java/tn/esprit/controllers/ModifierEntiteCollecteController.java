package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.EntiteCollecteService;

public class ModifierEntiteCollecteController extends BaseFront {

    private static EntiteDeCollecte entiteActive;

    public static void setEntiteActive(EntiteDeCollecte entite) {
        entiteActive = entite;
    }

    @FXML private TextField txtNom;
    @FXML private TextField txtType;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtVille;
    @FXML private TextField txtTelephone;

    @FXML private Label nomError;
    @FXML private Label typeError;
    @FXML private Label adresseError;
    @FXML private Label villeError;
    @FXML private Label telephoneError;

    private EntiteCollecteService service = new EntiteCollecteService();

    @FXML
    public void initialize() {
        super.initialize(); // Initialize BaseFront components
        resetErrors();

        if (entiteActive != null) {
            txtNom.setText(entiteActive.getNom());
            txtTelephone.setText(entiteActive.getTel());
            txtType.setText(entiteActive.getType());
            txtAdresse.setText(entiteActive.getAdresse());
            txtVille.setText(entiteActive.getVille());
        }
    }

    private void resetErrors() {
        nomError.setStyle("-fx-text-fill: -muted; -fx-font-size: 11px;");
        nomError.setText("Le nom officiel de l'entité de collecte (ex: Hôpital X).");

        typeError.setStyle("-fx-text-fill: -muted; -fx-font-size: 11px;");
        typeError.setText("Le type de l'entité.");

        adresseError.setStyle("-fx-text-fill: -muted; -fx-font-size: 11px;");
        adresseError.setText("Adresse complète de l'entité.");

        villeError.setStyle("-fx-text-fill: -muted; -fx-font-size: 11px;");
        villeError.setText("Ville où se situe l'entité.");

        telephoneError.setStyle("-fx-text-fill: -muted; -fx-font-size: 11px;");
        telephoneError.setText("Exactement 8 chiffres.");

        txtNom.setStyle("");
        txtType.setStyle("");
        txtAdresse.setStyle("");
        txtVille.setStyle("");
        txtTelephone.setStyle("");
    }

    private void showError(Label label, TextField field, String message) {
        label.setText(message);
        label.setStyle("-fx-text-fill: #ff4d4d; -fx-font-size: 11px; -fx-font-weight: bold;");
        field.setStyle("-fx-border-color: #ff4d4d; -fx-border-width: 2px; -fx-border-radius: 6px;");
    }

    @FXML
    void handleEnregistrer(ActionEvent event) {
        resetErrors();
        boolean isValid = true;

        String nom = txtNom.getText().trim();
        String type = txtType.getText().trim();
        String telephone = txtTelephone.getText().trim();
        String adresse = txtAdresse.getText().trim();
        String ville = txtVille.getText().trim();

        if (nom.isEmpty()) {
            showError(nomError, txtNom, "Le nom est obligatoire.");
            isValid = false;
        }

        if (type.isEmpty()) {
            showError(typeError, txtType, "Le type est obligatoire.");
            isValid = false;
        }

        if (adresse.isEmpty()) {
            showError(adresseError, txtAdresse, "L'adresse est obligatoire.");
            isValid = false;
        } else if (adresse.length() < 5) {
            showError(adresseError, txtAdresse, "L'adresse doit faire au moins 5 caractères.");
            isValid = false;
        }

        if (ville.isEmpty()) {
            showError(villeError, txtVille, "La ville est obligatoire.");
            isValid = false;
        }

        if (telephone.isEmpty()) {
            showError(telephoneError, txtTelephone, "Le téléphone est obligatoire.");
            isValid = false;
        } else if (!telephone.matches("^\\d{8}$")) {
            showError(telephoneError, txtTelephone, "Le numéro doit comporter exactement 8 chiffres.");
            isValid = false;
        }

        if (!isValid || entiteActive == null) return;

        entiteActive.setNom(nom);
        entiteActive.setTel(telephone);
        entiteActive.setType(type);
        entiteActive.setAdresse(adresse);
        entiteActive.setVille(ville);

        try {
            service.modifier(entiteActive);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Entité modifiée avec succès !");
            alert.showAndWait();
            switchScene(event, "/EntiteCollecteFront.fxml");
        } catch (IllegalArgumentException ex) {
            showError(nomError, txtNom, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError(nomError, txtNom, "Erreur serveur : " + ex.getMessage());
        }
    }

    @FXML
    void handleRetour(ActionEvent event) {
        switchScene(event, "/EntiteCollecteFront.fxml");
    }
}
