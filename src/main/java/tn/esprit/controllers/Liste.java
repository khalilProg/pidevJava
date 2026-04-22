package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.esprit.entities.*;
import tn.esprit.services.CampagneService;
import tn.esprit.services.EntiteCollecteService;
import tn.esprit.services.QuestionnaireService;
import tn.esprit.services.RendezVousService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Liste {

    @FXML private TableView<RendezVous> tableView;
    @FXML private TableColumn<RendezVous, String> campagneColumn;
    @FXML private TableColumn<RendezVous, String> entiteColumn;
    @FXML private TableColumn<RendezVous, LocalDateTime> dateRdvColumn;
    @FXML private TableColumn<RendezVous, String> statusColumn;
    @FXML private TableColumn<RendezVous, Void> actionsColumn;
    User u = new User(9,"chaffai", "yassine", "yassinechaffai4@gmail.com");
    private Client currentClient1 = new Client(2, "A-", LocalDate.of(2003, 10, 17), u);
    private Client currentClient = new Client(1, "O+", LocalDate.of(2023, 1, 1), u);

    @FXML
    public void initialize() {
        try {
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            RendezVousService rdvService = new RendezVousService();
            QuestionnaireService qsService = new QuestionnaireService();
            CampagneService campagneService = new CampagneService();
            EntiteCollecteService entiteService = new EntiteCollecteService();

            List<RendezVous> rendezvous = rdvService.recuperer();
            ObservableList<RendezVous> data = FXCollections.observableArrayList();

            // Only keep non-annulé
            for (RendezVous rdv : rendezvous) {
                Questionnaire q = qsService.getQuestionnaireById(rdv.getQuestionnaire_id());
                if (q.getClientId() == currentClient.getId() && !"annulé".equalsIgnoreCase(rdv.getStatus())) {
                    data.add(rdv);
                }
            }

            tableView.setItems(data);

            // Map columns
            campagneColumn.setCellValueFactory(cell -> {
                try {
                    Questionnaire q = qsService.getQuestionnaireById(cell.getValue().getQuestionnaire_id());
                    String titre = campagneService.getCampagneById(q.getCampagneId()).getTitre();
                    return new javafx.beans.property.SimpleStringProperty(titre);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return new javafx.beans.property.SimpleStringProperty("error");
                }
            });

            entiteColumn.setCellValueFactory(cell -> {
                try {
                    String entiteNom = entiteService.getEntiteById(cell.getValue().getEntite_id()).getNom();
                    return new javafx.beans.property.SimpleStringProperty(entiteNom);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return new javafx.beans.property.SimpleStringProperty("error");
                }
            });

            dateRdvColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getDateDon().minusHours(1)));
            statusColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));

            // Actions column
            actionsColumn.setCellFactory(col -> new TableCell<>() {
                private final Button updateBtn = new Button("Update");
                private final Button deleteBtn = new Button("Delete");
                private final HBox container = new HBox(5, updateBtn, deleteBtn);

                {
                    updateBtn.getStyleClass().add("action-btn-edit");
                    deleteBtn.getStyleClass().add("action-btn-delete");
                    tn.esprit.tools.AnimationUtils.applyHoverAnimation(updateBtn);
                    tn.esprit.tools.AnimationUtils.applyHoverAnimation(deleteBtn);
                    container.setAlignment(Pos.CENTER);
                    deleteBtn.setOnAction(e -> {
                        RendezVous rdv = getTableView().getItems().get(getIndex());
                        try {
                            if (rdvService.supprimerForClient(rdv.getId())) {
                                getTableView().getItems().remove(rdv);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });

                    updateBtn.setOnAction(e -> {
                        try {
                            RendezVous rdv = getTableView().getItems().get(getIndex());
                            Questionnaire q = qsService.getQuestionnaireById(rdv.getQuestionnaire_id());

                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Update.fxml"));
                            Parent root = loader.load();

                            Update controller = loader.getController();
                            controller.setData(q, rdv);
                            controller.setCampagne(campagneService.getCampagneById(q.getCampagneId()));

                            tableView.getScene().setRoot(root);
                        } catch (IOException | SQLException ex) {
                            throw new RuntimeException(ex);
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
                        setAlignment(Pos.CENTER);

                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
