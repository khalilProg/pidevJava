package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.EntiteCollecteService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListeEntitesAdmin {

    @FXML private TableView<EntiteDeCollecte> tableView;
    @FXML private TableColumn<EntiteDeCollecte, Integer> idColumn;
    @FXML private TableColumn<EntiteDeCollecte, String> nomColumn;
    @FXML private TableColumn<EntiteDeCollecte, String> adresseColumn;
    @FXML private TableColumn<EntiteDeCollecte, String> villeColumn;
    @FXML private TableColumn<EntiteDeCollecte, String> telephoneColumn;
    @FXML private TableColumn<EntiteDeCollecte, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;

    private List<EntiteDeCollecte> baseList = new ArrayList<>();

    @FXML public void initialize() {
        sortComboBox.setItems(FXCollections.observableArrayList(
            "Nom (A-Z)", "Nom (Z-A)", "Ville"
        ));

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        searchField.textProperty().addListener((obs, old, val) -> appliquerFiltresEtTri());
        sortComboBox.valueProperty().addListener((obs, old, val) -> appliquerFiltresEtTri());


        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setStyle("-fx-font-weight: 800; -fx-alignment: center-left; -fx-text-fill: -admin-table-strong;");

        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: -admin-table-strong; -fx-font-size: 14px; -fx-alignment: center-left;");
                }
            }
        });

        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        adresseColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label locationLbl = new Label("📍 " + item);
                    locationLbl.getStyleClass().add("admin-table-muted");
                    // Assuming the mock needs a slightly red map pin
                    // (we can simulate it by wrapping just the pin in red if we used TextFlow,
                    // but simple Label text usually works just fine)
                    setGraphic(locationLbl);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        villeColumn.setCellValueFactory(new PropertyValueFactory<>("ville"));
        villeColumn.setStyle("-fx-alignment: center-left; -fx-text-fill: -admin-table-strong;");

        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("tel"));
        telephoneColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label phoneLbl = new Label("📞 " + item);
                    phoneLbl.getStyleClass().add("admin-table-strong");
                    setGraphic(phoneLbl);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Actions Column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button actViewBtn = createActionButton("fas-eye", "Voir", "action-icon-btn");
            private final Button actEditBtn = createActionButton("fas-edit", "Modifier", "action-icon-btn");
            private final Button actDeleteBtn = createActionButton("fas-trash", "Supprimer", "action-icon-btn", "action-icon-delete");
            private final HBox container = new HBox(8, actViewBtn, actEditBtn, actDeleteBtn);

            {
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(actViewBtn);
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(actEditBtn);
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(actDeleteBtn);

                container.setAlignment(Pos.CENTER_LEFT);

                actViewBtn.setOnAction(e -> {
                    EntiteDeCollecte ent = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ViewEntiteAdmin.fxml"));
                        Parent root = loader.load();
                        ViewEntiteAdmin controller = loader.getController();
                        controller.setEntite(ent);
                        
                        Stage stage = (Stage) actViewBtn.getScene().getWindow();
                        tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
                        stage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                actDeleteBtn.setOnAction(e -> {
                    EntiteDeCollecte ent = getTableView().getItems().get(getIndex());
                    try {
                        new EntiteCollecteService().supprimer(ent);
                        getTableView().getItems().remove(ent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                actEditBtn.setOnAction(e -> {
                    EntiteDeCollecte ent = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEntiteAdmin.fxml"));
                        Parent root = loader.load();
                        ModifierEntiteAdmin controller = loader.getController();
                        controller.setEntiteToEdit(ent);
                        
                        Stage stage = (Stage) actEditBtn.getScene().getWindow();
                        tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
                        stage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        // Load Data
        try {
            baseList = new EntiteCollecteService().recuperer();
            appliquerFiltresEtTri();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Button createActionButton(String iconLiteral, String tooltipText, String... styleClasses) {
        Button button = new Button();
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(13);
        icon.getStyleClass().add("ui-font-icon");
        button.setGraphic(icon);
        button.setText("");
        button.setTooltip(new Tooltip(tooltipText));
        button.setAccessibleText(tooltipText);
        button.getStyleClass().addAll(styleClasses);
        return button;
    }

    private void appliquerFiltresEtTri() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String sortType = sortComboBox.getValue();

        List<EntiteDeCollecte> filteredList = baseList.stream()
            .filter(e -> e.getNom().toLowerCase().contains(search) || 
                         e.getAdresse().toLowerCase().contains(search) ||
                         e.getVille().toLowerCase().contains(search))
            .collect(Collectors.toList());

        if (sortType != null) {
            switch (sortType) {
                case "Nom (A-Z)":
                    filteredList.sort(Comparator.comparing(EntiteDeCollecte::getNom, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "Nom (Z-A)":
                    filteredList.sort(Comparator.comparing(EntiteDeCollecte::getNom, String.CASE_INSENSITIVE_ORDER).reversed());
                    break;
                case "Ville":
                    filteredList.sort(Comparator.comparing(EntiteDeCollecte::getVille, String.CASE_INSENSITIVE_ORDER));
                    break;
            }
        }

        tableView.setItems(FXCollections.observableArrayList(filteredList));
    }

    @FXML private void handleAjouter(ActionEvent event) {
        navigateTo(event, "/AjouterEntiteAdmin.fxml");
    }


    // ── Navigation Handlers ──

    @FXML void handleLogout(ActionEvent event) {
        navigateTo(event, "/login.fxml");
    }

    @FXML void handleNavigateDashboard(ActionEvent event) {
        navigateTo(event, "/admin_dashboard.fxml");
    }

    @FXML void handleNavigateUsers(ActionEvent event) {
        navigateTo(event, "/admin_users.fxml");
    }

    @FXML void handleNavigateDemandes(ActionEvent event) {
        navigateTo(event, "/DemandeBackView.fxml");
    }

    @FXML void handleNavigateTransferts(ActionEvent event) {
        navigateTo(event, "/TransfertBackView.fxml");
    }

    @FXML void handleNavigateQuestionnaires(ActionEvent event) {
        navigateTo(event, "/ListeQuestAdmin.fxml");
    }

    @FXML void handleNavigateRendezVous(ActionEvent event) {
        navigateTo(event, "/ListeRdvAdmin.fxml");
    }

    @FXML void handleNavigateCampagnes(ActionEvent event) {
        navigateTo(event, "/ListeCampagnesAdmin.fxml");
    }

    @FXML void handleNavigateCollectes(ActionEvent event) {
        // Already here
    }

    private void navigateTo(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
