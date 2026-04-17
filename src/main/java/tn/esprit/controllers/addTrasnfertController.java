package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.Demande;
import tn.esprit.entities.Transfert;
import tn.esprit.services.TransfertService;

public class addTrasnfertController {
    
    @FXML private TextField txtFrom;
    @FXML private TextField txtTo;
    @FXML private TextField txtQuantite;

    @FXML private DatePicker dateEnvoie;
    @FXML private DatePicker dateReception;

    @FXML private ComboBox<String> comboStatus;
    private Demande demande;
    
    private final TransfertService service = new TransfertService();

    @FXML
    public void initialize() {
        comboStatus.getItems().addAll(
            "EN COURS",
            "REÇU",
            "ANNULÉ"
        );
    }

    @FXML
    private void ajouterTransfert(ActionEvent event) {
        if (!validateForm()) return;

        try {
            Transfert t = new Transfert();

            t.setFromOrgId(Integer.parseInt(txtFrom.getText()));
            t.setToOrgId(Integer.parseInt(txtTo.getText()));
            t.setQuantite(Integer.parseInt(txtQuantite.getText()));

            // Optional or hardcoded display strings for organizations
            t.setFromOrg("BloodLink Central"); 
            t.setToOrg("Banque " + txtTo.getText());

            if (dateEnvoie.getValue() != null) t.setDateEnvoie(dateEnvoie.getValue());
            if (dateReception.getValue() != null) t.setDateReception(dateReception.getValue());

            // Convert back to DB format
            String status = comboStatus.getValue();
            if (status != null) {
                if (status.equals("EN COURS")) status = "EN_COURS";
                if (status.equals("REÇU")) status = "RECU";
                if (status.equals("ANNULÉ")) status = "ANNULE";
            } else {
                status = "EN_COURS";
            }
            t.setStatus(status);

            service.ajouter(t);

            // Go back directly instead of alerting to save clicks
            retour(event);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (txtFrom.getText() == null || txtFrom.getText().trim().isEmpty()) errors.append("- ID Origine obligatoire\n");
        if (txtTo.getText() == null || txtTo.getText().trim().isEmpty()) errors.append("- ID Destination obligatoire\n");
        
        if (txtQuantite.getText() == null || txtQuantite.getText().trim().isEmpty()) {
            errors.append("- Quantité obligatoire\n");
        } else {
            try {
                if (Integer.parseInt(txtQuantite.getText()) <= 0) {
                    errors.append("- Quantité doit être > 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Quantité invalide\n");
            }
        }
        
        if (dateEnvoie.getValue() == null) errors.append("- Date d'envoi obligatoire\n");
        if (dateReception.getValue() == null) errors.append("- Date de réception obligatoire\n");
        if (comboStatus.getValue() == null) errors.append("- Statut obligatoire\n");

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur de validation");
            alert.setContentText(errors.toString());
            alert.show();
            return false;
        }
        return true;
    }

    @FXML
    private void fermer(ActionEvent event) {
        retour(event);
    }
    
    private void retour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TransfertBackView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setDemande(Demande demande) {
        this.demande = demande;
    }
}
