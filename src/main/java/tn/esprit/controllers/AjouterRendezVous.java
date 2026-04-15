package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;
import tn.esprit.services.EntiteCollecteService;
import tn.esprit.services.QuestionnaireService;
import tn.esprit.services.RendezVousService;

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
    private ObservableList<EntiteDeCollecte> entites = FXCollections.observableArrayList();

    public void setCampagne(Campagne campagne) {
        this.campagne=campagne;
    }

    @FXML public void initialize() {
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        entiteCombo.setItems(entites);

        entiteCombo.setCellFactory(c -> new javafx.scene.control.ListCell<EntiteDeCollecte>() {
            @Override
            protected void updateItem(EntiteDeCollecte item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNom());
            }
        });
        entiteCombo.setButtonCell(entiteCombo.getCellFactory().call(null));
    }

    // Method to inject the list from previous page
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
        // reset lel errors
        entiteError.setVisible(false);
        dateError.setVisible(false);
        timeError.setVisible(false);
        EntiteDeCollecte selectedEntity = entiteCombo.getValue();

        if (selectedEntity == null) {
            entiteError.setText("Veuillez choisir une entité de collecte");
            entiteError.setVisible(true);
            valid = false;
        }

        if (date == null || date.isBefore(campagne.getDateDebut()) || date.isAfter(campagne.getDateFin())) {
            dateError.setText("La date doit être comprise entre " + campagne.getDateDebut() + " et " + campagne.getDateFin());
            dateError.setVisible(true);
            valid = false;
        }
        if (hour < 8 || hour > 17) {
            timeError.setText("l'heure doit etre entre 8h et 17h");
            timeError.setVisible(true);
            valid = false;
        }
        if (!valid) return; // stop itha validation fails
        LocalDateTime rdvDateTime = date.atTime(hour, minute);
        int selectedId = selectedEntity.getId();

        new QuestionnaireService().ajouter(questionnaire);
        int questionnaireId = questionnaire.getId();
        RendezVous rdv = new RendezVous("confirmé", rdvDateTime, questionnaireId, selectedId);
        new RendezVousService().ajouter(rdv);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Liste.fxml"));
        Parent root = loader.load();
        confirmerRdv.getScene().setRoot(root);

    }

    @FXML public void handleAnnuler(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeCampagnes.fxml"));
        Parent root = loader.load();
        annulerRdv.getScene().setRoot(root);
    }
    @FXML public void btnAnnuler(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeRdvAdmin.fxml"));
        Parent root = loader.load();
        annulerRdv.getScene().setRoot(root);
    }


}
