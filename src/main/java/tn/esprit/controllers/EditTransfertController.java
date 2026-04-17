package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import tn.esprit.entities.Transfert;
import tn.esprit.services.TransfertService;

import java.time.LocalDate;

public class EditTransfertController {

    @FXML private TextField txtFrom;
    @FXML private TextField txtTo;
    @FXML private TextField txtQuantite;

    @FXML private DatePicker dateEnvoie;
    @FXML private DatePicker dateReception;

    @FXML private ComboBox<String> comboStatus;

    private Transfert transfert;
    private final TransfertService service = new TransfertService();

    // ================= INIT =================
    @FXML
    public void initialize() {

        comboStatus.getItems().addAll(
            "EN_COURS",
            "RECU",
            "ANNULE"
        );
    }

    // ================= SET DATA =================
    public void setTransfert(Transfert t) {
        this.transfert = t;

        txtFrom.setText(String.valueOf(t.getFromOrgId()));
        txtTo.setText(String.valueOf(t.getToOrgId()));
        txtQuantite.setText(String.valueOf(t.getQuantite()));

        dateEnvoie.setValue(t.getDateEnvoie());
        dateReception.setValue(t.getDateReception());

        comboStatus.setValue(t.getStatus());
    }

    // ================= UPDATE =================
    @FXML
    private void updateTransfert() {

        if (!validateForm()) return;

        try {
            transfert.setFromOrgId(Integer.parseInt(txtFrom.getText()));
            transfert.setToOrgId(Integer.parseInt(txtTo.getText()));
            transfert.setQuantite(Integer.parseInt(txtQuantite.getText()));

            transfert.setDateEnvoie(dateEnvoie.getValue());
            transfert.setDateReception(dateReception.getValue());

            transfert.setStatus(comboStatus.getValue());

            // 🔥 IMPORTANT pour éviter erreurs SQL
            transfert.setFromOrg("BloodLink Central");
            transfert.setToOrg("Banque " + txtTo.getText());

            service.modifier(transfert);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Transfert modifié !");
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= VALIDATION =================
    private boolean validateForm() {

        StringBuilder errors = new StringBuilder();

        // FROM
        if (txtFrom.getText().isEmpty()) {
            errors.append("From ID obligatoire\n");
        }

        // TO
        if (txtTo.getText().isEmpty()) {
            errors.append("To ID obligatoire\n");
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

        // DATES
        if (dateEnvoie.getValue() == null) {
            errors.append("Date envoi obligatoire\n");
        }

        if (dateReception.getValue() == null) {
            errors.append("Date réception obligatoire\n");
        }

        // STATUS
        if (comboStatus.getValue() == null) {
            errors.append("Status obligatoire\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur validation");
            alert.setContentText(errors.toString());
            alert.show();
            return false;
        }

        return true;
    }

    // ================= RETOUR =================
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
