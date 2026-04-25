package tn.esprit.controllers;

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
import tn.esprit.entities.Don;
import tn.esprit.services.ServiceDon;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DonationListController implements Initializable {
    @FXML private StackPane rootPane;
    @FXML private TableView<Don> donationTable;
    @FXML private TableColumn<Don, Integer> colId, colClientId, colEntityId;
    @FXML private TableColumn<Don, String> colType;
    @FXML private TableColumn<Don, Float> colQte;
    @FXML private TextField searchField;

    private ServiceDon service = new ServiceDon();
    public static Don selectedDonForEdit = null;
    private ObservableList<Don> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClientId.setCellValueFactory(new PropertyValueFactory<>("id_client"));
        colEntityId.setCellValueFactory(new PropertyValueFactory<>("id_entite"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type_don"));
        colQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        loadMasterData();
        setupSearch();
    }

    private void loadMasterData() {
        try {
            masterData.setAll(service.afficherAll());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupSearch() {
        FilteredList<Don> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(don -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String low = newValue.toLowerCase();
                return don.getType_don().toLowerCase().contains(low) || String.valueOf(don.getId()).contains(low);
            });
        });
        SortedList<Don> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(donationTable.comparatorProperty());
        donationTable.setItems(sortedData);
    }

    @FXML void goToMedicalFolders(ActionEvent event) {
        switchScene("/DossierMedList.fxml");
    }

    @FXML void openAddDonationView(ActionEvent event) {
        openPopup("/DonationAdd.fxml", "Register Intake");
    }

    @FXML void openManageDonationView(ActionEvent event) {
        selectedDonForEdit = donationTable.getSelectionModel().getSelectedItem();
        if(selectedDonForEdit == null) {
            new Alert(Alert.AlertType.WARNING, "Select a record to modify.").show();
            return;
        }
        openPopup("/DonationManage.fxml", "Edit Logistics Record");
    }

    // --- REUSABLE UTILITIES TO PREVENT NullPointerExceptions ---

    private void switchScene(String fxmlPath) {
        try {
            URL path = getClass().getResource(fxmlPath);
            if (path == null) throw new IOException("Cannot find FXML: " + fxmlPath);
            Parent root = FXMLLoader.load(path);
            rootPane.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("❌ Critical Scene Error: " + e.getMessage());
        }
    }

    private void openPopup(String fxmlPath, String title) {
        try {
            URL path = getClass().getResource(fxmlPath);
            if (path == null) {
                new Alert(Alert.AlertType.ERROR, "File Missing: " + fxmlPath).show();
                return;
            }
            Parent root = FXMLLoader.load(path);
            Stage s = new Stage();
            s.setScene(new Scene(root));
            s.setTitle(title);
            s.initModality(Modality.APPLICATION_MODAL);
            s.showAndWait();
            loadMasterData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}