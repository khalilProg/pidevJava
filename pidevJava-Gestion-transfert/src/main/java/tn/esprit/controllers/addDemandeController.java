package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import tn.esprit.entities.Demande;
import tn.esprit.services.DemandeService;
import javafx.scene.control.TextField;

public class addDemandeController {
    @FXML
    private TextField txtBanque;
    @FXML private TextField txtType;
    @FXML private TextField txtQuantite;
    @FXML private ComboBox<String> comboUrgence;
    @FXML private ComboBox<String> comboType;
    private Demande demandeToEdit;
    private boolean isEditMode = false;
    private DemandeService service = new DemandeService();
    private DemandeController mainController;

    public void setMainController(DemandeController controller) {
        this.mainController = controller;
    }

    @FXML
    public void ajouterDemande() {

        // 🔥 VALIDATION AVANT TOUT
        if (!validateForm()) {
            return;
        }

        try {

            if (isEditMode) {
                // 🔥 UPDATE
                demandeToEdit.setBanque(Integer.parseInt(txtBanque.getText()));
                demandeToEdit.setTypeSang(comboType.getValue());
                demandeToEdit.setQuantite(Integer.parseInt(txtQuantite.getText()));
                demandeToEdit.setUrgence(comboUrgence.getValue());

                service.modifier(demandeToEdit);

                System.out.println("Modifié avec succès");

            } else {
                // 🔥 ADD
                Demande d = new Demande();

                d.setBanque(Integer.parseInt(txtBanque.getText()));
                d.setTypeSang(comboType.getValue());
                d.setQuantite(Integer.parseInt(txtQuantite.getText()));
                d.setUrgence(comboUrgence.getValue());
                d.setStatus("EN_ATTENTE");

                service.ajouter(d);

                System.out.println("Ajouté avec succès");
            }

            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        txtBanque.clear();
        txtQuantite.clear();
        comboType.setValue(null);
        comboUrgence.setValue(null);
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // BANQUE
        if (txtBanque.getText().isEmpty()) {
            errors.append("Banque ID obligatoire\n");
        } else {
            try {
                Integer.parseInt(txtBanque.getText());
            } catch (NumberFormatException e) {
                errors.append("Banque ID doit être un nombre\n");
            }
        }

        // TYPE SANG
        if (comboType.getValue() == null) {
            errors.append("Type de sang obligatoire\n");
        }

        // QUANTITE
        if (txtQuantite.getText().isEmpty()) {
            errors.append("Quantité obligatoire\n");
        } else {
            try {
                int q = Integer.parseInt(txtQuantite.getText());
                if (q <= 0) {
                    errors.append("Quantité doit être > 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Quantité invalide\n");
            }
        }

        // URGENCE
        if (comboUrgence.getValue() == null) {
            errors.append("Urgence obligatoire\n");
        }

        // AFFICHAGE ERREUR
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Vérifier les champs");
            alert.setContentText(errors.toString());
            alert.show();
            return false;
        }

        return true;
    }

    @FXML

    private void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutDemande.fxml"));
            Parent root = loader.load();

            // récupérer la fenêtre actuelle
            Stage stage = (Stage) txtBanque.getScene().getWindow();

            // changer seulement la scène (PAS nouvelle fenêtre)
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEditData(Demande d) {
        this.demandeToEdit = d;
        this.isEditMode = true;

        txtBanque.setText(String.valueOf(d.getBanque()));
        comboType.setValue(d.getTypeSang());
        txtQuantite.setText(String.valueOf(d.getQuantite()));
        comboUrgence.setValue(d.getUrgence());
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DemandeBackView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) txtBanque.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
