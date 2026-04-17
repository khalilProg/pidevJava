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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;
import tn.esprit.services.CampagneService;
import tn.esprit.services.EntiteCollecteService;
import tn.esprit.services.QuestionnaireService;
import tn.esprit.services.RendezVousService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListeRdvAdmin {

    @FXML private TableView<RendezVous> tableView;
    @FXML private TableColumn<RendezVous, Integer> idColumn;
    @FXML private TableColumn<RendezVous, RendezVous> donneurColumn;
    @FXML private TableColumn<RendezVous, RendezVous> dateLieuColumn;
    @FXML private TableColumn<RendezVous, RendezVous> campagneColumn;
    @FXML private TableColumn<RendezVous, String> statusColumn;
    @FXML private TableColumn<RendezVous, Void> actionsColumn;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML public void initialize() {
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

        // Donneur Column
        donneurColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
        donneurColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(RendezVous rdv, boolean empty) {
                super.updateItem(rdv, empty);
                if (empty || rdv == null) {
                    setGraphic(null);
                } else {
                    try {
                        Questionnaire q = new QuestionnaireService().getQuestionnaireById(rdv.getQuestionnaire_id());
                        if (q != null) {
                            HBox box = new HBox(12);
                            box.setAlignment(Pos.CENTER_LEFT);
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
                        } else {
                            setGraphic(new Label("Inconnu"));
                        }
                    } catch (SQLException e) {
                        setGraphic(null);
                    }
                }
            }
        });

        // Date & Lieu Column
        dateLieuColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
        dateLieuColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(RendezVous rdv, boolean empty) {
                super.updateItem(rdv, empty);
                if (empty || rdv == null) {
                    setGraphic(null);
                } else {
                    try {
                        String entiteNom = new EntiteCollecteService().getEntiteById(rdv.getEntite_id()).getNom();
                        String dateStr = rdv.getDateDon() != null ? FORMATTER.format(rdv.getDateDon()) : "";
                        
                        VBox box = new VBox(2);
                        box.setAlignment(Pos.CENTER_LEFT);
                        
                        Label dateLbl = new Label("📅 " + dateStr);
                        dateLbl.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
                        
                        Label lieuLbl = new Label("📍 " + entiteNom);
                        lieuLbl.setStyle("-fx-text-fill: -muted; -fx-font-size: 11px;");
                        
                        box.getChildren().addAll(dateLbl, lieuLbl);
                        setGraphic(box);
                    } catch (SQLException e) {
                        setGraphic(new Label("Erreur"));
                    }
                }
            }
        });

        // Campagne Column
        campagneColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
        campagneColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(RendezVous rdv, boolean empty) {
                super.updateItem(rdv, empty);
                if (empty || rdv == null) {
                    setGraphic(null);
                } else {
                    try {
                        Questionnaire q = new QuestionnaireService().getQuestionnaireById(rdv.getQuestionnaire_id());
                        if(q != null) {
                           String titre = new CampagneService().getCampagneById(q.getCampagneId()).getTitre();
                           Label badge = new Label(titre);
                           badge.getStyleClass().add("badge-campagne");
                           setGraphic(badge);
                        } else {
                           setGraphic(null);
                        }
                    } catch (SQLException e) {
                        setGraphic(new Label("Erreur"));
                    }
                }
            }
        });

        // Status Column
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge-status"); // Yellow badge
                    setGraphic(badge);
                }
            }
        });

        // Actions column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button actFileBtn = new Button("📄");
            private final Button actEditBtn = new Button("✏");
            private final Button actDeleteBtn = new Button("🗑");
            private final HBox container = new HBox(8, actFileBtn, actEditBtn, actDeleteBtn);

            {   
                actFileBtn.getStyleClass().add("action-icon-btn");
                actFileBtn.setStyle("-fx-text-fill: #3b82f6; -fx-background-color: rgba(59, 130, 246, 0.1);");
                
                actEditBtn.getStyleClass().add("action-icon-btn");
                
                actDeleteBtn.getStyleClass().addAll("action-icon-btn", "action-icon-delete");

                container.setAlignment(Pos.CENTER_LEFT);

                actDeleteBtn.setOnAction(e -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    try {
                        new RendezVousService().supprimer(rdv);
                        getTableView().getItems().remove(rdv);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                actEditBtn.setOnAction(e -> {
                    try {
                        RendezVous rdv = getTableView().getItems().get(getIndex());
                        Questionnaire q = new QuestionnaireService().getQuestionnaireById(rdv.getQuestionnaire_id());

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateRdvAdmin.fxml"));
                        Parent root = loader.load();

                        UpdateRdvAdmin controller = loader.getController();

                        controller.setData(rdv, q);
                        controller.setCampagne(new CampagneService().getCampagneById(q.getCampagneId()));

                        tableView.getScene().setRoot(root);
                    } catch (IOException | SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    // Only show Edit if the appointment hasn't passed
                    actEditBtn.setVisible(rdv.getDateDon() != null && rdv.getDateDon().isAfter(LocalDateTime.now()));
                    actEditBtn.setManaged(actEditBtn.isVisible());
                    setGraphic(container);
                }
            }
        });


        // Load data
        try {
            List<RendezVous> rendezVous = new RendezVousService().recuperer();
            ObservableList<RendezVous> data = FXCollections.observableArrayList(rendezVous);
            tableView.setItems(data);
        } catch (SQLException e) {
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
        navigateTo(event, "/ListeQuestAdmin.fxml");
    }

    @FXML
    void handleNavigateRendezVous(ActionEvent event) {
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