package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.esprit.entities.DossierMed;
import tn.esprit.services.ServiceDossierMed;
import tn.esprit.tools.ComboItem;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DossierMedAddController implements Initializable {

    @FXML private TextField prenomField, nomField, ageField, bloodTypeField, poidField, tailleField;

    // 🔥 RENAMED 'donBox' TO 'entiteBox' to match the FXML and its logical purpose
    @FXML private ComboBox<ComboItem> clientBox, entiteBox;

    @FXML private ComboBox<String> sexeBox;

    private ServiceDossierMed service = new ServiceDossierMed();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // --- 1. Static Dropdown for Gender ---
        ObservableList<String> genders = FXCollections.observableArrayList("Male", "Female");
        sexeBox.setItems(genders);

        // Custom cell factory to ensure dark theme compatibility
        Callback<ListView<String>, ListCell<String>> cellFactory = lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: white; -fx-background-color: #333;");
            }
        };
        sexeBox.setCellFactory(cellFactory);
        sexeBox.setButtonCell(cellFactory.call(null));


        // --- 2. Dynamic Database Dropdowns ---
        try {
            // Fills the dropdown with all Patients
            clientBox.setItems(FXCollections.observableArrayList(service.getClientComboItems()));

            // 🔥 CORRECTED: Fills the 'entiteBox' with all Regions/Facilities
            entiteBox.setItems(FXCollections.observableArrayList(service.getDonComboItems())); // Assuming this fetches your 'entites'

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load data from the database. Please check your connection and services.").show();
        }

        // --- 3. Auto-fill names based on Patient selection ---
        clientBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String label = newVal.getLabel();
                if (label.contains(",")) {
                    String[] parts = label.split(",");
                    nomField.setText(parts[0].trim());
                    prenomField.setText(parts[1].trim());
                } else {
                    nomField.setText(label);
                }
            }
        });
    }

    // Input Validation Method (Contrôle de Saisie)
    private boolean isCreationFormValid() {
        String errorMsg = "";

        if (clientBox.getValue() == null) errorMsg += "• A Patient must be selected.\n";
        if (entiteBox.getValue() == null) errorMsg += "• The associated Facility/Region must be selected.\n";
        if (sexeBox.getValue() == null) errorMsg += "• Biological Sex is required.\n";

        if (prenomField.getText().trim().isEmpty() || !prenomField.getText().matches("^[a-zA-Z\\s]+$"))
            errorMsg += "• First Name must contain only letters.\n";

        if (nomField.getText().trim().isEmpty() || !nomField.getText().matches("^[a-zA-Z\\s]+$"))
            errorMsg += "• Last Name must contain only letters.\n";

        try {
            float t = Float.parseFloat(tailleField.getText());
            if (t < 50 || t > 250) errorMsg += "• Height must be a realistic value (50-250 cm).\n";
        } catch (NumberFormatException e) { errorMsg += "• Height must be a valid number.\n"; }

        try {
            float w = Float.parseFloat(poidField.getText());
            if (w < 30 || w > 400) errorMsg += "• Weight must be a realistic value (30-400 kg).\n";
        } catch (NumberFormatException e) { errorMsg += "• Weight must be a valid number.\n"; }

        try {
            int age = Integer.parseInt(ageField.getText());
            if (age < 16 || age > 95) errorMsg += "• Age must be between 16 and 95.\n";
        } catch (NumberFormatException e) { errorMsg += "• Age must be a valid integer.\n"; }

        if (bloodTypeField.getText().trim().isEmpty() || !bloodTypeField.getText().toUpperCase().matches("^(A|B|AB|O)[+-]$"))
            errorMsg += "• Blood Type format is invalid (e.g., A+, O-).\n";

        if (errorMsg.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the form inputs");
            alert.setContentText(errorMsg);
            alert.showAndWait();
            return false;
        }
    }

    @FXML
    void handleAddFolder(ActionEvent event) {
        if (!isCreationFormValid()) {
            return; // Stop if validation fails
        }

        try {
            // 🔥 CORRECTED: Now gets the ID from the correctly named 'entiteBox'
            DossierMed dm = new DossierMed(
                    0,
                    Float.parseFloat(tailleField.getText()),
                    Float.parseFloat(poidField.getText()),
                    37.0f,
                    sexeBox.getValue(),
                    0,
                    nomField.getText(),
                    prenomField.getText(),
                    Integer.parseInt(ageField.getText()),
                    bloodTypeField.getText().toUpperCase(),
                    clientBox.getValue().getId(),
                    entiteBox.getValue().getId() // The ID of the selected Facility/Region
            );

            service.ajouter(dm);

            new Alert(Alert.AlertType.INFORMATION, "Medical File created and linked successfully!").showAndWait();
            closeWindow(event);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Submission Failed: " + e.getMessage()).show();
        }
    }

    @FXML
    void closeWindow(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}