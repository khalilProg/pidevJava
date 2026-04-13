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
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;
import tn.esprit.services.EntiteCollecteService;
import tn.esprit.services.QuestionnaireService;
import tn.esprit.services.RendezVousService;
import tn.esprit.services.CampagneService;

import java.io.IOException;
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
        private RendezVous rdv;
        private Questionnaire q;
        private String campagneNom;
        private String entiteNom;
        private LocalDateTime dateRdv;
        private String status;

        public Row(RendezVous rdv, Questionnaire q, String campagneNom, String entiteNom, LocalDateTime dateRdv, String status) {
            this.rdv = rdv;
            this.q = q;
            this.campagneNom = campagneNom;
            this.entiteNom = entiteNom;
            this.dateRdv = dateRdv;
            this.status = status;
        }
        public RendezVous getRdv() {return rdv;}
        public Questionnaire getQ() {return q;}
        public String getCampagneNom() { return campagneNom; }
        public String getEntiteNom() { return entiteNom; }
        public LocalDateTime getDateRdv() { return dateRdv.minusHours(1); }
        public String getStatus(){ return status; }

    }

    @FXML public void initialize() {
        try {
            List<RendezVous> rendezvous = new RendezVousService().recuperer();
            List<Row> rows = new ArrayList<>();

            for (RendezVous rdv : rendezvous) {
                if (!"annulé".equals(rdv.getStatus())) {
                    System.out.println("RendezVous ID: " + rdv.getId() + ", questionnaire_id: " + rdv.getQuestionnaire_id());
                    QuestionnaireService qs = new QuestionnaireService();
                    Questionnaire q = qs.getQuestionnaireById(rdv.getQuestionnaire_id());
                    String campagneTitre = new CampagneService().getCampagneById(q.getCampagneId()).getTitre();
                    EntiteCollecteService entiteService = new EntiteCollecteService();
                    EntiteDeCollecte entite = entiteService.getEntiteById(rdv.getEntite_id());
                    String entiteNom = entite.getNom();

                    rows.add(new Row(
                            rdv,
                            q,
                            campagneTitre,
                            entiteNom,
                            rdv.getDateDon(),
                            rdv.getStatus()
                    ));
                }
            }

            ObservableList<Row> data = FXCollections.observableArrayList(rows);

            campagneColumn.setCellValueFactory(new PropertyValueFactory<>("campagneNom"));
            entiteColumn.setCellValueFactory(new PropertyValueFactory<>("entiteNom"));
            dateRdvColumn.setCellValueFactory(new PropertyValueFactory<>("dateRdv"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            actionsColumn.setCellFactory(col -> new TableCell<>() {

                private final Button updateBtn = new Button("Update");
                private final Button deleteBtn = new Button("Delete");
                private final HBox container = new HBox(5, updateBtn, deleteBtn);

                {
                    deleteBtn.setOnAction(e -> {
                        Row row = getTableView().getItems().get(getIndex());
                        try {
                            boolean success = new RendezVousService().supprimerForClient(row.getRdv().getId());
                            if (success) {
                                getTableView().getItems().remove(row); // remove row from table view
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });

                    updateBtn.setOnAction(e -> {
                        try {
                            //to get el row
                            Row row = getTableView().getItems().get(getIndex());
                            // Fetch the actual Questionnaire and RendezVous from the DB
                            Questionnaire q = row.getQ();
                            RendezVous rdv = row.getRdv();

                            // Load the Update.fxml
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Update.fxml"));
                            Parent root = loader.load();

                            // Pass the data to the controller
                            Update controller = loader.getController();
                            controller.setData(q, rdv);
                            controller.setCampagne(new CampagneService().getCampagneById(q.getCampagneId()));

                            // Show the new scene
                            tableView.getScene().setRoot(root);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }

                    });
                }

                //to show el buttons
                @Override
                protected void updateItem(Void item, boolean empty) {
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