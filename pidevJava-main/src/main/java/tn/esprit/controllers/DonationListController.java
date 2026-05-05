package tn.esprit.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import tn.esprit.entities.Don;
import tn.esprit.services.ServiceDon;
import tn.esprit.services.ServiceDossierMed;
import tn.esprit.tools.ComboItem;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DonationListController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private TableView<Don> donationTable;
    @FXML private TableColumn<Don, String> colClientName, colEntityName, colType;
    @FXML private TableColumn<Don, Float> colQte;

    @FXML private ComboBox<ComboItem> clientBox;
    @FXML private ComboBox<ComboItem> entityBox;
    @FXML private TextField typeField, qteField, searchField;

    private Don selectedDonationRow = null;

    private ServiceDon service = new ServiceDon();
    private ServiceDossierMed utilsService = new ServiceDossierMed();

    private List<ComboItem> allClientsCache;
    private List<ComboItem> allEntitiesCache;
    private ObservableList<Don> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // --- 1. FILL DROPDOWNS FROM DATABASE ---
        try {
            // Gets Patient Names from the DossierMed service
            allClientsCache = utilsService.getClientComboItems();
            if (allClientsCache != null) {
                clientBox.setItems(FXCollections.observableArrayList(allClientsCache));
            }

            // ✅ FIXED: This now correctly calls getEntiteComboItems() from the 'service' (ServiceDon) instance.
            allEntitiesCache = service.getEntiteComboItems();
            if (allEntitiesCache != null) {
                entityBox.setItems(FXCollections.observableArrayList(allEntitiesCache));
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "CRITICAL: Failed to load linked data from the database. Check connection and service queries.").show();
        }

        // --- 2. SETUP TABLE COLUMNS ---
        colType.setCellValueFactory(new PropertyValueFactory<>("type_don"));
        colQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        // Translate Client ID to Name
        colClientName.setCellValueFactory(cellData -> {
            int clientId = cellData.getValue().getId_client();
            String patientName = findLabelById(allClientsCache, clientId, "Unknown Client");
            return new SimpleStringProperty(patientName);
        });

        // Translate Entite ID to Name
        colEntityName.setCellValueFactory(cellData -> {
            int entiteId = cellData.getValue().getId_entite();
            String entiteName = findLabelById(allEntitiesCache, entiteId, "Unknown Branch");
            return new SimpleStringProperty(entiteName);
        });

        loadData();
        setupSearch();

        // --- 3. TABLE SELECTION LISTENER ---
        donationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, clickedRow) -> {
            if (clickedRow != null) {
                this.selectedDonationRow = clickedRow;
                selectComboItemById(clientBox, clickedRow.getId_client());
                selectComboItemById(entityBox, clickedRow.getId_entite());
                typeField.setText(clickedRow.getType_don());
                qteField.setText(String.valueOf(clickedRow.getQuantite()));
            }
        });
    }

    // --- HELPER METHODS for cleaner code ---
    private String findLabelById(List<ComboItem> list, int id, String defaultText) {
        if (list != null) {
            for (ComboItem item : list) {
                if (item.getId() == id) {
                    return item.getLabel();
                }
            }
        }
        return defaultText + " (ID:" + id + ")";
    }

    private void selectComboItemById(ComboBox<ComboItem> comboBox, int id) {
        if (comboBox.getItems() != null) {
            for (ComboItem item : comboBox.getItems()) {
                if (item.getId() == id) {
                    comboBox.getSelectionModel().select(item);
                    return;
                }
            }
        }
    }

    private void loadData() {
        try {
            masterData.clear();
            masterData.setAll(service.afficherAll());
            donationTable.setItems(masterData);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupSearch() {
        FilteredList<Don> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, newVal) -> {
            filteredData.setPredicate(don -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return don.getType_don().toLowerCase().contains(newVal.toLowerCase());
            });
        });
        SortedList<Don> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(donationTable.comparatorProperty());
        donationTable.setItems(sortedData);
    }

    @FXML void goToMedicalFolders(ActionEvent event) {
        try { rootPane.getScene().setRoot(FXMLLoader.load(getClass().getResource("/DossierMedList.fxml"))); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private boolean isDonationInputValid() {
        String errorMsg = "";
        if (clientBox.getValue() == null) errorMsg += "• A Patient must be selected.\n";
        if (entityBox.getValue() == null) errorMsg += "• The Extraction Branch must be selected.\n";
        if (typeField.getText() == null || typeField.getText().trim().isEmpty() || !typeField.getText().matches("^[a-zA-Z\\s]+$")) {
            errorMsg += "• Category must contain only letters (Ex: 'Whole Blood').\n";
        }
        try {
            float volume = Float.parseFloat(qteField.getText());
            if (volume < 50 || volume > 1500) errorMsg += "• Volume must be between 50ml and 1500ml.\n";
        } catch (NumberFormatException e) {
            errorMsg += "• Volume must be a valid number (e.g., 450.0).\n";
        }
        if (errorMsg.isEmpty()) return true;

        Alert alert = new Alert(Alert.AlertType.ERROR, errorMsg, ButtonType.OK);
        alert.setTitle("Input Validation Error");
        alert.setHeaderText("Please correct the form inputs:");
        alert.showAndWait();
        return false;
    }

    @FXML void handleAddDonation(ActionEvent event) {
        if (!isDonationInputValid()) return;
        try {
            service.ajouter(new Don(
                    0,
                    clientBox.getValue().getId(),
                    entityBox.getValue().getId(),
                    Float.parseFloat(qteField.getText()),
                    typeField.getText()
            ));
            clearForm();
            loadData();
            new Alert(Alert.AlertType.INFORMATION, "Donation logged successfully!").show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Database write error: " + ex.getMessage()).show();
        }
    }

    @FXML void handleUpdateDonation(ActionEvent event) {
        if (selectedDonationRow == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an entry from the table to update.").show();
            return;
        }
        if (!isDonationInputValid()) return;
        try {
            selectedDonationRow.setId_client(clientBox.getValue().getId());
            selectedDonationRow.setId_entite(entityBox.getValue().getId());
            selectedDonationRow.setType_don(typeField.getText());
            selectedDonationRow.setQuantite(Float.parseFloat(qteField.getText()));

            service.modifier(selectedDonationRow);
            clearForm();
            loadData();
            new Alert(Alert.AlertType.INFORMATION, "Record updated successfully.").show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void handleDeleteDonation(ActionEvent event) {
        if (selectedDonationRow != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this record permanently?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if(r == ButtonType.YES) {
                    try {
                        service.supprimer(selectedDonationRow.getId());
                        clearForm();
                        loadData();
                    } catch (Exception e) { e.printStackTrace(); }
                }
            });
        } else {
            new Alert(Alert.AlertType.WARNING, "Please select a record to delete.").show();
        }
    }

    private void clearForm() {
        clientBox.getSelectionModel().clearSelection();
        entityBox.getSelectionModel().clearSelection();
        typeField.clear();
        qteField.clear();
        selectedDonationRow = null;
        donationTable.getSelectionModel().clearSelection();
    }
}