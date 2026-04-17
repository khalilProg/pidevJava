package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Demande;
import tn.esprit.services.DemandeService;

public class EditDemandeController {

    @FXML private TextField txtType;
    @FXML private TextField txtQuantite;
    @FXML private ComboBox<String> comboStatus;
    @FXML private Button btnSave;

    private Demande demande;
    private final DemandeService demandeService = new DemandeService();

    @FXML
    public void initialize() {

        comboStatus.getItems().addAll(
            "EN_ATTENTE",
            "CONFIRME",
            "REFUSE"
        );
    }

    // 🔥 reçoit la demande depuis la table
    public void setDemande(Demande d) {
        this.demande = d;

        txtType.setText(d.getTypeSang());
        txtQuantite.setText(String.valueOf(d.getQuantite()));
        comboStatus.setValue(d.getStatus());
    }

    // 🔥 bouton save
    @FXML
    private void updateDemande() {

        try {
            if (txtType.getText().isEmpty() || txtQuantite.getText().isEmpty()) {
                System.out.println("Champs obligatoires !");
                return;
            }

            demande.setTypeSang(txtType.getText());
            demande.setQuantite(Integer.parseInt(txtQuantite.getText()));
            demande.setStatus(comboStatus.getValue());

            demandeService.modifier(demande);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Demande mise à jour !");
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DemandeBackView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) txtType.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
