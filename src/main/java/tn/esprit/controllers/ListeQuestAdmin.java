package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
    @FXML private Button addBtn;

    @FXML public void initialize() {
        // Set up columns
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

        // Load data
        try {
            List<Questionnaire> questionnaires = new QuestionnaireService().recuperer();
            ObservableList<Questionnaire> data = FXCollections.observableArrayList(questionnaires);
            tableView.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleAjouter() {
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