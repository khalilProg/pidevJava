package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;
import tn.esprit.services.CampagneService;
import tn.esprit.services.QuestionnaireService;
import tn.esprit.services.RendezVousService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ListeQuestAdmin {

    @FXML private TableView<Questionnaire> tableView;
    @FXML private TableColumn<Questionnaire, Integer> idColumn;
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

    @FXML public void initialize() {
        // Set up columns
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        sexeColumn.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        poidsColumn.setCellValueFactory(new PropertyValueFactory<>("poids"));
        autresColumn.setCellValueFactory(new PropertyValueFactory<>("autres"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        campagneColumn.setCellValueFactory(cellData -> {
            try {
                String titre = new CampagneService().getCampagneById(cellData.getValue().getCampagneId()).getTitre();
                return new javafx.beans.property.SimpleStringProperty(titre);
            } catch (SQLException e) {
                e.printStackTrace();
                return new javafx.beans.property.SimpleStringProperty("error");
            }
        });

        typeSangColumn.setCellValueFactory(new PropertyValueFactory<>("groupeSanguin"));
        // Actions column
        actionsColumn.setCellFactory(col -> new TableCell<Questionnaire, Void>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setOnAction(e -> {
                    // Get the current row
                    Questionnaire q = getTableView().getItems().get(getIndex());

                    try {
                        // Delete questionnaire (and its rendez-vous) from DB
                        new QuestionnaireService().supprimer(q);
                        // Remove it from the TableView
                        getTableView().getItems().remove(q);
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
            // Load the FXML for adding a questionnaire
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterQuestAdmin.fxml"));
            Parent root = loader.load();

            // Optionally, you can pass data to the new controller if needed
            // AjouterQuestionnaire controller = loader.getController();

            // Show the add form
            addBtn.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}