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
import tn.esprit.services.EntiteCollecteService;
import tn.esprit.services.QuestionnaireService;
import tn.esprit.services.RendezVousService;

import java.io.IOException;
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
    @FXML private TableColumn<RendezVous, Void> actionsColumn;

    @FXML public void initialize() {
        RendezVousService rdvService = new RendezVousService();
        QuestionnaireService qsService = new QuestionnaireService();
        CampagneService campagneService = new CampagneService();
        EntiteCollecteService entiteService = new EntiteCollecteService();
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
        // Actions column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button updateBtn = new Button("Update");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(5, updateBtn, deleteBtn);

            {
                deleteBtn.setOnAction(e -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    try {
                        new RendezVousService().supprimer(rdv);
                        getTableView().getItems().remove(rdv);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                updateBtn.setOnAction(e -> {
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
                    updateBtn.setVisible(rdv.getDateDon().isAfter(LocalDateTime.now()));
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
}