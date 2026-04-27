package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.text.Text;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.Client;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;
import tn.esprit.entities.User;
import tn.esprit.services.ClientService;
import tn.esprit.services.EmailService;
import tn.esprit.services.QuestionnaireService;
import tn.esprit.services.RendezVousService;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AjouterRendezVous {
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

    public void setCampagne(Campagne campagne) {
        this.campagne = campagne;
    }

    @FXML
    public void initialize() {
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        entiteCombo.setItems(entites);
        entiteCombo.setCellFactory(c -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(EntiteDeCollecte item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNom());
            }
        });
        entiteCombo.setButtonCell(entiteCombo.getCellFactory().call(null));
    }

    public void setEntities(List<EntiteDeCollecte> entitiesList) {
        entites.setAll(entitiesList);
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
        sendConfirmationEmail(rdv, selectedEntity, true);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Liste.fxml"));
        Parent root = loader.load();
        confirmerRdv.getScene().setRoot(root);
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
        confirmerRdv.getScene().setRoot(root);
    }

    @FXML
    public void handleAnnuler(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeCampagnes.fxml"));
        Parent root = loader.load();
        annulerRdv.getScene().setRoot(root);
    }

    @FXML
    public void btnAnnuler(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeRdvAdmin.fxml"));
        Parent root = loader.load();
        annulerRdv.getScene().setRoot(root);
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

            new EmailService().sendRendezVousConfirmation(
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
}
