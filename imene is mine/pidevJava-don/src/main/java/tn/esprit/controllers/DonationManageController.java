package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.Don;
import tn.esprit.services.ServiceDon;

import java.net.URL;
import java.util.ResourceBundle;

public class DonationManageController implements Initializable {
    @FXML private TextField qteField, typeField;
    @FXML private Label donHeaderIdInfo;

    private ServiceDon service = new ServiceDon();
    private Don sessionDon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sessionDon = DonationListController.selectedDonForEdit;
        if(sessionDon != null) {
            donHeaderIdInfo.setText("Tracking Code Reference: #" + sessionDon.getId());
            typeField.setText(sessionDon.getType_don());
            qteField.setText(String.valueOf(sessionDon.getQuantite()));
        }
    }

    @FXML void handleUpdateDonation(ActionEvent event) {
        try {
            float quantity = Float.parseFloat(qteField.getText()); // Validate input

            sessionDon.setType_don(typeField.getText());
            sessionDon.setQuantite(quantity);
            service.modifier(sessionDon);
            closeWindow(event);

        } catch (NumberFormatException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Input Error");
            errorAlert.setHeaderText("Invalid Data Format");
            errorAlert.setContentText("The 'Volume' field must be a valid number (e.g., 450.5).");
            errorAlert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "An error occurred while updating.").show();
        }
    }

    @FXML void handleDeleteDonation(ActionEvent event) {
        try {
            service.supprimer(sessionDon.getId());
            closeWindow(event);
        } catch(Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not delete the record from the database.").show();
        }
    }

    @FXML
    void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}