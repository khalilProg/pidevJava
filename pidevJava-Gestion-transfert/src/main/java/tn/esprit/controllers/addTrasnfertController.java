package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.Demande;
import tn.esprit.entities.Transfert;
import tn.esprit.services.DemandeService;
import tn.esprit.services.TransfertService;

public class addTrasnfertController {
    private Demande demande;
    @FXML private TextField txtFrom;
    @FXML private TextField txtTo;
    @FXML private TextField txtQuantite;

    @FXML private DatePicker dateEnvoie;
    @FXML private DatePicker dateReception;

    @FXML private ComboBox<String> comboStatus;
    private TransfertService service = new TransfertService();

    public void setDemande(Demande demande) {
        this.demande = demande;
    }
    @FXML
    private void ajouterTransfert() {

        try {
            Transfert t = new Transfert();

            t.setDemande(demande);

            if (txtFrom.getText().isEmpty() || txtTo.getText().isEmpty()) {
                System.out.println("IDs obligatoires !");
                return;
            }

            t.setFromOrgId(Integer.parseInt(txtFrom.getText()));
            t.setToOrgId(Integer.parseInt(txtTo.getText()));

            if (txtQuantite.getText().isEmpty()) {
                System.out.println("Quantité obligatoire !");
                return;
            }

            t.setQuantite(Integer.parseInt(txtQuantite.getText()));

            if (dateEnvoie.getValue() != null) {
                t.setDateEnvoie(dateEnvoie.getValue());
            }

            if (dateReception.getValue() != null) {
                t.setDateReception(dateReception.getValue());
            }

            t.setStatus(
                comboStatus.getValue() != null ? comboStatus.getValue() : "EN_ATTENTE"
            );

            service.ajouter(t);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Transfert ajouté !");
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) txtFrom.getScene().getWindow();
        stage.close();
    }
}
