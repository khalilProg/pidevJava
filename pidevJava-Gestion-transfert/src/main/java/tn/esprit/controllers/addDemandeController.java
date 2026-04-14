package tn.esprit.controllers;

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

import java.awt.event.ActionEvent;

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

        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
