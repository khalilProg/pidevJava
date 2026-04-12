package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;
import tn.esprit.services.EntiteCollecteService;
import tn.esprit.services.QuestionnaireService;
import tn.esprit.services.RendezVousService;
import tn.esprit.services.CampagneService;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Liste {

    @FXML private TableView<Row> tableView;
    @FXML private TableColumn<Row, String> campagneColumn;
    @FXML private TableColumn<Row, String> entiteColumn;
    @FXML private TableColumn<Row, String> dateRdvColumn;
    @FXML private TableColumn<Row, String> statusColumn;
    @FXML private TableColumn<Row, Void> actionsColumn;

    public static class Row {
        private String campagneNom;
        private String entiteNom;
        private LocalDateTime dateRdv;
        private String status;

        public Row(String campagneNom, String entiteNom, LocalDateTime dateRdv, String status) {
            this.campagneNom = campagneNom;
            this.entiteNom = entiteNom;
            this.dateRdv = dateRdv;
            this.status = status;
        }

        public String getCampagneNom() { return campagneNom; }
        public String getEntiteNom() { return entiteNom; }
        public LocalDateTime getDateRdv() { return dateRdv; }
        public String getStatus() { return status; }
    }

    @FXML
    public void initialize() {
        try {
            List<RendezVous> rendezvous = new RendezVousService().recuperer();
            List<Row> rows = new ArrayList<>();

            for (RendezVous rdv : rendezvous) {
                System.out.println("RendezVous ID: " + rdv.getId() + ", questionnaire_id: " + rdv.getQuestionnaire_id());
                QuestionnaireService qs = new QuestionnaireService();
                Questionnaire q = qs.getQuestionnaireById(rdv.getQuestionnaire_id());
                String campagneTitre = new CampagneService().getCampagneById(q.getCampagneId()).getTitre();
                EntiteCollecteService entiteService = new EntiteCollecteService();
                EntiteDeCollecte entite = entiteService.getEntiteById(rdv.getEntite_id());
                String entiteNom = entite.getNom();

                rows.add(new Row(
                        campagneTitre,
                        entiteNom,
                        rdv.getDateDon(),
                        rdv.getStatus()
                ));
            }

            ObservableList<Row> data = FXCollections.observableArrayList(rows);

            campagneColumn.setCellValueFactory(new PropertyValueFactory<>("campagneNom"));
            entiteColumn.setCellValueFactory(new PropertyValueFactory<>("entiteNom"));
            dateRdvColumn.setCellValueFactory(new PropertyValueFactory<>("dateRdv"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            actionsColumn.setCellFactory(col -> new TableCell<Row, Void>() {

                private final Button updateBtn = new Button("Update");
                private final Button deleteBtn = new Button("Delete");
                private final HBox container = new HBox(5, updateBtn, deleteBtn);

                {
                    deleteBtn.setOnAction(e -> {
//                        Row row = getTableView().getItems().get(getIndex());
//                        System.out.println("Delete clicked for: " + row.getCampagneNom());
                    });

                    updateBtn.setOnAction(e -> {
//                        Row row = getTableView().getItems().get(getIndex());
//                        System.out.println("Update clicked for: " + row.getCampagneNom());
//                        // Here call your service to update
                    });
                }

                //to show el buttons
                @Override protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty) {
                        setGraphic(null);
                    } else {
                        Row row = getTableView().getItems().get(getIndex());
                        updateBtn.setVisible(row.getDateRdv().isAfter(LocalDateTime.now()));
                        setGraphic(container);
                    }
                }
            });

            tableView.setItems(data);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}