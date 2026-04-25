package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.DossierMed;
import tn.esprit.services.ServiceDossierMed;

import java.net.URL;
import java.util.ResourceBundle;

public class DossierMedManageController implements Initializable {

    @FXML private TextField prenomField, nomField, ageField, bloodTypeField, tailleField, poidField, tempField, sexeField;
    private ServiceDossierMed service = new ServiceDossierMed();
    private DossierMed file;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        file = DossierMedListController.selectedDossierForEdit;
        if(file != null) {
            // LOAD DATA INTO FIELDS
            prenomField.setText(file.getPrenom());
            nomField.setText(file.getNom());
            ageField.setText(String.valueOf(file.getAge()));
            bloodTypeField.setText(file.getType_sang());
            tailleField.setText(String.valueOf(file.getTaille()));
            poidField.setText(String.valueOf(file.getPoid()));
            tempField.setText(String.valueOf(file.getTemperature()));
            sexeField.setText(file.getSexe());
        }
    }

    @FXML void handleUpdateFolder(ActionEvent event) {
        try {
            // MAP DATA FROM UI TO OBJECT
            file.setPrenom(prenomField.getText());
            file.setNom(nomField.getText());
            file.setAge(Integer.parseInt(ageField.getText()));
            file.setType_sang(bloodTypeField.getText());
            file.setTaille(Float.parseFloat(tailleField.getText()));
            file.setPoid(Float.parseFloat(poidField.getText()));
            file.setTemperature(Float.parseFloat(tempField.getText()));
            file.setSexe(sexeField.getText());

            service.modifier(file);
            closeWindow(event);

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Input Error: Age, Height, Weight and Temp must be numbers.").show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML void handleDeleteFolder(ActionEvent event) {
        try {
            service.supprimer(file.getId());
            closeWindow(event);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void closeWindow(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}