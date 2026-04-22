package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import tn.esprit.entities.Questionnaire;
import tn.esprit.services.CampagneService;
import tn.esprit.services.QuestionnaireService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListeQuestAdmin {

    @FXML private TableView<Questionnaire> tableView;
    @FXML private TableColumn<Questionnaire, Integer> idColumn;
    @FXML private TableColumn<Questionnaire, Questionnaire> donneurColumn;
    @FXML private TableColumn<Questionnaire, String> typeSangColumn;
    @FXML private TableColumn<Questionnaire, Integer> campagneColumn;
    @FXML private TableColumn<Questionnaire, LocalDateTime> dateColumn;
    @FXML private TableColumn<Questionnaire, Void> actionsColumn;
    @FXML private Button addBtn;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML public void initialize() {
        // Set up columns
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // ID Column
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

        // Donneur Column (Initials Circle + Name)
        donneurColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
        donneurColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Questionnaire q, boolean empty) {
                super.updateItem(q, empty);
                if (empty || q == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(12);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    String prenom = q.getPrenom() != null ? q.getPrenom().trim() : "";
                    String nom = q.getNom() != null ? q.getNom().trim() : "";
                    
                    String init1 = prenom.length() > 0 ? prenom.substring(0, 1).toUpperCase() : "";
                    String init2 = nom.length() > 0 ? nom.substring(0, 1).toUpperCase() : "";
                    
                    Label circle = new Label(init1 + init2);
                    circle.getStyleClass().add("initials-circle");
                    
                    Label nameLbl = new Label((nom.toUpperCase() + " " + prenom.toLowerCase()).trim());
                    nameLbl.getStyleClass().add("donneur-name");
                    
                    box.getChildren().addAll(circle, nameLbl);
                    setGraphic(box);
                }
            }
        });

        // Groupe Sanguin Column
        typeSangColumn.setCellValueFactory(new PropertyValueFactory<>("groupeSanguin"));
        typeSangColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge-groupe");
                    setGraphic(badge);
                }
            }
        });

        // Campagne Column
        campagneColumn.setCellValueFactory(new PropertyValueFactory<>("campagneId"));
        campagneColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    try {
                        String titre = new CampagneService().getCampagneById(item).getTitre();
                        Label badge = new Label(titre);
                        badge.getStyleClass().add("badge-campagne");
                        setGraphic(badge);
                    } catch (SQLException e) {
                        setGraphic(new Label("Erreur"));
                    }
                }
            }
        });

        // Date Column
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(FORMATTER.format(item));
                    setStyle("-fx-text-fill: -muted; -fx-font-size: 12px;");
                }
            }
        });

        // Actions Column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("👁");

            {
                viewBtn.getStyleClass().add("action-icon-btn");
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(viewBtn);
                viewBtn.setOnAction(e -> {
                    // Logic to view the questionnaire could go here. For now it's just visual.
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(viewBtn);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });

        // Load data
        try {
            List<Questionnaire> questionnaires = new QuestionnaireService().recuperer();
            ObservableList<Questionnaire> data = FXCollections.observableArrayList(questionnaires);
            tableView.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    @FXML private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterQuestAdmin.fxml"));
            Parent root = loader.load();
            addBtn.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        // Already here
    }

    @FXML
    void handleNavigateRendezVous(ActionEvent event) {
        navigateTo(event, "/ListeRdvAdmin.fxml");
    }

    @FXML
    void handleNavigateCampagnes(ActionEvent event) {
        navigateTo(event, "/ListeCampagnesAdmin.fxml");
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
