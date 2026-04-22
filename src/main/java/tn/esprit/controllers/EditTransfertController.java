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

    @FXML private Label titleLabel;

    @FXML private TextField txtFrom;
    @FXML private Label txtFromError;

    @FXML private TextField txtTo;
    @FXML private Label txtToError;

    @FXML private TextField txtQuantite;
    @FXML private Label txtQuantiteError;

    @FXML private DatePicker dateEnvoie;
    @FXML private Label dateEnvoieError;

    @FXML private DatePicker dateReception;
    @FXML private Label dateReceptionError;

    @FXML private ComboBox<String> comboStatus;
    @FXML private Label comboStatusError;

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

        if (titleLabel != null) {
            titleLabel.setText("MODIFIER TRANSFERT #" + t.getId());
        }

        txtFrom.setText(String.valueOf(t.getFromOrgId()));
        txtTo.setText(String.valueOf(t.getToOrgId()));
        txtQuantite.setText(String.valueOf(t.getQuantite()));

        dateEnvoie.setValue(t.getDateEnvoie());
        dateReception.setValue(t.getDateReception());

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

            String status = comboStatus.getValue();
            if (status.equals("EN COURS")) status = "EN_COURS";
            if (status.equals("REÇU")) status = "RECU";
            if (status.equals("ANNULÉ")) status = "ANNULE";
            
            transfert.setStatus(status);

            service.modifier(transfert);

            retour();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        resetErrors();

        if (txtFrom.getText() == null || txtFrom.getText().trim().isEmpty()) {
            showError(txtFromError, txtFrom, "ID Origine obligatoire");
            isValid = false;
        }

        if (txtTo.getText() == null || txtTo.getText().trim().isEmpty()) {
            showError(txtToError, txtTo, "ID Destination obligatoire");
            isValid = false;
        }

        if (txtQuantite.getText() == null || txtQuantite.getText().trim().isEmpty()) {
            showError(txtQuantiteError, txtQuantite, "Quantité obligatoire");
            isValid = false;
        } else {
            try {
                if (Integer.parseInt(txtQuantite.getText()) <= 0) {
                    showError(txtQuantiteError, txtQuantite, "Doit être > 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showError(txtQuantiteError, txtQuantite, "Non valide");
                isValid = false;
            }
        }

        if (dateEnvoie.getValue() == null) {
            showError(dateEnvoieError, dateEnvoie, "Date obligatoire");
            isValid = false;
        }

        if (dateReception.getValue() == null) {
            showError(dateReceptionError, dateReception, "Date obligatoire");
            isValid = false;
        } else if (dateEnvoie.getValue() != null && dateReception.getValue().isBefore(dateEnvoie.getValue())) {
            showError(dateReceptionError, dateReception, "Date invalide");
            isValid = false;
        }

        if (comboStatus.getValue() == null) {
            showError(comboStatusError, comboStatus, "Statut obligatoire");
            isValid = false;
        }

        return isValid;
    }

    private void showError(Label errorLabel, Control inputField, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (!inputField.getStyleClass().contains("text-field-error")) {
            inputField.getStyleClass().add("text-field-error");
        }
    }

    private void resetErrors() {
        Label[] labels = {txtFromError, txtToError, txtQuantiteError, dateEnvoieError, dateReceptionError, comboStatusError};
        Control[] fields = {txtFrom, txtTo, txtQuantite, dateEnvoie, dateReception, comboStatus};

        for (Label l : labels) {
            if (l != null) {
                l.setVisible(false);
                l.setManaged(false);
                l.setText("");
            }
        }
        for (Control c : fields) {
            if (c != null) {
                c.getStyleClass().remove("text-field-error");
            }
        }
    }

    @FXML
    private void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TransfertBackView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtFrom.getScene().getWindow();
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
