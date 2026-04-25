package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.entities.DossierMed;
import tn.esprit.services.AIService;
import tn.esprit.services.ServiceDossierMed;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DossierMedListController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private TableView<DossierMed> mainTable;

    @FXML private TableColumn<DossierMed, Integer> colId, colAge;
    @FXML private TableColumn<DossierMed, String> colPrenom, colNom, colBlood, colSexe;
    @FXML private TableColumn<DossierMed, Float> colTaille, colTemp;

    @FXML private TextField searchField;
    @FXML private Label aiResultLabel;

    private ServiceDossierMed service = new ServiceDossierMed();
    private AIService aiService = new AIService();
    public static DossierMed selectedDossierForEdit = null;
    private ObservableList<DossierMed> masterDossierList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colBlood.setCellValueFactory(new PropertyValueFactory<>("type_sang"));

        // NEW COLUMNS MAPPED HERE
        colSexe.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        colTaille.setCellValueFactory(new PropertyValueFactory<>("taille"));
        colTemp.setCellValueFactory(new PropertyValueFactory<>("temperature"));

        loadMasterData();
        initializeSearchFilter();
    }

    private void loadMasterData() {
        try {
            masterDossierList.clear();
            masterDossierList.setAll(service.afficherAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeSearchFilter() {
        FilteredList<DossierMed> filteredData = new FilteredList<>(masterDossierList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(dossier -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return dossier.getPrenom().toLowerCase().contains(lower) ||
                        dossier.getNom().toLowerCase().contains(lower);
            });
        });
        SortedList<DossierMed> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(mainTable.comparatorProperty());
        mainTable.setItems(sortedData);
    }

    @FXML void runAIPrediction(ActionEvent event) {
        DossierMed selected = mainTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            aiResultLabel.setText("⚠️ Please select a patient profile first.");
            return;
        }
        aiResultLabel.setText("🧠 AI Clinical engine conducting biometric scan...");
        new Thread(() -> {
            try {
                String response = aiService.analyzeDossierMed(selected);
                Platform.runLater(() -> aiResultLabel.setText(response));
            } catch (Exception e) {
                Platform.runLater(() -> aiResultLabel.setText("❌ Offline diagnostic mode active."));
            }
        }).start();
    }

    @FXML void goToDonationLogs(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/DonationList.fxml"));
            rootPane.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML void openAddMedicalView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/DossierMedAdd.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadMasterData(); // Refresh after adding
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML void openManageMedicalView(ActionEvent event) {
        selectedDossierForEdit = mainTable.getSelectionModel().getSelectedItem();
        if (selectedDossierForEdit == null) {
            new Alert(Alert.AlertType.WARNING, "Choose a patient to modify.").show();
            return;
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/DossierMedManage.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadMasterData(); // Refresh after edit/delete
        } catch (IOException e) { e.printStackTrace(); }
    }
}