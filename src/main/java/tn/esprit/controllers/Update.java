package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
import java.util.List;

public class Update {

    private Questionnaire questionnaire;
    private RendezVous rendezVous;
    private Campagne campagne;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> sexeCombo;
    @FXML private TextField poidsField;
    @FXML private TextArea autresField;
    @FXML private DatePicker dateRdvPicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private ComboBox<EntiteDeCollecte> entiteCombo;
    @FXML private Button confirmerBtn;
    @FXML private Button annulerBtn;
    @FXML private Text ageError;
    @FXML private Text poidsError;
    @FXML private Text dateError;
    @FXML private Text timeError;

    private ObservableList<EntiteDeCollecte> entites = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // time spinners
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        // entite
        entiteCombo.setItems(entites);
        entiteCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(EntiteDeCollecte item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNom());
            }
        });
        entiteCombo.setButtonCell(entiteCombo.getCellFactory().call(null));
    }

    public void setCampagne(Campagne campagne) {
        this.campagne = campagne;
    }
    //to prefill les champs
    public void setData(Questionnaire q, RendezVous rdv) throws SQLException {
        this.questionnaire = q;
        this.rendezVous = rdv;

        // Prefill Questionnaire fields
        ageField.setText(String.valueOf(q.getAge()));
        sexeCombo.setValue(q.getSexe());
        poidsField.setText(String.valueOf(q.getPoids()));
        autresField.setText(q.getAutres());

        // Prefill RendezVous date/time
        dateRdvPicker.setValue(rdv.getDateDon().toLocalDate());
        hourSpinner.getValueFactory().setValue(rdv.getDateDon().getHour()-1);
        minuteSpinner.getValueFactory().setValue(rdv.getDateDon().getMinute());

        // Load all entities linked to this campaign
//        entiteCombo.setAccessibleText(new EntiteCollecteService().getEntiteById(rdv.getEntite_id()).getNom());
        EntiteCollecteService ecs = new EntiteCollecteService();
        List<EntiteDeCollecte> entityList = ecs.getByCampagneId(q.getCampagneId());
        entiteCombo.setItems(FXCollections.observableArrayList(entityList));
//        EntiteDeCollecte selectedEntity = ecs.getEntiteById(rdv.getEntite_id());
//        if (selectedEntity != null) {
//            entiteCombo.setValue(selectedEntity);
//        }
        // Pre-select the entity for this rendez-vous
        for (EntiteDeCollecte e : entityList) {
            if (e.getId() == rdv.getEntite_id()) {
                entiteCombo.setValue(e);
                break;
            }
        }
    }


    @FXML public void handleConfirmer() throws SQLException, IOException {
        boolean valid = true;

        // Reset error messages
        ageError.setVisible(false);
        poidsError.setVisible(false);
        dateError.setVisible(false);
        timeError.setVisible(false);

        // --- Validate Age ---
        int ageValue = 0;
        if (ageField.getText().isEmpty()) {
            ageError.setText("L'âge ne peut pas être vide");
            ageError.setVisible(true);
            valid = false;
        } else {
            ageValue = Integer.parseInt(ageField.getText());
            if (ageValue < 18 || ageValue > 70) {
                ageError.setText("L'âge doit être entre 18 et 70 ans");
                ageError.setVisible(true);
                valid = false;
            }
        }

        // --- Validate Poids ---
        double poidsValue = 0;
        if (poidsField.getText().isEmpty()) {
            poidsError.setText("Le poids ne peut pas être vide");
            poidsError.setVisible(true);
            valid = false;
        } else {
            poidsValue = Double.parseDouble(poidsField.getText());
            if (poidsValue < 50 || poidsValue > 100) {
                poidsError.setText("Le poids doit être entre 50 et 100 kg");
                poidsError.setVisible(true);
                valid = false;
            }
        }
        // --- Validate Date ---
        if (dateRdvPicker.getValue()==null) {
            dateError.setText("La date ne peut pas être vide");
            dateError.setVisible(true);
            valid = false;
        }else{
            LocalDate dateValue = dateRdvPicker.getValue();

            if (dateValue == null || dateValue.isBefore(campagne.getDateDebut()) || dateValue.isAfter(campagne.getDateFin())) {
            dateError.setText("La date doit être comprise entre " + campagne.getDateDebut() + " et " + campagne.getDateFin());
            dateError.setVisible(true);
            valid = false;}
        }
        // --- Validate time ---
        int hour = hourSpinner.getValue();
        if (hour < 8 || hour > 17) {
            timeError.setText("l'heure doit etre entre 8h et 17h");
            timeError.setVisible(true);
            valid = false;
        }

        if (!valid) {
            // Stop if validation failed
            return;
        }

        // --- Update Questionnaire & RendezVous ---
        questionnaire.setAge(ageValue);
        questionnaire.setSexe(sexeCombo.getValue());
        questionnaire.setPoids(poidsValue);
        questionnaire.setAutres(autresField.getText());
        rendezVous.setDateDon(dateRdvPicker.getValue().atTime(hourSpinner.getValue(), minuteSpinner.getValue()));
        rendezVous.setEntite_id(entiteCombo.getValue().getId());

        // Save to DB
        new QuestionnaireService().modifier(questionnaire);
        new RendezVousService().modifier(rendezVous);

        // Return to Liste
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Liste.fxml"));
        Parent root = loader.load();
        confirmerBtn.getScene().setRoot(root);
    }

    public void handleAnnuler() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Liste.fxml"));
        Parent root = loader.load();
        annulerBtn.getScene().setRoot(root);
    }
}