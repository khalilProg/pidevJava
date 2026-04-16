package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.esprit.entities.Stock;
import tn.esprit.services.StockService;
import tn.esprit.tools.MyDatabase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AfficherStocksController {

    @FXML private Label lblTotalCount;
    @FXML private Button btnNouvelleStock;
    @FXML private TableView<Stock> tableStocks;
    @FXML private TableColumn<Stock, String> colTypeOrg;
    @FXML private TableColumn<Stock, Stock> colOrganisation;
    @FXML private TableColumn<Stock, String> colTypeSang;
    @FXML private TableColumn<Stock, Integer> colQuantite;
    @FXML private TableColumn<Stock, java.sql.Timestamp> colDate;
    @FXML private TableColumn<Stock, Stock> colActions;

    private StockService stockService = new StockService();
    private ObservableList<Stock> stockList = FXCollections.observableArrayList();
    private Connection cnx = MyDatabase.getInstance().getCnx();

    // Map: Type -> (ID -> Nom)
    private Map<String, Map<Integer, String>> orgNamesMap = new HashMap<>();

    @FXML
    public void initialize() {
        btnNouvelleStock.setOnAction(event -> switchScene("/AjoutStock.fxml", "BLOODLINK — Nouveau Stock"));
        
        loadOrganisationNames();
        setupTableColumns();
        loadStocks();
    }

    private void loadOrganisationNames() {
        orgNamesMap.put("banque", new HashMap<>());
        orgNamesMap.put("entitecollecte", new HashMap<>());

        try {
            Statement st = cnx.createStatement();
            
            // Load Banques
            ResultSet rsBanque = st.executeQuery("SELECT id, nom FROM banque");
            while (rsBanque.next()) {
                orgNamesMap.get("banque").put(rsBanque.getInt("id"), rsBanque.getString("nom"));
            }
            
            // Load Entité Collecte
            ResultSet rsEntite = st.executeQuery("SELECT id, nom FROM entite_collecte");
            while (rsEntite.next()) {
                orgNamesMap.get("entitecollecte").put(rsEntite.getInt("id"), rsEntite.getString("nom"));
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors du chargement des noms d'organisations : " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        // Type Org
        colTypeOrg.setCellValueFactory(new PropertyValueFactory<>("typeOrg"));
        colTypeOrg.setCellFactory(column -> new TableCell<Stock, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String display = item.equals("banque") ? "Banque" : "Entité Collecte";
                    Label lbl = new Label(display);
                    lbl.setStyle("-fx-text-fill: #cccccc;");
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        // Nom Organisation
        colOrganisation.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        colOrganisation.setCellFactory(column -> new TableCell<Stock, Stock>() {
            @Override
            protected void updateItem(Stock item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Map<Integer, String> idMap = orgNamesMap.get(item.getTypeOrg().toLowerCase());
                    String nom = idMap != null ? idMap.getOrDefault(item.getTypeOrgid(), "Inconnu") : "Inconnu";
                    
                    Label lbl = new Label(nom);
                    lbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        // Type Sang (Badge style)
        colTypeSang.setCellValueFactory(new PropertyValueFactory<>("typeSang"));
        colTypeSang.setCellFactory(column -> new TableCell<Stock, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().addAll("badge", "badge-red");
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        // Quantite
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colQuantite.setCellFactory(column -> new TableCell<Stock, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lbl = new Label(item + " Unités");
                    lbl.setStyle("-fx-text-fill: #cccccc; -fx-font-weight: bold;");
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        // Date
        colDate.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
        colDate.setCellFactory(column -> new TableCell<Stock, java.sql.Timestamp>() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(java.sql.Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lbl = new Label(sdf.format(item));
                    lbl.setStyle("-fx-text-fill: #888888;");
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        // Actions
        colActions.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        colActions.setCellFactory(new Callback<TableColumn<Stock, Stock>, TableCell<Stock, Stock>>() {
            @Override
            public TableCell<Stock, Stock> call(TableColumn<Stock, Stock> param) {
                return new TableCell<Stock, Stock>() {
                    private final Button btnEdit = new Button("🖍 ");
                    private final Button btnDelete = new Button("🗑 ");

                    {
                        btnEdit.getStyleClass().add("icon-button");
                        btnEdit.setOnAction(event -> {
                            Stock s = getTableView().getItems().get(getIndex());
                            handleEdit(s);
                        });

                        btnDelete.getStyleClass().add("icon-button-danger");
                        btnDelete.setOnAction(event -> {
                            Stock s = getTableView().getItems().get(getIndex());
                            handleDelete(s);
                        });
                    }

                    @Override
                    protected void updateItem(Stock item, boolean empty) {
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

    private void loadStocks() {
        try {
            stockList.clear();
            List<Stock> stocks = stockService.recuperer();
            stockList.addAll(stocks);
            tableStocks.setItems(stockList);
            updateCount();
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors du chargement des stocks : " + e.getMessage());
        }
    }

    private void updateCount() {
        lblTotalCount.setText(stockList.size() + " stock(s)");
    }

    private void handleEdit(Stock stock) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierStock.fxml"));
            Parent root = loader.load();
            
            ModifierStockController controller = loader.getController();
            controller.initData(stock);

            Stage stage = (Stage) tableStocks.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("BLOODLINK — Modifier le Stock");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Stock stock) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer ce stock");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce stock d'inventaire ? Cette action est irréversible.");

        // Motif Dark Theme
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");
        dialogPane.lookupAll(".label").forEach(node -> node.setStyle("-fx-text-fill: white;"));

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                stockService.supprimer(stock);
                stockList.remove(stock);
                updateCount();
            } catch (SQLException e) {
                System.out.println("❌ Erreur lors de la suppression du stock : " + e.getMessage());
            }
        }
    }

    private void switchScene(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) btnNouvelleStock.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToCommandes() {
         try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminAfficherCommandes.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnNouvelleStock.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("BLOODLINK — Gestion des Commandes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToStocks() {
        // Already here
    }
}
