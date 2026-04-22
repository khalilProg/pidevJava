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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.CampagneService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListeCampagnesAdmin {

    @FXML private TableView<Campagne> tableView;
    @FXML private TableColumn<Campagne, Integer> idColumn;
    @FXML private TableColumn<Campagne, String> titreColumn;
    @FXML private TableColumn<Campagne, String> descColumn;
    @FXML private TableColumn<Campagne, Campagne> entiteColumn;
    @FXML private TableColumn<Campagne, String> typesSangColumn;
    @FXML private TableColumn<Campagne, Campagne> datesColumn;
    @FXML private TableColumn<Campagne, Void> actionsColumn;
    @FXML private Button addBtn;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        titreColumn.setCellFactory(col -> new TableCell<>() {
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

        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Entités
        entiteColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
        entiteColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Campagne c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setGraphic(null);
                } else {
                    FlowPane flow = new FlowPane();
                    flow.setHgap(5);
                    flow.setVgap(5);
                    if (c.getEntiteDeCollectes() != null) {
                        for (EntiteDeCollecte e : c.getEntiteDeCollectes()) {
                            Label badge = new Label(e.getNom());
                            badge.getStyleClass().add("badge-campagne"); // outline badge
                            flow.getChildren().add(badge);
                        }
                    }
                    setGraphic(flow);
                }
            }
        });

        // Types de sang (JSON -> Tags)
        typesSangColumn.setCellValueFactory(new PropertyValueFactory<>("typeSang"));
        typesSangColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setGraphic(null);
                } else {
                    FlowPane flow = new FlowPane();
                    flow.setHgap(5);
                    flow.setVgap(5);
                    // item is comma separated from jsonVersTypeSang, e.g. "A+, O-"
                    String[] types = item.split(",\\s*");
                    for (String t : types) {
                        if(!t.isEmpty()){
                            Label badge = new Label(t);
                            badge.getStyleClass().add("badge-groupe");
                            flow.getChildren().add(badge);
                        }
                    }
                    setGraphic(flow);
                }
            }
        });

        // Dates Column
        datesColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
        datesColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Campagne c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setGraphic(null);
                } else {
                    VBox box = new VBox(2);
                    box.setAlignment(Pos.CENTER_LEFT);
                    
                    String debutStr = c.getDateDebut() != null ? FORMATTER.format(c.getDateDebut()) : "";
                    String finStr = c.getDateFin() != null ? FORMATTER.format(c.getDateFin()) : "";
                    
                    Label debutLbl = new Label("📅 " + debutStr);
                    debutLbl.setStyle("-fx-text-fill: -primary; -fx-font-size: 11px;");
                    
                    Label finLbl = new Label("📅 " + finStr);
                    finLbl.setStyle("-fx-text-fill: -muted; -fx-font-size: 11px;");
                    
                    box.getChildren().addAll(debutLbl, finLbl);
                    setGraphic(box);
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

                actEditBtn.getStyleClass().add("action-icon-btn");

                actDeleteBtn.getStyleClass().addAll("action-icon-btn", "action-icon-delete");
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(actViewBtn);
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(actEditBtn);
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(actDeleteBtn);

                container.setAlignment(Pos.CENTER_LEFT);

                actViewBtn.setOnAction(e -> {
                    Campagne c = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ViewCampagneAdmin.fxml"));
                        Parent root = loader.load();
                        ViewCampagneAdmin controller = loader.getController();
                        controller.setCampagne(c);
                        
                        Stage stage = (Stage) actViewBtn.getScene().getWindow();
                        stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
                        stage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                actDeleteBtn.setOnAction(e -> {
                    Campagne c = getTableView().getItems().get(getIndex());
                    try {
                        new CampagneService().supprimer(c);
                        getTableView().getItems().remove(c);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                actEditBtn.setOnAction(e -> {
                    Campagne c = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCampagneAdmin.fxml"));
                        Parent root = loader.load();
                        ModifierCampagneAdmin controller = loader.getController();
                        controller.setCampagneToEdit(c);
                        
                        Stage stage = (Stage) actEditBtn.getScene().getWindow();
                        stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
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


        // Load data
        try {
            List<Campagne> campagnes = new CampagneService().recuperer();
            ObservableList<Campagne> data = FXCollections.observableArrayList(campagnes);
            tableView.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML private void handleAjouter(ActionEvent event) {
        navigateTo(event, "/AjouterCampagneAdmin.fxml");
    }

    // ── Navigation Handlers ──

    @FXML
    void handleLogout(ActionEvent event) {
        navigateTo(event, "/login.fxml");
    }

    @FXML
    void handleNavigateDashboard(ActionEvent event) {
        navigateTo(event, "/admin_dashboard.fxml");
    }

    @FXML
    void handleNavigateUsers(ActionEvent event) {
        navigateTo(event, "/admin_users.fxml");
    }

    @FXML
    void handleNavigateDemandes(ActionEvent event) {
        navigateTo(event, "/DemandeBackView.fxml");
    }

    @FXML
    void handleNavigateTransferts(ActionEvent event) {
        navigateTo(event, "/TransfertBackView.fxml");
    }

    @FXML
    void handleNavigateQuestionnaires(ActionEvent event) {
        navigateTo(event, "/ListeQuestAdmin.fxml");
    }

    @FXML
    void handleNavigateRendezVous(ActionEvent event) {
        navigateTo(event, "/ListeRdvAdmin.fxml");
    }
    
    @FXML
    void handleNavigateCampagnes(ActionEvent event) {
        // Already here
    }
    
    @FXML
    void handleNavigateCollectes(ActionEvent event) {
        navigateTo(event, "/ListeEntitesAdmin.fxml");
    }

    private void navigateTo(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
