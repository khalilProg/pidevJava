package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.entities.DossierMed;
import tn.esprit.services.AIService;
import tn.esprit.services.ServiceDossierMed;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DossierMedController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private TableView<DossierMed> mainTable;
    @FXML private TableColumn<DossierMed, Integer> colId, colAge;
    @FXML private TableColumn<DossierMed, String> colPrenom, colNom, colBlood;
    @FXML private TextField prenomField, nomField, ageField, bloodTypeField, searchField, poidField, tailleField;
    @FXML private Label aiResultLabel;

    private ServiceDossierMed service = new ServiceDossierMed();
    private AIService aiService = new AIService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colBlood.setCellValueFactory(new PropertyValueFactory<>("type_sang"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        loadTable();

        mainTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                prenomField.setText(newSel.getPrenom());
                nomField.setText(newSel.getNom());
                ageField.setText(String.valueOf(newSel.getAge()));
                bloodTypeField.setText(newSel.getType_sang());
                poidField.setText(String.valueOf(newSel.getPoid()));
                tailleField.setText(String.valueOf(newSel.getTaille()));
            }
        });
    }

    public void loadTable() {
        try { mainTable.setItems(FXCollections.observableArrayList(service.afficherAll())); }
        catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void runAIPrediction(ActionEvent event) {
        DossierMed selected = mainTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            aiResultLabel.setText("Please select a medical record from the table first.");
            return;
        }

        aiResultLabel.setText("🧠 AI is conducting deep clinical analysis...");

        new Thread(() -> {
            try {
                // Now directly passing the whole entity object!
                String result = aiService.analyzeDossierMed(selected);
                Platform.runLater(() -> aiResultLabel.setText(result));
            } catch (Exception e) {
                Platform.runLater(() -> aiResultLabel.setText("AI Link Offline: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    void handleAddFolder(ActionEvent event) {
        try {
            // Utilizing fully built object from Entity
            DossierMed dm = new DossierMed(
                    0, Float.parseFloat(tailleField.getText()), Float.parseFloat(poidField.getText()),
                    37.0f, "Inconnu", 190, nomField.getText(), prenomField.getText(),
                    Integer.parseInt(ageField.getText()), bloodTypeField.getText(), 1, 1 // defaults ID client/don for now
            );
            service.ajouter(dm);
            loadTable();
            handleClearFields();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML void handleUpdateFolder(ActionEvent event) {
        DossierMed selected = mainTable.getSelectionModel().getSelectedItem();
        if(selected != null) {
            try {
                selected.setPrenom(prenomField.getText());
                selected.setNom(nomField.getText());
                selected.setAge(Integer.parseInt(ageField.getText()));
                selected.setType_sang(bloodTypeField.getText());
                // Can also do taille and weight here by modifying your ServiceDossierMed's modifier query!
                service.modifier(selected);
                loadTable();
                handleClearFields();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML void handleDeleteFolder(ActionEvent event) {
        DossierMed selected = mainTable.getSelectionModel().getSelectedItem();
        if(selected != null) {
            try {
                service.supprimer(selected.getId());
                loadTable();
                handleClearFields();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML void handleRefresh() { loadTable(); }

    @FXML void handleClearFields() {
        prenomField.clear(); nomField.clear(); ageField.clear();
        bloodTypeField.clear(); poidField.clear(); tailleField.clear();
    }

    @FXML void goToDonations(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/DonationView.fxml"));
            ((Stage) rootPane.getScene().getWindow()).getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}