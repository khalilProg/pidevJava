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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.EntiteCollecteService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ListeEntitesAdmin {

    @FXML private TableView<EntiteDeCollecte> tableView;
    @FXML private TableColumn<EntiteDeCollecte, Integer> idColumn;
    @FXML private TableColumn<EntiteDeCollecte, String> nomColumn;
    @FXML private TableColumn<EntiteDeCollecte, String> adresseColumn;
    @FXML private TableColumn<EntiteDeCollecte, String> villeColumn;
    @FXML private TableColumn<EntiteDeCollecte, String> telephoneColumn;
    @FXML private TableColumn<EntiteDeCollecte, Void> actionsColumn;

    @FXML public void initialize() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("#" + item);
                    setStyle("-fx-font-weight: 800; -fx-text-fill: white; -fx-font-size: 12px;");
                }
            }
        });

        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
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
                    locationLbl.setStyle("-fx-text-fill: -muted; -fx-font-size: 12px;");
                    // Assuming the mock needs a slightly red map pin
                    // (we can simulate it by wrapping just the pin in red if we used TextFlow,
                    // but simple Label text usually works just fine)
                    setGraphic(locationLbl);
                }
            }
        });

        villeColumn.setCellValueFactory(new PropertyValueFactory<>("ville"));

        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("tel"));
        telephoneColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label phoneLbl = new Label("📞 " + item);
                    phoneLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                    setGraphic(phoneLbl);
                }
            }
        });

        // Actions Column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button actViewBtn = new Button("👁");
            private final Button actEditBtn = new Button("✏");
            private final Button actDeleteBtn = new Button("🗑");
            private final HBox container = new HBox(8, actViewBtn, actEditBtn, actDeleteBtn);

            {
                actViewBtn.getStyleClass().add("action-icon-btn");
                actViewBtn.setStyle("-fx-min-width: 32px; -fx-min-height: 32px; -fx-font-size: 14px; -fx-text-fill: #3498db;");

                actEditBtn.getStyleClass().add("action-icon-btn");
                actEditBtn.setStyle("-fx-min-width: 32px; -fx-min-height: 32px; -fx-font-size: 14px;");

                actDeleteBtn.getStyleClass().addAll("action-icon-btn", "action-icon-delete");
                actDeleteBtn.setStyle("-fx-min-width: 32px; -fx-min-height: 32px; -fx-font-size: 14px;");

                container.setAlignment(Pos.CENTER_LEFT);

                actViewBtn.setOnAction(e -> {
                    EntiteDeCollecte ent = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ViewEntiteAdmin.fxml"));
                        Parent root = loader.load();
                        ViewEntiteAdmin controller = loader.getController();
                        controller.setEntite(ent);
                        
                        Stage stage = (Stage) actViewBtn.getScene().getWindow();
                        stage.setScene(new Scene(root));
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
                        stage.setScene(new Scene(root));
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
            List<EntiteDeCollecte> entites = new EntiteCollecteService().recuperer();
            ObservableList<EntiteDeCollecte> data = FXCollections.observableArrayList(entites);
            tableView.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
