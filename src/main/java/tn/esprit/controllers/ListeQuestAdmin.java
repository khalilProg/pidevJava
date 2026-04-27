package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
    @FXML private TextField searchField;
    @FXML private ComboBox<String> bloodGroupFilter;
    @FXML private DatePicker dateFilter;

    private ObservableList<Questionnaire> questionnairesData = FXCollections.observableArrayList();

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
                    setStyle("-fx-font-weight: 800; -fx-text-fill: -admin-table-strong; -fx-font-size: 12px; -fx-alignment: center-left;");
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
                    setStyle("-fx-text-fill: -admin-table-muted; -fx-font-size: 12px; -fx-alignment: center-left;");
                }
            }
        });

        // Actions Column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button deleteBtn = new Button("Delete");

            {
                viewBtn.getStyleClass().add("action-icon-btn");
                deleteBtn.getStyleClass().addAll("action-icon-btn", "action-icon-delete");
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(viewBtn);
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(deleteBtn);
                viewBtn.setOnAction(e -> {
                    // Logic to view the questionnaire could go here. For now it's just visual.
                });
                deleteBtn.setOnAction(e -> {
                    Questionnaire q = getTableView().getItems().get(getIndex());
                    try {
                        new QuestionnaireService().supprimer(q);
                        questionnairesData.remove(q);
                    } catch (SQLException ex) {
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
                    HBox box = new HBox(8, viewBtn, deleteBtn);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });

        try {
            List<Questionnaire> questionnaires = new QuestionnaireService().recuperer();
            questionnairesData = FXCollections.observableArrayList(questionnaires);
            setupFilters();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void setupFilters() {
        if (bloodGroupFilter != null) {
            bloodGroupFilter.setItems(FXCollections.observableArrayList("", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        }

        FilteredList<Questionnaire> filteredData = new FilteredList<>(questionnairesData, p -> true);
        Runnable refresh = () -> filteredData.setPredicate(this::matchesFilters);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        }
        if (bloodGroupFilter != null) {
            bloodGroupFilter.valueProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        }
        if (dateFilter != null) {
            dateFilter.valueProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        }

        SortedList<Questionnaire> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    private boolean matchesFilters(Questionnaire q) {
        String query = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().toLowerCase().trim();
        String group = bloodGroupFilter == null ? null : bloodGroupFilter.getValue();

        if (group != null && !group.isBlank() && !group.equalsIgnoreCase(q.getGroupeSanguin())) {
            return false;
        }
        if (dateFilter != null && dateFilter.getValue() != null
                && (q.getDate() == null || !dateFilter.getValue().equals(q.getDate().toLocalDate()))) {
            return false;
        }
        if (query.isEmpty()) {
            return true;
        }

        String campagne = "";
        try {
            campagne = new CampagneService().getCampagneById(q.getCampagneId()).getTitre();
        } catch (SQLException ignored) {
        }

        return contains(q.getNom(), query)
                || contains(q.getPrenom(), query)
                || contains(q.getSexe(), query)
                || contains(q.getGroupeSanguin(), query)
                || contains(campagne, query)
                || String.valueOf(q.getAge()).contains(query)
                || String.valueOf(q.getPoids()).contains(query)
                || (q.getDate() != null && q.getDate().toString().toLowerCase().contains(query));
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    @FXML
    private void handleResetFilters() {
        if (searchField != null) {
            searchField.clear();
        }
        if (bloodGroupFilter != null) {
            bloodGroupFilter.getSelectionModel().clearSelection();
        }
        if (dateFilter != null) {
            dateFilter.setValue(null);
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
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
