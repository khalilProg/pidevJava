package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.DossierMed;
import tn.esprit.services.ServiceDossierMed;
import tn.esprit.tools.ComboItem;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DossierMedAddController implements Initializable {

    @FXML private TextField prenomField, nomField, ageField, bloodTypeField, poidField, tailleField;

    // Updated to match fx:id="donBox" from your FXML
    @FXML private ComboBox<ComboItem> clientBox, donBox;
    @FXML private ComboBox<String> sexeBox;

    private ServiceDossierMed service = new ServiceDossierMed();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Populate ComboBoxes from database
            clientBox.setItems(FXCollections.observableArrayList(service.getClientComboItems()));
            donBox.setItems(FXCollections.observableArrayList(service.getDonComboItems()));
            sexeBox.setItems(FXCollections.observableArrayList("Male", "Female"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Auto-fill names when a patient is selected
        clientBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getLabel().contains(",")) {
                String[] parts = newVal.getLabel().split(",");
                nomField.setText(parts[0].trim());
                prenomField.setText(parts[1].trim());
            }
        });
    }

    /**
     * Handles the creation of the medical folder after validation.
     */
    @FXML
    void handleAddFolder(ActionEvent event) {
        if (!isCreationFormValid()) return;

        try {
            // Create the entity using IDs from selected ComboItems
            DossierMed dm = new DossierMed(
                    0,
                    Float.parseFloat(tailleField.getText()),
                    Float.parseFloat(poidField.getText()),
                    37.0f, // Default temperature
                    sexeBox.getValue(),
                    0,     // Default emergency contact
                    nomField.getText(),
                    prenomField.getText(),
                    Integer.parseInt(ageField.getText()),
                    bloodTypeField.getText().toUpperCase(),
                    clientBox.getValue().getId(),
                    donBox.getValue().getId()
            );

            service.ajouter(dm);
            new Alert(Alert.AlertType.INFORMATION, "Medical record created and linked successfully!").showAndWait();
            closeWindow(event);

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Submission Failed: " + e.getMessage()).show();
        }
    }

    /**
     * Validates all inputs before database submission.
     */
    private boolean isCreationFormValid() {
        StringBuilder errorMsg = new StringBuilder();

        if (clientBox.getValue() == null) errorMsg.append("• A Patient must be selected.\n");
        if (donBox.getValue() == null) errorMsg.append("• A Donation Log must be selected.\n");
        if (sexeBox.getValue() == null) errorMsg.append("• Biological Sex is required.\n");

        try {
            float t = Float.parseFloat(tailleField.getText());
            if (t < 50 || t > 250) errorMsg.append("• Height must be between 50 and 250 cm.\n");

            float w = Float.parseFloat(poidField.getText());
            if (w < 30 || w > 400) errorMsg.append("• Weight must be between 30 and 400 kg.\n");

            int age = Integer.parseInt(ageField.getText());
            if (age < 16 || age > 95) errorMsg.append("• Age must be between 16 and 95.\n");
        } catch (NumberFormatException e) {
            errorMsg.append("• Biometric fields must contain valid numbers.\n");
        }

        if (bloodTypeField.getText().trim().isEmpty() || !bloodTypeField.getText().toUpperCase().matches("^(A|B|AB|O)[+-]$")) {
            errorMsg.append("• Blood Type format is invalid (e.g., A+, O-).\n");
        }

        if (errorMsg.length() == 0) return true;

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Please correct the following fields:");
        alert.setContentText(errorMsg.toString());
        alert.showAndWait();
        return false;
    }

    @FXML
    void closeWindow(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}