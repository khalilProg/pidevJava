package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.Don;
import tn.esprit.services.ServiceDon;

public class DonationManageController {

    // PERFECT MAP TO YOUR FXML FILE
    @FXML private Label donHeaderIdInfo;
    @FXML private TextField qteField, typeField;
    @FXML private TextField entiteIdField;
    private ServiceDon service = new ServiceDon();
    private Don currentDon;

    // Called instantly when Modal opens to set up the form boxes!
    public void initData(Don selectedDonation) {
        this.currentDon = selectedDonation;

        donHeaderIdInfo.setText("Tracking Number #" + currentDon.getId());
        qteField.setText(String.valueOf(currentDon.getQuantite()));
        typeField.setText(currentDon.getType_don());
    }

    @FXML
    void handleUpdateDonation(ActionEvent event) {
        try {
            currentDon.setQuantite(Float.parseFloat(qteField.getText()));
            currentDon.setType_don(typeField.getText());

            // Exectue the overwrite logic via Service Layer
            service.modifier(currentDon);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Volume metrics and tags rewritten accurately.");
            alert.showAndWait();

            closeWindow(event);
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Warning: Volume field strictly demands valid numeric float.").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Backend Update Exception: " + e.getMessage()).show();
        }
    }

    @FXML
    void handleDeleteDonation(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Decompile this record from system log permanently?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    service.supprimer(currentDon.getId());
                    closeWindow(event);
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    // 🔥 ADDED THE CRITICAL @FXML ANNOTATION HERE SO JAVAFX CAN FIND IT
    @FXML
    void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}