package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.DossierMed;
import tn.esprit.services.ServiceDossierMed;

import java.net.URL;
import java.util.ResourceBundle;

public class DossierMedManageController implements Initializable {

    @FXML private TextField prenomField, nomField;
    @FXML private TextField tailleField, poidField;
    @FXML private TextField bloodTypeField, tempField;
    @FXML private TextField ageField, sexeField;

    private ServiceDossierMed service = new ServiceDossierMed();
    private DossierMed currentRecord;

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    public void initData(DossierMed selectedRecord) {
        this.currentRecord = selectedRecord;
        if (currentRecord != null) {
            prenomField.setText(currentRecord.getPrenom());
            nomField.setText(currentRecord.getNom());
            tailleField.setText(String.valueOf(currentRecord.getTaille()));
            poidField.setText(String.valueOf(currentRecord.getPoid()));
            bloodTypeField.setText(currentRecord.getType_sang());
            tempField.setText(String.valueOf(currentRecord.getTemperature()));
            ageField.setText(String.valueOf(currentRecord.getAge()));
            sexeField.setText(currentRecord.getSexe());
        }
    }

    // 🔒 ======= METHODE DE CONTRÔLE DE SAISIE (VALIDATION BIO-MÉDICALE) =======
    private boolean isValidBioMedicalData() {
        String errorMsg = "";

        // Verify String purely Alphabetical format
        if (prenomField.getText().trim().isEmpty() || !prenomField.getText().matches("^[a-zA-Z\\s]+$")) {
            errorMsg += "• Patient 'First Name' format invalid. Alphabet letters only.\n";
        }
        if (nomField.getText().trim().isEmpty() || !nomField.getText().matches("^[a-zA-Z\\s]+$")) {
            errorMsg += "• Patient 'Last Name' format invalid. Alphabet letters only.\n";
        }

        // Biological Safety Constraints (Ranges)
        try {
            float t = Float.parseFloat(tailleField.getText());
            if (t < 50 || t > 250) errorMsg += "• Unrealistic Height value (Require between 50 - 250 cm).\n";
        } catch (NumberFormatException e) { errorMsg += "• Height demands structural floating numbers (e.g. 175.5)\n"; }

        try {
            float w = Float.parseFloat(poidField.getText());
            if (w < 40 || w > 350) errorMsg += "• Invalid Weight constraints. Medical criteria rejects extreme limits.\n";
        } catch (NumberFormatException e) { errorMsg += "• Weight demands structural numerical data.\n"; }

        try {
            float temp = Float.parseFloat(tempField.getText());
            if (temp < 32 || temp > 43) errorMsg += "• Temperature falls drastically outside survivability bio-metric limit (Use: 37.0).\n";
        } catch (NumberFormatException e) { errorMsg += "• Temperature metric needs strict number.\n"; }

        try {
            int age = Integer.parseInt(ageField.getText());
            if (age < 16 || age > 95) errorMsg += "• Extracted Bio-age restricted outside donation protocol (Min 16 / Max 95 allowed).\n";
        } catch (NumberFormatException e) { errorMsg += "• Age must exist as absolute integer calculation.\n"; }

        // Highly rated syntax check - strictly verifying standard Blood types using powerful REGEX logic!
        if (bloodTypeField.getText().trim().isEmpty() || !bloodTypeField.getText().toUpperCase().matches("^(A|B|AB|O)[+-]$")) {
            errorMsg += "• Corrupt Blood Type Input (Format Accepted purely as: A+, B-, AB+, O-, etc).\n";
        }

        // Biological sex strictly verified!
        String s = sexeField.getText().toLowerCase().trim();
        if (!s.equals("male") && !s.equals("female") && !s.equals("homme") && !s.equals("femme")) {
            errorMsg += "• Biometric protocol strictly expects specific values in Sexe format.\n";
        }

        if (errorMsg.isEmpty()) return true;

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Medical Validation Filter");
        alert.setHeaderText("Biometrics Safety Halt");
        alert.setContentText(errorMsg);
        alert.showAndWait();
        return false;
    }
    // 🔒 =========================================================================

    @FXML void handleUpdateFolder(ActionEvent event) {
        if (!isValidBioMedicalData()) return; // 🔥 Execution halted if Biometric forms are polluted.

        try {
            currentRecord.setPrenom(prenomField.getText());
            currentRecord.setNom(nomField.getText());
            currentRecord.setTaille(Float.parseFloat(tailleField.getText()));
            currentRecord.setPoid(Float.parseFloat(poidField.getText()));
            currentRecord.setType_sang(bloodTypeField.getText().toUpperCase()); // Auto upper-cases blood types
            currentRecord.setTemperature(Float.parseFloat(tempField.getText()));
            currentRecord.setAge(Integer.parseInt(ageField.getText()));
            currentRecord.setSexe(sexeField.getText());

            service.modifier(currentRecord);

            new Alert(Alert.AlertType.INFORMATION, "Target modified properly").showAndWait();
            closeWindow(event);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Backend failed validation string translation: " + e.getMessage()).show();
        }
    }

    @FXML void handleDeleteFolder(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Deconstruct medical ID entirely?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                try {
                    service.supprimer(currentRecord.getId());
                    closeWindow(event);
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    @FXML void closeWindow(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}