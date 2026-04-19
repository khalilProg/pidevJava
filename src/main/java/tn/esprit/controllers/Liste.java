package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import tn.esprit.entities.*;
import tn.esprit.services.*;

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
    @FXML private Text campagnesCountText;
    @FXML private Text nextDonText;
    @FXML private TextField searchField;

    User u = new User(9,"chaffai", "yassine", "yassinechaffai4@gmail.com");
    private Client currentClient1 = new Client(2, "A-", LocalDate.of(2003, 10, 17), u);
    private Client currentClient = new Client(1, "O+", LocalDate.of(2023, 1, 1), u);
    @FXML
    public void initialize() {
        try {
            // --- Cards ---
            CampagneService campagneService = new CampagneService();
            DonService donService = new DonService();
            RendezVousService rdvService = new RendezVousService();
            QuestionnaireService qsService = new QuestionnaireService();
            EntiteCollecteService entiteService = new EntiteCollecteService();

            // Card 1: Nombre de campagnes
            int campagnesCount = donService.countDonByClient(currentClient.getId());
            campagnesCountText.setText(String.valueOf(campagnesCount));

            // Card 2: Prochain don possible
            LocalDate prochainDon = currentClient.getDernierDon().plusWeeks(3);
            LocalDate today = LocalDate.now();
            nextDonText.setText(!prochainDon.isAfter(today) ? "Eligible to donate" : "Not eligible to donate");

            // --- TableView setup ---
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            // Fetch all RendezVous for the current client (non-annulé)
            List<RendezVous> allRdvs = rdvService.recuperer();
            ObservableList<RendezVous> clientRdvs = FXCollections.observableArrayList();
            for (RendezVous rdv : allRdvs) {
                if (!"annulé".equalsIgnoreCase(rdv.getStatus()) &&
                        qsService.getQuestionnaireById(rdv.getQuestionnaire_id()).getClientId() == currentClient.getId()) {
                    clientRdvs.add(rdv);
                }
            }
            tableView.setItems(clientRdvs);

            // --- Column mappings ---
            campagneColumn.setCellValueFactory(cell -> {
                try {
                    Questionnaire q = qsService.getQuestionnaireById(cell.getValue().getQuestionnaire_id());
                    String titre = campagneService.getCampagneById(q.getCampagneId()).getTitre();
                    return new javafx.beans.property.SimpleStringProperty(titre);
                } catch (SQLException e) {
                    return new javafx.beans.property.SimpleStringProperty("error");
                }
            });

            entiteColumn.setCellValueFactory(cell -> {
                try {
                    return new javafx.beans.property.SimpleStringProperty(
                            entiteService.getEntiteById(cell.getValue().getEntite_id()).getNom());
                } catch (SQLException e) {
                    return new javafx.beans.property.SimpleStringProperty("error");
                }
            });

            dateRdvColumn.setCellValueFactory(cell ->
                    new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getDateDon().minusHours(1)));
            statusColumn.setCellValueFactory(cell ->
                    new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));

            // --- Actions column ---
            actionsColumn.setCellFactory(col -> new TableCell<>() {
                private final Button updateBtn = new Button("Update");
                private final Button deleteBtn = new Button("Delete");
                private final HBox container = new HBox(5, updateBtn, deleteBtn);

                {
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

            // Optional: Add search filtering
            FilteredList<RendezVous> filteredData = new FilteredList<>(clientRdvs, p -> true);
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                String lower = newVal.toLowerCase();
                filteredData.setPredicate(rdv -> {
                    try {
                        Questionnaire q = qsService.getQuestionnaireById(rdv.getQuestionnaire_id());
                        String campagneTitre = campagneService.getCampagneById(q.getCampagneId()).getTitre().toLowerCase();
                        String entiteNom = entiteService.getEntiteById(rdv.getEntite_id()).getNom().toLowerCase();
                        String dateStr = rdv.getDateDon().toLocalDate().toString();
                        String status = rdv.getStatus().toLowerCase();
                        return campagneTitre.contains(lower) || entiteNom.contains(lower) || dateStr.contains(lower) || status.contains(lower);
                    } catch (SQLException e) {
                        return false;
                    }
                });
            });
            SortedList<RendezVous> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(tableView.comparatorProperty());
            tableView.setItems(sortedData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}