package tn.esprit.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import tn.esprit.entities.Client;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;
import tn.esprit.entities.User;
import tn.esprit.services.CampagneService;
import tn.esprit.services.ClientService;
import tn.esprit.services.DonService;
import tn.esprit.services.EntiteCollecteService;
import tn.esprit.services.QuestionnaireService;
import tn.esprit.services.RendezVousService;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Liste extends BaseFront {

    @FXML private TableView<RendezVous> tableView;
    @FXML private TableColumn<RendezVous, String> campagneColumn;
    @FXML private TableColumn<RendezVous, String> entiteColumn;
    @FXML private TableColumn<RendezVous, LocalDateTime> dateRdvColumn;
    @FXML private TableColumn<RendezVous, String> statusColumn;
    @FXML private TableColumn<RendezVous, Void> actionsColumn;
    @FXML private Text campagnesCountText;
    @FXML private Text nextDonText;
    @FXML private TextField searchField;

    private Client currentClient;

    @FXML
    public void initialize() {
        applySessionUser();

        try {
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            RendezVousService rdvService = new RendezVousService();
            QuestionnaireService qsService = new QuestionnaireService();
            CampagneService campagneService = new CampagneService();
            EntiteCollecteService entiteService = new EntiteCollecteService();
            DonService donService = new DonService();
            currentClient = resolveCurrentClient();

            ObservableList<RendezVous> data = loadClientRendezVous(rdvService, qsService);
            updateSummaryCards(donService);
            setupColumns(rdvService, qsService, campagneService, entiteService);
            setupSearch(data, qsService, campagneService, entiteService);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<RendezVous> loadClientRendezVous(RendezVousService rdvService,
                                                            QuestionnaireService qsService) throws SQLException {
        List<RendezVous> rendezvous = rdvService.recuperer();
        ObservableList<RendezVous> data = FXCollections.observableArrayList();
        if (currentClient == null) {
            return data;
        }

        for (RendezVous rdv : rendezvous) {
            Questionnaire q = qsService.getQuestionnaireById(rdv.getQuestionnaire_id());
            if (q != null && q.getClientId() == currentClient.getId() && !isCancelled(rdv.getStatus())) {
                data.add(rdv);
            }
        }
        return data;
    }

    private void updateSummaryCards(DonService donService) throws SQLException {
        if (campagnesCountText != null) {
            campagnesCountText.setText(currentClient == null ? "0" : String.valueOf(donService.countDonByClient(currentClient.getId())));
        }
        if (nextDonText != null) {
            if (currentClient == null) {
                nextDonText.setText("Profil client requis");
                return;
            }
            LocalDate dernierDon = currentClient.getDernierDon();
            if (dernierDon == null || !dernierDon.plusWeeks(3).isAfter(LocalDate.now())) {
                nextDonText.setText("Eligible au don");
            } else {
                nextDonText.setText(dernierDon.plusWeeks(3).toString());
            }
        }
    }

    private void setupColumns(RendezVousService rdvService, QuestionnaireService qsService,
                              CampagneService campagneService, EntiteCollecteService entiteService) {
        campagneColumn.setCellValueFactory(cell -> {
            try {
                Questionnaire q = qsService.getQuestionnaireById(cell.getValue().getQuestionnaire_id());
                return new SimpleStringProperty(campagneService.getCampagneById(q.getCampagneId()).getTitre());
            } catch (SQLException e) {
                return new SimpleStringProperty("Erreur");
            }
        });

        entiteColumn.setCellValueFactory(cell -> {
            try {
                return new SimpleStringProperty(entiteService.getEntiteById(cell.getValue().getEntite_id()).getNom());
            } catch (SQLException e) {
                return new SimpleStringProperty("Erreur");
            }
        });

        dateRdvColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDateDon()));
        statusColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));

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

                        tn.esprit.tools.ThemeManager.getInstance()
                                .setScene((javafx.stage.Stage) tableView.getScene().getWindow(), root);
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
                    updateBtn.setVisible(rdv.getDateDon() != null && rdv.getDateDon().isAfter(LocalDateTime.now()));
                    updateBtn.setManaged(updateBtn.isVisible());
                    setGraphic(container);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void setupSearch(ObservableList<RendezVous> data, QuestionnaireService qsService,
                             CampagneService campagneService, EntiteCollecteService entiteService) {
        FilteredList<RendezVous> filteredData = new FilteredList<>(data, p -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                String lower = newVal == null ? "" : newVal.toLowerCase().trim();
                filteredData.setPredicate(rdv -> matchesSearch(rdv, lower, qsService, campagneService, entiteService));
            });
        }

        SortedList<RendezVous> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    private boolean matchesSearch(RendezVous rdv, String lower, QuestionnaireService qsService,
                                  CampagneService campagneService, EntiteCollecteService entiteService) {
        if (lower.isEmpty()) {
            return true;
        }
        try {
            Questionnaire q = qsService.getQuestionnaireById(rdv.getQuestionnaire_id());
            String campagneTitre = campagneService.getCampagneById(q.getCampagneId()).getTitre();
            String entiteNom = entiteService.getEntiteById(rdv.getEntite_id()).getNom();
            return contains(campagneTitre, lower)
                    || contains(entiteNom, lower)
                    || contains(rdv.getStatus(), lower)
                    || (rdv.getDateDon() != null && rdv.getDateDon().toString().toLowerCase().contains(lower));
        } catch (SQLException e) {
            return false;
        }
    }

    private Client resolveCurrentClient() throws SQLException {
        User sessionUser = SessionManager.getCurrentUser();
        if (sessionUser != null) {
            Client sessionClient = new ClientService().getByUserId(sessionUser.getId());
            if (sessionClient != null) {
                return sessionClient;
            }
        }
        return null;
    }

    private boolean isCancelled(String status) {
        return status != null && (status.equalsIgnoreCase("annule")
                || status.equalsIgnoreCase("annulee")
                || status.equalsIgnoreCase("annulé")
                || status.equalsIgnoreCase("annulée"));
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase().contains(needle);
    }
}
