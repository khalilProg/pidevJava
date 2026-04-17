package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.esprit.entities.Stock;
import tn.esprit.services.StockService;
import tn.esprit.tools.MyDatabase;
import tn.esprit.tools.ThemeManager;

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
    @FXML private Button btnThemeToggle;
    @FXML private TextField tfSearch;
    @FXML private TableView<Stock> tableStocks;
    @FXML private TableColumn<Stock, Integer> colId;
    @FXML private TableColumn<Stock, String> colTypeSang;
    @FXML private TableColumn<Stock, Integer> colQuantite;
    @FXML private TableColumn<Stock, Stock> colOrganisation;
    @FXML private TableColumn<Stock, java.sql.Timestamp> colDate;
    @FXML private TableColumn<Stock, Stock> colActions;

    private StockService stockService = new StockService();
    private final ThemeManager themeManager = ThemeManager.getInstance();
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

        // Apply theme
        javafx.application.Platform.runLater(() -> {
            themeManager.applyTheme(tfSearch.getScene());
            themeManager.updateToggleButton(btnThemeToggle);
            refreshInlineStyles();
        });

        // Add animations
        tn.esprit.tools.AnimationUtils.animateNode(btnNouvelleStock, 100);
        tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnNouvelleStock);
        tn.esprit.tools.AnimationUtils.animateNode(tfSearch, 200);
        tn.esprit.tools.AnimationUtils.animateNode(tableStocks, 300);
    }

    @FXML
    private void handleThemeToggle() {
        themeManager.toggleTheme(btnThemeToggle.getScene());
        themeManager.updateToggleButton(btnThemeToggle);
        refreshInlineStyles();
        tableStocks.refresh();
    }

    private void refreshInlineStyles() {
        lblTotalCount.setStyle(themeManager.getCountLabelStyle());
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
        // ID Column
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setCellFactory(column -> new TableCell<Stock, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lbl = new Label("#" + item);
                    lbl.setStyle(themeManager.getReferenceChipStyle());
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
                    lbl.setStyle(themeManager.getTableBoldStyle());
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        // Organisation (shows type org as label, e.g. "BANQUE")
        colOrganisation.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        colOrganisation.setCellFactory(column -> new TableCell<Stock, Stock>() {
            @Override
            protected void updateItem(Stock item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String display = item.getTypeOrg().equalsIgnoreCase("banque") ? "BANQUE" : "ENTITÉ COLLECTE";
                    Label lbl = new Label(display);
                    lbl.setStyle(themeManager.getOrgLabelStyle());
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        // Date
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
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
                    lbl.setStyle(themeManager.getDateLabelStyle());
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
                    private final Button btnEdit = new Button("✏  MODIFIER");
                    private final Button btnDelete = new Button("🗑  SUPPRIMER");

                    {
                        btnEdit.getStyleClass().add("action-btn-edit");
                        tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnEdit);
                        btnEdit.setOnAction(event -> {
                            Stock s = getTableView().getItems().get(getIndex());
                            handleEdit(s);
                        });

                        btnDelete.getStyleClass().add("action-btn-delete");
                        tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnDelete);
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
            stage.getScene().setRoot(root);
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
        themeManager.styleDialog(dialogPane);

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
            stage.getScene().setRoot(root);
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
            stage.getScene().setRoot(root);
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
