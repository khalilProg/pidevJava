package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.RendezVous;
import tn.esprit.services.EntiteCollecteService;
import tn.esprit.services.RendezVousService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ListeRdvAdmin {

    @FXML private TableView<RendezVous> tableView;
    @FXML private TableColumn<RendezVous, Integer> idColumn;
    @FXML private TableColumn<RendezVous, LocalDateTime> dateColumn;
    @FXML private TableColumn<RendezVous, String> statusColumn;
    @FXML private TableColumn<RendezVous, Integer> qIdColumn;
    @FXML private TableColumn<RendezVous, String> entiteColumn;

    @FXML public void initialize() {
        // Set up columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateDon"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        qIdColumn.setCellValueFactory(new PropertyValueFactory<>("questionnaire_id"));
        entiteColumn.setCellValueFactory(new PropertyValueFactory<>("entite"));
        entiteColumn.setCellValueFactory(cellData -> {
            try {
                String entiteNom = new EntiteCollecteService()
                        .getEntiteById(cellData.getValue().getEntite_id())
                        .getNom();
                return new javafx.beans.property.SimpleStringProperty(entiteNom);
            } catch (SQLException e) {
                e.printStackTrace();
                return new javafx.beans.property.SimpleStringProperty("N/A");
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
}