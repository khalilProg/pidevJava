package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.Client;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;
import tn.esprit.entities.User;
import tn.esprit.services.*;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AjouterRendezVous extends BaseFront {
    private Campagne campagne;
    @FXML private DatePicker dateRdv;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private Button confirmerRdv;
    @FXML private Button annulerRdv;
    private Questionnaire questionnaire;
    @FXML private Text entiteError;
    @FXML private Text dateError;
    @FXML private Text timeError;
    @FXML private ComboBox<EntiteDeCollecte> entiteCombo;
    private final ObservableList<EntiteDeCollecte> entites = FXCollections.observableArrayList();
    private UserService us = new UserService();
    private User currentclient = SessionManager.getCurrentUser();

    public void setCampagne(Campagne campagne) {
        this.campagne = campagne;
    }

    @FXML
    public void initialize() {
        applySessionUser();

        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        entiteCombo.setItems(entites);
        entiteCombo.setCellFactory(c -> createEntiteCell());
        entiteCombo.setButtonCell(createEntiteCell());
    }

    public void setEntities(List<EntiteDeCollecte> entitiesList) {
        entites.setAll(entitiesList == null ? List.of() : entitiesList);
    }

    public void setQuestionnaire(Questionnaire q) {
        this.questionnaire = q;
        System.out.println("questionnaire id : " + q.getId());
    }

    @FXML
    private void handleConfirmerRdv() throws SQLException, IOException {
        boolean valid = true;
        LocalDate date = dateRdv.getValue();
        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();

        entiteError.setVisible(false);
        dateError.setVisible(false);
        timeError.setVisible(false);

        EntiteDeCollecte selectedEntity = entiteCombo.getValue();

        if (selectedEntity == null) {
            entiteError.setText("Veuillez choisir une entite de collecte");
            entiteError.setVisible(true);
            valid = false;
        }

        if (date == null || date.isBefore(LocalDate.now()) || date.isBefore(campagne.getDateDebut()) || date.isAfter(campagne.getDateFin())) {
            dateError.setText("La date doit etre comprise entre aujourd'hui, " + campagne.getDateDebut() + " et " + campagne.getDateFin());
            dateError.setVisible(true);
            valid = false;
        }

        if (hour < 8 || hour > 17) {
            timeError.setText("L'heure doit etre entre 8h et 17h");
            timeError.setVisible(true);
            valid = false;
        }

        if (!valid) {
            return;
        }

        LocalDateTime rdvDateTime = date.atTime(hour, minute);
        int selectedId = selectedEntity.getId();
        int campagneId = campagne.getId();

        boolean dejaReserve = new RendezVousService().recuperer().stream()
                .anyMatch(r -> {
                    try {
                        return r.getEntite_id() == selectedId
                                && r.getDateDon().equals(rdvDateTime)
                                && new QuestionnaireService().getQuestionnaireById(r.getQuestionnaire_id()).getCampagneId() == campagneId;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

        if (dejaReserve) {
            dateError.setText("Ce creneau est deja reserve pour cette entite de collecte.");
            dateError.setVisible(true);
            return;
        }

        Client rdvClient = resolveClient(true);
        if (rdvClient == null) {
            dateError.setText("Aucun profil client n'est associe a cette session.");
            dateError.setVisible(true);
            return;
        }

        syncQuestionnaireClient(rdvClient);
        new QuestionnaireService().ajouter(questionnaire);
        int questionnaireId = questionnaire.getId();
        RendezVous rdv = new RendezVous("confirme", rdvDateTime, questionnaireId, selectedId);
        new RendezVousService().ajouter(rdv);
         askAddToGoogleCalendar(
                 currentclient.getNom() + " " + currentclient.getPrenom(),
                campagne.getTitre(),
                rdvDateTime,
                selectedEntity.getNom()
        );

        sendConfirmationEmail(rdv, selectedEntity, true);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Liste.fxml"));
        Parent root = loader.load();
        setRoot(root, confirmerRdv);
    }
private void askAddToGoogleCalendar(String patientName, String campagne, LocalDateTime dateTime, String entite) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Ajouter à Google Calendar");
        alert.setHeaderText("Voulez-vous ajouter ce rendez-vous à votre calendrier Google ?");
        alert.setContentText("Campagne: " + campagne + "\nDate & Heure: " + dateTime + "\nEntité: " + entite);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Run on background thread — the OAuth flow opens a browser and waits
                new Thread(() -> {
                    try {
                        GoogleCalendarOAuthService service = new GoogleCalendarOAuthService();
                        String link = service.addRendezVous(patientName, campagne, dateTime, entite);
                        javafx.application.Platform.runLater(() -> {
                            Alert confirm = new Alert(Alert.AlertType.INFORMATION);
                            confirm.setTitle("Rendez-vous ajouté");
                            confirm.setHeaderText("Rendez-vous ajouté à Google Calendar !");
                            confirm.setContentText("Vous pouvez voir votre événement ici:\n" + link);
                            confirm.show();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            Alert error = new Alert(Alert.AlertType.ERROR);
                            error.setTitle("Erreur Google Calendar");
                            error.setHeaderText("Impossible d'ajouter l'événement");
                            error.setContentText(e.getMessage());
                            error.show();
                        });
                    }
                }).start();
            }
        });
    }
    @FXML
    private void handleConfirmerRdvAdmin() throws SQLException, IOException {
        boolean valid = true;
        LocalDate date = dateRdv.getValue();
        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();

        entiteError.setVisible(false);
        dateError.setVisible(false);
        timeError.setVisible(false);

        EntiteDeCollecte selectedEntity = entiteCombo.getValue();

        if (selectedEntity == null) {
            entiteError.setText("Veuillez choisir une entite de collecte");
            entiteError.setVisible(true);
            valid = false;
        }

        if (date == null || date.isBefore(LocalDate.now()) || date.isBefore(campagne.getDateDebut()) || date.isAfter(campagne.getDateFin())) {
            dateError.setText("La date doit etre comprise entre aujourd'hui, " + campagne.getDateDebut() + " et " + campagne.getDateFin());
            dateError.setVisible(true);
            valid = false;
        }

        if (hour < 8 || hour > 17) {
            timeError.setText("L'heure doit etre entre 8h et 17h");
            timeError.setVisible(true);
            valid = false;
        }

        if (!valid) {
            return;
        }

        LocalDateTime rdvDateTime = date.atTime(hour, minute);
        int selectedId = selectedEntity.getId();
        int campagneId = campagne.getId();

        boolean dejaReserve = new RendezVousService().recuperer().stream()
                .anyMatch(r -> {
                    try {
                        return r.getEntite_id() == selectedId
                                && r.getDateDon().equals(rdvDateTime)
                                && new QuestionnaireService().getQuestionnaireById(r.getQuestionnaire_id()).getCampagneId() == campagneId;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

        if (dejaReserve) {
            dateError.setText("Ce creneau est deja reserve pour cette entite de collecte.");
            dateError.setVisible(true);
            return;
        }

        Client rdvClient = resolveClient(false);
        if (rdvClient == null) {
            dateError.setText("Aucun profil client n'est associe a ce questionnaire.");
            dateError.setVisible(true);
            return;
        }

        syncQuestionnaireClient(rdvClient);
        new QuestionnaireService().ajouter(questionnaire);
        int questionnaireId = questionnaire.getId();
        RendezVous rdv = new RendezVous("confirme", rdvDateTime, questionnaireId, selectedId);
        new RendezVousService().ajouter(rdv);
        sendConfirmationEmail(rdv, selectedEntity, false);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeRdvAdmin.fxml"));
        Parent root = loader.load();
        setRoot(root, confirmerRdv);
    }

    private void setupDatePickerConstraints(int entiteId) {
        try {
            // Fetch all appointments and the current campaign ID
            List<RendezVous> allRdvs = new RendezVousService().recuperer();
            int currentCampagneId = this.campagne.getId();
            QuestionnaireService qService = new QuestionnaireService();

            dateRdv.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) return;

                    // 1. Range Validation
                    boolean isOutOfRange = item.isBefore(LocalDate.now()) ||
                            item.isBefore(campagne.getDateDebut()) ||
                            item.isAfter(campagne.getDateFin());

                    // 2. Strict Filter: Same Entity AND Same Campaign
                    long count = allRdvs.stream()
                            .filter(r -> {
                                try {
                                    // Filter by Entity ID
                                    if (r.getEntite_id() != entiteId) return false;

                                    // Filter by Date
                                    if (!r.getDateDon().toLocalDate().equals(item)) return false;

                                    // Filter by Campaign ID (via the Questionnaire)
                                    Questionnaire q = qService.getQuestionnaireById(r.getQuestionnaire_id());
                                    return q != null && q.getCampagneId() == currentCampagneId;

                                } catch (SQLException e) {
                                    return false;
                                }
                            })
                            .count();

                    if (isOutOfRange || count >= 18) {
                        setDisable(true);
                        if (count >= 18) {
                            setStyle("-fx-background-color: #d3d3d3; -fx-text-fill: #808080;");
                            setTooltip(new Tooltip("Limite atteinte pour cette campagne (18/18)"));
                        }
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void handleAnnuler(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeCampagnes.fxml"));
        Parent root = loader.load();
        setRoot(root, annulerRdv);
    }

    @FXML
    public void btnAnnuler(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeRdvAdmin.fxml"));
        Parent root = loader.load();
        setRoot(root, annulerRdv);
    }

    private void sendConfirmationEmail(RendezVous rdv, EntiteDeCollecte selectedEntity, boolean preferSessionUser) {
        try {
            Client client = resolveClient(preferSessionUser);
            if (client == null || client.getUser() == null) {
                return;
            }

            User user = client.getUser();
            String fullName = ((user.getPrenom() == null ? "" : user.getPrenom()) + " "
                    + (user.getNom() == null ? "" : user.getNom())).trim();

            new MailService().sendConfirmation(
                    user.getEmail(),
                    fullName,
                    campagne == null ? "" : campagne.getTitre(),
                    rdv.getDateDon(),
                    selectedEntity == null ? "" : selectedEntity.getNom()
            );
        } catch (Exception e) {
            System.err.println("Could not send rendez-vous confirmation email: " + e.getMessage());
        }
    }

    private Client resolveClient(boolean preferSessionUser) throws SQLException {
        if (preferSessionUser) {
            Client sessionClient = resolveSessionClient();
            if (sessionClient != null) {
                return sessionClient;
            }
        }

        if (questionnaire != null && questionnaire.getClientId() > 0) {
            for (Client client : new ClientService().recuperer()) {
                if (client.getId() == questionnaire.getClientId()) {
                    return client;
                }
            }
        }

        if (!preferSessionUser) {
            return resolveSessionClient();
        }
        return null;
    }

    private Client resolveSessionClient() throws SQLException {
        User sessionUser = SessionManager.getCurrentUser();
        return sessionUser == null ? null : new ClientService().getByUserId(sessionUser.getId());
    }

    private void syncQuestionnaireClient(Client client) {
        if (questionnaire == null || client == null) {
            return;
        }

        questionnaire.setClientId(client.getId());
        questionnaire.setGroupeSanguin(client.getTypeSang());
        if (client.getUser() != null) {
            questionnaire.setNom(client.getUser().getNom());
            questionnaire.setPrenom(client.getUser().getPrenom());
        }
    }

    private ListCell<EntiteDeCollecte> createEntiteCell() {
        return new ListCell<>() {
            {
                getStyleClass().add("front-combo-cell");
            }

            @Override
            protected void updateItem(EntiteDeCollecte item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatEntite(item));
            }
        };
    }

    private String formatEntite(EntiteDeCollecte entite) {
        String nom = entite.getNom() == null ? "" : entite.getNom().trim();
        String ville = entite.getVille() == null ? "" : entite.getVille().trim();
        return ville.isEmpty() ? nom : nom + " - " + ville;
    }

    private void setRoot(Parent root, Button source) {
        Stage stage = (Stage) source.getScene().getWindow();
        tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
    }
}
