package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.esprit.entities.Commande;
import tn.esprit.services.CommandeService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherCommandesController implements Initializable {

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbStatutFiltre;
    @FXML private ComboBox<String> cbPrioriteFiltre;
    @FXML private Label lblCount;
    
    @FXML private TableView<Commande> tableCommandes;
    @FXML private TableColumn<Commande, Integer> colReference;
    @FXML private TableColumn<Commande, String> colTypeSang;
    @FXML private TableColumn<Commande, Integer> colQuantite;
    @FXML private TableColumn<Commande, String> colPriorite;
    @FXML private TableColumn<Commande, String> colStatut;

    private final CommandeService commandeService = new CommandeService();
    private ObservableList<Commande> commandesList;
    private FilteredList<Commande> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        setupTableColumns();
        loadDonnees();
    }

    private void setupFilters() {
        cbStatutFiltre.setItems(FXCollections.observableArrayList("Tous", "En attente", "Validée", "Annulée"));
        cbStatutFiltre.setValue("Tous");

        cbPrioriteFiltre.setItems(FXCollections.observableArrayList("Toutes", "Haute", "Moyenne", "Basse"));
        cbPrioriteFiltre.setValue("Toutes");

        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbStatutFiltre.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbPrioriteFiltre.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void setupTableColumns() {
        colReference.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colReference.setCellFactory(column -> new TableCell<Commande, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lbl = new Label("#" + item);
                    lbl.setStyle("-fx-text-fill: #cccccc; -fx-font-weight: bold;");
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colQuantite.setCellFactory(column -> new TableCell<Commande, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lbl = new Label(String.valueOf(item));
                    lbl.setStyle("-fx-text-fill: #cccccc; -fx-font-weight: bold;");
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        colTypeSang.setCellValueFactory(new PropertyValueFactory<>("typeSang"));
        colTypeSang.setCellFactory(column -> new TableCell<Commande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().addAll("badge", "badge-red");
                    setGraphic(lbl);
                }
            }
        });

        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colPriorite.setCellFactory(column -> new TableCell<Commande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().addAll("badge", "badge-yellow");
                    if ("Haute".equals(item)) {
                        lbl.getStyleClass().add("badge-red");
                    }
                    setGraphic(lbl);
                }
            }
        });

        colStatut.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatut.setCellFactory(column -> new TableCell<Commande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label("🕘 " + item);
                    lbl.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold; -fx-font-size: 12px;");
                    setGraphic(lbl);
                }
            }
        });
    }

    private void loadDonnees() {
        try {
            List<Commande> list = commandeService.recuperer();
            commandesList = FXCollections.observableArrayList(list);
            filteredData = new FilteredList<>(commandesList, p -> true);
            tableCommandes.setItems(filteredData);
            updateCount();
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors du chargement des commandes: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (filteredData == null) return;
        
        String searchText = tfSearch.getText() != null ? tfSearch.getText().toLowerCase() : "";
        String statut = cbStatutFiltre.getValue();
        String priorite = cbPrioriteFiltre.getValue();

        filteredData.setPredicate(commande -> {
            // Check Priorité
            if (priorite != null && !priorite.equals("Toutes") && !commande.getPriorite().equalsIgnoreCase(priorite)) {
                return false;
            }
            // Check Statut
            if (statut != null && !statut.equals("Tous") && !commande.getStatus().equalsIgnoreCase(statut)) {
                return false;
            }
            // Check Search Text
            if (searchText.isEmpty()) {
                return true;
            }
            String bloodType = commande.getTypeSang() != null ? commande.getTypeSang().toLowerCase() : "";
            String ref = String.valueOf(commande.getReference());
            return bloodType.contains(searchText) || ref.contains(searchText);
        });
        
        updateCount();
    }

    private void updateCount() {
        if (filteredData != null) {
            lblCount.setText(filteredData.size() + " commande(s)");
        }
    }

    @FXML
    private void handleNouvelleCommande() {
        switchScene("/AjoutCommande.fxml", "BLOODLINK — Créer une Commande");
    }

    private void switchScene(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) tfSearch.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            System.out.println("❌ Erreur de navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
