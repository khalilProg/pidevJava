package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.Questionnaire;
import tn.esprit.services.CampagneService;
import tn.esprit.services.QuestionnaireService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ListeQuestAdmin {

    @FXML private TableView<Questionnaire> tableView;
    @FXML private TableColumn<Questionnaire, String> nomColumn;
    @FXML private TableColumn<Questionnaire, String> prenomColumn;
    @FXML private TableColumn<Questionnaire, Integer> ageColumn;
    @FXML private TableColumn<Questionnaire, String> sexeColumn;
    @FXML private TableColumn<Questionnaire, Double> poidsColumn;
    @FXML private TableColumn<Questionnaire, String> autresColumn;
    @FXML private TableColumn<Questionnaire, LocalDateTime> dateColumn;
    @FXML private TableColumn<Questionnaire, String> campagneColumn;
    @FXML private TableColumn<Questionnaire, String> typeSangColumn;
    @FXML private TableColumn<Questionnaire, Void> actionsColumn;
    @FXML private Button addBtn;
    @FXML private TextField searchField;

    private ObservableList<Questionnaire> questionnairesData;

    @FXML
    public void initialize() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Initialize columns
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        sexeColumn.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        poidsColumn.setCellValueFactory(new PropertyValueFactory<>("poids"));
        autresColumn.setCellValueFactory(new PropertyValueFactory<>("autres"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeSangColumn.setCellValueFactory(new PropertyValueFactory<>("groupeSanguin"));

        // Campagne column uses service to get title
        campagneColumn.setCellValueFactory(cellData -> {
            try {
                String titre = new CampagneService().getCampagneById(cellData.getValue().getCampagneId()).getTitre();
                return new javafx.beans.property.SimpleStringProperty(titre);
            } catch (SQLException e) {
                e.printStackTrace();
                return new javafx.beans.property.SimpleStringProperty("error");
            }
        });

        // Actions column (delete button)
        actionsColumn.setCellFactory(col -> new TableCell<Questionnaire, Void>() {
            private final Button deleteBtn = new Button("Delete");

            {
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
                setGraphic(empty ? null : deleteBtn);
            }
        });

        // Load data from DB
        try {
            List<Questionnaire> list = new QuestionnaireService().recuperer();
            questionnairesData = FXCollections.observableArrayList(list);

            // --- Setup dynamic search ---
            FilteredList<Questionnaire> filteredData = new FilteredList<>(questionnairesData, p -> true);

            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                String lower = newVal.toLowerCase();

                filteredData.setPredicate(q -> {
                    if (lower.isEmpty()) return true;

                    // Search fields: nom, prenom, age, sexe, poids, groupe sanguin, campagne title
                    String campagne = "";
                    try {
                        campagne = new CampagneService()
                                .getCampagneById(q.getCampagneId())
                                .getTitre()
                                .toLowerCase();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return q.getNom().toLowerCase().contains(lower)
                            || q.getPrenom().toLowerCase().contains(lower)
                            || String.valueOf(q.getAge()).contains(lower)
                            || q.getSexe().toLowerCase().contains(lower)
                            || String.valueOf(q.getPoids()).contains(lower)
                            || q.getGroupeSanguin().toLowerCase().contains(lower)
                            || campagne.contains(lower) || q.getDate().toLocalDate().toString().contains(lower);
                });
            });

            SortedList<Questionnaire> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(tableView.comparatorProperty());
            tableView.setItems(sortedData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterQuestAdmin.fxml"));
            Parent root = loader.load();
            addBtn.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}