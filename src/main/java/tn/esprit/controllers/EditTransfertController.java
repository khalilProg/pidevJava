package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import tn.esprit.entities.Transfert;
import tn.esprit.services.TransfertService;

public class EditTransfertController {

    @FXML private Label titleLabel; // Added to match mockup dynamic title

    @FXML private TextField txtFrom;
    @FXML private TextField txtTo;
    @FXML private TextField txtQuantite;

    @FXML private DatePicker dateEnvoie;
    @FXML private DatePicker dateReception;

    @FXML private ComboBox<String> comboStatus;

    private Transfert transfert;
    private final TransfertService service = new TransfertService();

    @FXML
    public void initialize() {
        comboStatus.getItems().addAll(
            "EN COURS",
            "REÇU",
            "ANNULÉ"
        );
    }

    public void setTransfert(Transfert t) {
        this.transfert = t;

        // Dynamic title matching mockup
        if (titleLabel != null) {
            titleLabel.setText("MODIFIER TRANSFERT #" + t.getId());
        }

        txtFrom.setText(String.valueOf(t.getFromOrgId()));
        txtTo.setText(String.valueOf(t.getToOrgId()));
        txtQuantite.setText(String.valueOf(t.getQuantite()));

        dateEnvoie.setValue(t.getDateEnvoie());
        dateReception.setValue(t.getDateReception());

        // Map status cleanly to combo
        String status = t.getStatus();
        if (status != null) {
            status = status.toUpperCase();
            if (status.equals("EN_COURS")) status = "EN COURS";
            if (status.equals("RECU")) status = "REÇU";
            if (status.equals("ANNULE")) status = "ANNULÉ";
        } else {
            status = "EN COURS";
        }
        comboStatus.setValue(status);
    }

    @FXML
    private void updateTransfert() {
        if (!validateForm()) return;

        try {
            transfert.setFromOrgId(Integer.parseInt(txtFrom.getText()));
            transfert.setToOrgId(Integer.parseInt(txtTo.getText()));
            transfert.setQuantite(Integer.parseInt(txtQuantite.getText()));

            transfert.setDateEnvoie(dateEnvoie.getValue());
            transfert.setDateReception(dateReception.getValue());

            // Convert back to DB format
            String status = comboStatus.getValue();
            if (status.equals("EN COURS")) status = "EN_COURS";
            if (status.equals("REÇU")) status = "RECU";
            if (status.equals("ANNULÉ")) status = "ANNULE";
            
            transfert.setStatus(status);

            service.modifier(transfert);

            // Go back directly instead of alerting to save clicks
            retour();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (txtFrom.getText().isEmpty()) errors.append("- ID Origine obligatoire\n");
        if (txtTo.getText().isEmpty()) errors.append("- ID Destination obligatoire\n");
        if (txtQuantite.getText().isEmpty()) {
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
    private void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TransfertBackView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtFrom.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
