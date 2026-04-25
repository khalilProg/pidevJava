package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.DossierMed;
import tn.esprit.services.ServiceDossierMed;
import tn.esprit.tools.ComboItem;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DossierMedAddController implements Initializable {

    @FXML private TextField prenomField, nomField, ageField, bloodTypeField, poidField, tailleField;
    @FXML private ComboBox<ComboItem> clientBox, donBox;

    private ServiceDossierMed service = new ServiceDossierMed();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            clientBox.setItems(FXCollections.observableArrayList(service.getClientComboItems()));
            donBox.setItems(FXCollections.observableArrayList(service.getDonComboItems()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Auto-fill Listener for Patient Names
        clientBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String selectedText = newValue.getLabel();
                if (selectedText.contains(",")) {
                    String[] parts = selectedText.split(",");
                    nomField.setText(parts[0].trim());
                    prenomField.setText(parts[1].trim());
                } else {
                    nomField.setText(selectedText);
                }
            }
        });
    }

    @FXML
    void handleAddFolder(ActionEvent event) {
        try {
            if (clientBox.getValue() == null || donBox.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "You must select a Patient and a Linked Donation.").show();
                return;
            }

            if (!prenomField.getText().matches("[a-zA-ZÀ-ÿ\\s]{2,}") || !nomField.getText().matches("[a-zA-ZÀ-ÿ\\s]{2,}")) {
                new Alert(Alert.AlertType.WARNING, "Name fields must contain only letters and be at least 2 characters long.").show();
                return;
            }

            String blood = bloodTypeField.getText().toUpperCase().trim();
            if (!blood.matches("^(A|B|AB|O)[+-]$")) {
                new Alert(Alert.AlertType.WARNING, "Invalid Blood Type. Use format: A+, O-, AB+, etc.").show();
                return;
            }

            int age = Integer.parseInt(ageField.getText());
            float poid = Float.parseFloat(poidField.getText());
            float taille = Float.parseFloat(tailleField.getText());

            int clientId = clientBox.getValue().getId();
            int donId = donBox.getValue().getId();

            DossierMed dm = new DossierMed(
                    0, taille, poid, 37.0f, "Non-Specified",
                    0, nomField.getText(), prenomField.getText(),
                    age, blood, clientId, donId
            );

            service.ajouter(dm);
            closeWindow(event);

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Age, Height, and Weight MUST be numeric values.").show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "SQL Error: " + e.getMessage()).show();
        }
    }

    @FXML
    void closeWindow(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}