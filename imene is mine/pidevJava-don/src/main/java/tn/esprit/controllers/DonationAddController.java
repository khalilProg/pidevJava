package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Don;
import tn.esprit.services.ServiceDon;
import tn.esprit.tools.ComboItem;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DonationAddController implements Initializable {

    @FXML private TextField qteField;
    @FXML private ComboBox<String> typeFieldBox;
    @FXML private ComboBox<ComboItem> clientBox, entiteBox;

    private ServiceDon service = new ServiceDon();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        typeFieldBox.setItems(FXCollections.observableArrayList("Whole Blood", "Plasma", "Platelets", "RBCs"));

        try {
            clientBox.setItems(FXCollections.observableArrayList(service.getClientComboItems()));
            entiteBox.setItems(FXCollections.observableArrayList(service.getEntiteComboItems()));
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load database connections.").show();
        }
    }

    @FXML
    void handleAddDonation(ActionEvent event) {
        try {
            if (typeFieldBox.getValue() == null || clientBox.getValue() == null || entiteBox.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Please select values from all dropdown menus.").show();
                return;
            }

            int clientId = clientBox.getValue().getId();
            int entityId = entiteBox.getValue().getId();
            float quantity = Float.parseFloat(qteField.getText());

            if (quantity < 100 || quantity > 600) {
                new Alert(Alert.AlertType.WARNING, "Extracted volume must be between 100ml and 600ml.").show();
                return;
            }

            Don d = new Don(0, clientId, entityId, quantity, typeFieldBox.getValue());
            service.ajouter(d);
            closeWindow(event);

        } catch(NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Volume must be strictly numeric.").show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database Insertion Failed: " + e.getMessage()).show();
        }
    }

    @FXML
    void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}