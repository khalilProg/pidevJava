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
import tn.esprit.entities.DossierMed;
import tn.esprit.services.AIService;
import tn.esprit.services.ServiceDossierMed;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DossierMedListController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private TableView<DossierMed> mainTable;

    // 🔥 ERADICATED: `colId` was permanently removed for purely Name-based displays!
    @FXML private TableColumn<DossierMed, Integer> colAge;
    @FXML private TableColumn<DossierMed, String> colPrenom, colNom, colBlood, colSexe;
    @FXML private TableColumn<DossierMed, Float> colTaille, colTemp;

    @FXML private TextField searchField;
    @FXML private Label aiResultLabel;

    private ServiceDossierMed service = new ServiceDossierMed();
    private AIService aiService = new AIService();
    private ObservableList<DossierMed> masterDossierList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Table injections (Notice 'id' column mapping was destroyed completely)
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colBlood.setCellValueFactory(new PropertyValueFactory<>("type_sang"));
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
                String lowerSearchText = newValue.toLowerCase();

                return dossier.getPrenom().toLowerCase().contains(lowerSearchText) ||
                        dossier.getNom().toLowerCase().contains(lowerSearchText) ||
                        dossier.getType_sang().toLowerCase().contains(lowerSearchText);
            });
        });
        SortedList<DossierMed> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(mainTable.comparatorProperty());
        mainTable.setItems(sortedData);
    }

    @FXML
    void runAIPrediction(ActionEvent event) {
        DossierMed selected = mainTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            aiResultLabel.setText("⚠️ You must physically target a genetic profile first.");
            return;
        }

        aiResultLabel.setText("🧠 Neural link calibrating... Invoking Gemini framework...");

        // Memory leak preventative thread launcher
        Thread aiTask = new Thread(() -> {
            try {
                String response = aiService.analyzeDossierMed(selected);
                Platform.runLater(() -> aiResultLabel.setText(response));
            } catch (Exception e) {
                Platform.runLater(() -> aiResultLabel.setText("❌ Terminal disconnected from mainframe offline."));
            }
        });
        aiTask.setDaemon(true);
        aiTask.start();
    }

    @FXML
    void goToDonationLogs(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/DonationList.fxml"));
            rootPane.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openAddMedicalView(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/DossierMedAdd.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Onboard New Target Patient");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadMasterData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openManageMedicalView(ActionEvent event) {
        DossierMed selected = mainTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Choose an assigned target to begin modifications.").show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DossierMedManage.fxml"));
            Parent root = loader.load();

            DossierMedManageController controller = loader.getController();
            controller.initData(selected);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Security Override: Biometric Manipulation");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadMasterData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}