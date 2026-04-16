package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Stock;
import tn.esprit.services.StockService;
import tn.esprit.tools.MyDatabase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class ModifierStockController {

    @FXML private ComboBox<String> cbTypeOrg;
    @FXML private ComboBox<String> cbOrganisation;
    @FXML private ComboBox<String> cbTypeSang;
    @FXML private TextField txtQuantite;
    @FXML private Button btnAnnuler;
    @FXML private Button btnEnregistrer;

    private StockService stockService = new StockService();
    private Connection cnx = MyDatabase.getInstance().getCnx();
    
    // Maps UI names to IDs for organisations and types
    private Map<String, Integer> orgIdMap = new HashMap<>();
    private Map<String, String> mapTypeOrgToDb = new HashMap<>(); // UI to actual db value
    
    private Stock currentStock;

    @FXML
    public void initialize() {
        // Initialize Type Sang
        ObservableList<String> typesSang = FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"
        );
        cbTypeSang.setItems(typesSang);

        // Initialize Type Organisation
        mapTypeOrgToDb.put("Banque", "banque");
        mapTypeOrgToDb.put("Entité Collecte", "entitecollecte");
        cbTypeOrg.setItems(FXCollections.observableArrayList(mapTypeOrgToDb.keySet()));

        // Listen for type changes to populate specific organisations dynamically
        cbTypeOrg.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Determine if we're restoring state from initData
                String typeOrgDb = mapTypeOrgToDb.get(newVal);
                loadOrganisations(typeOrgDb);
                
                // If initData just ran and set currentStock, we select the ID
                if (currentStock != null && typeOrgDb.equals(currentStock.getTypeOrg())) {
                    // find the name corresponding to currentStock.getTypeOrgid()
                    for (Map.Entry<String, Integer> entry : orgIdMap.entrySet()) {
                        if (entry.getValue() == currentStock.getTypeOrgid()) {
                            cbOrganisation.setValue(entry.getKey());
                            break;
                        }
                    }
                }
            } else {
                cbOrganisation.getItems().clear();
                cbOrganisation.setPromptText("Sélectionnez un type d'abord");
            }
        });

        // Strict numeric input for quantite
        txtQuantite.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtQuantite.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Set up action listeners
        btnAnnuler.setOnAction(e -> navigateToAffichage());
        btnEnregistrer.setOnAction(e -> updateStock());
    }
    
    public void initData(Stock stock) {
        this.currentStock = stock;
        
        // Populate static fields
        cbTypeSang.setValue(stock.getTypeSang());
        txtQuantite.setText(String.valueOf(stock.getQuantite()));
        
        // This triggers the cbTypeOrg listener which will populate cbOrganisation and select the correct entity
        String uiTypeOrg = stock.getTypeOrg().equals("banque") ? "Banque" : "Entité Collecte";
        cbTypeOrg.setValue(uiTypeOrg);
    }

    private void loadOrganisations(String typeOrgDb) {
        ObservableList<String> orgNames = FXCollections.observableArrayList();
        orgIdMap.clear();

        try {
            String query = typeOrgDb.equals("banque") ? "SELECT id, nom FROM banque" : "SELECT id, nom FROM entite_collecte";
            ResultSet rs = cnx.createStatement().executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("nom");
                orgNames.add(name);
                orgIdMap.put(name, id);
            }
            cbOrganisation.setItems(orgNames);
            cbOrganisation.setPromptText("Choisir une organisation");
        } catch (SQLException e) {
            System.out.println("❌ Erreur de chargement des organisations: " + e.getMessage());
            showError("Erreur lors du chargement des " + typeOrgDb + "s.");
        }
    }

    private void updateStock() {
        // Validation
        if (cbTypeOrg.getValue() == null || cbTypeSang.getValue() == null || cbOrganisation.getValue() == null) {
            showError("Veuillez remplir tous les champs de sélection.");
            return;
        }

        if (txtQuantite.getText() == null || txtQuantite.getText().isEmpty()) {
            showError("Veuillez saisir une quantité.");
            return;
        }

        String typeOrgDb = mapTypeOrgToDb.get(cbTypeOrg.getValue());
        int orgId = orgIdMap.getOrDefault(cbOrganisation.getValue(), 0);
        String typeSang = cbTypeSang.getValue();
        int quantite = Integer.parseInt(txtQuantite.getText());

        if (quantite <= 0) {
            showError("Veuillez saisir une quantité valide (> 0).");
            return;
        }

        if (orgId == 0) {
            showError("Veuillez choisir une organisation valide.");
            return;
        }

        currentStock.setTypeOrgid(orgId);
        currentStock.setTypeOrg(typeOrgDb);
        currentStock.setTypeSang(typeSang);
        currentStock.setQuantite(quantite);
        currentStock.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        try {
            stockService.modifier(currentStock);
            System.out.println("✅ Modification Stock Réussie!");
            navigateToAffichage();
        } catch (SQLException e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            showError("Une erreur est survenue lors de l'enregistrement de la modification.");
        }
    }

    private void navigateToAffichage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherStocks.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAnnuler.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("BLOODLINK — Inventaire des Stocks");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to navigate to AfficherStocks.fxml");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Avertissement");
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");
        dialogPane.lookupAll(".label").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        alert.show();
    }
}
