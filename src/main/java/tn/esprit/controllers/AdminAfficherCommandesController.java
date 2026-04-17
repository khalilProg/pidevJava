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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.esprit.entities.Commande;
import tn.esprit.services.CommandeService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AdminAfficherCommandesController implements Initializable {

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
    @FXML private TableColumn<Commande, Commande> colActions;

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
                    lbl.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: #cccccc; -fx-font-weight: bold; -fx-padding: 4 10 4 10; -fx-background-radius: 12;");
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
                    Label lbl = new Label(item.toUpperCase());
                    lbl.getStyleClass().add("badge");
                    if ("Haute".equalsIgnoreCase(item)) {
                        lbl.getStyleClass().add("badge-red");
                    } else if ("Moyenne".equalsIgnoreCase(item)) {
                        lbl.getStyleClass().add("badge-blue");
                    } else {
                        lbl.getStyleClass().add("badge-cyan");
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
                    Label lbl = new Label("● " + item.toUpperCase());
                    if ("Validée".equalsIgnoreCase(item) || "Approuvé".equalsIgnoreCase(item)) {
                        lbl.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 12px;");
                    } else if ("Annulée".equalsIgnoreCase(item) || "Refusé".equalsIgnoreCase(item)) {
                        lbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 12px;");
                    } else {
                        lbl.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold; -fx-font-size: 12px;");
                    }
                    setGraphic(lbl);
                }
            }
        });

        // Action Column: Edit & Delete Buttons (Admin)
        colActions.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        colActions.setCellFactory(new Callback<TableColumn<Commande, Commande>, TableCell<Commande, Commande>>() {
            @Override
            public TableCell<Commande, Commande> call(TableColumn<Commande, Commande> param) {
                return new TableCell<Commande, Commande>() {
                    private final Button btnEdit = new Button("✏  MODIFIER");
                    private final Button btnDelete = new Button("🗑  SUPPRIMER");

                    {
                        btnEdit.getStyleClass().add("action-btn-edit");
                        btnEdit.setOnAction(event -> {
                            Commande c = getTableView().getItems().get(getIndex());
                            handleEdit(c);
                        });

                        btnDelete.getStyleClass().add("action-btn-delete");
                        btnDelete.setOnAction(event -> {
                            Commande c = getTableView().getItems().get(getIndex());
                            handleDelete(c);
                        });
                    }

                    @Override
                    protected void updateItem(Commande item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            HBox box = new HBox(8, btnEdit, btnDelete);
                            box.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                            setGraphic(box);
                        }
                    }
                };
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

    private void handleEdit(Commande commande) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminModifierCommande.fxml"));
            Parent root = loader.load();
            
            AdminModifierCommandeController controller = loader.getController();
            controller.initData(commande);

            Stage stage = (Stage) tableCommandes.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("BLOODLINK — Gérer la Commande");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Commande commande) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la commande #" + commande.getReference());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette commande système ? Cette action est irréversible.");

        // Apply dark theme motif to alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");
        dialogPane.lookupAll(".label").forEach(node -> node.setStyle("-fx-text-fill: white;"));

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                commandeService.supprimer(commande);
                commandesList.remove(commande);
                updateCount();
            } catch (SQLException e) {
                System.out.println("❌ Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void goToCommandes() {
        // Already here
    }

    @FXML
    private void goToStocks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherStocks.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tfSearch.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("BLOODLINK — Inventaire des Stocks");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
