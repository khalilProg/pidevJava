package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.List;

public class UpdateRdvAdmin {
    @FXML private DatePicker dateRdvPicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private ComboBox<EntiteDeCollecte> entiteCombo;
    @FXML private Button confirmerBtn;
    @FXML private Button annulerBtn;
    @FXML private Text dateError;
    @FXML private Text timeError;
    private Campagne campagne;
    private RendezVous rendezVous;
    private Questionnaire questionnaire;
    private ObservableList<EntiteDeCollecte> entites = FXCollections.observableArrayList();
    public void setCampagne(Campagne campagne) {
        this.campagne = campagne;
    }

    public void setData(RendezVous rdv, Questionnaire q) throws SQLException {
        this.rendezVous = rdv;
        this.questionnaire = q;

        // Prefill date & time if available
        if (rdv.getDateDon() != null) {
            dateRdvPicker.setValue(rdv.getDateDon().toLocalDate());
            hourSpinner.getValueFactory().setValue(rdv.getDateDon().getHour());
            minuteSpinner.getValueFactory().setValue(rdv.getDateDon().getMinute());
        }

        // Load entities linked to the campagne
        List<EntiteDeCollecte> entityList = new EntiteCollecteService()
                .getByCampagneId(q.getCampagneId());
        entiteCombo.setItems(FXCollections.observableArrayList(entityList));

        // Pre-select entity for this rendez-vous
        for (EntiteDeCollecte e : entityList) {
            if (e.getId() == rdv.getEntite_id()) {
                entiteCombo.setValue(e);
                break;
            }
        }
    }
    @FXML public void initialize() {
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

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

    @FXML
    public void handleConfirmer() throws SQLException, IOException {
        boolean valid = true;

        dateError.setVisible(false);
        timeError.setVisible(false);

        LocalDate dateValue = dateRdvPicker.getValue();
        int hour = hourSpinner.getValue();

        if (dateValue == null || dateValue.isBefore(campagne.getDateDebut()) || dateValue.isAfter(campagne.getDateFin())) {
            dateError.setText("La date doit être comprise entre " + campagne.getDateDebut() + " et " + campagne.getDateFin());
            dateError.setVisible(true);
            valid = false;
        }

        if (hour < 8 || hour > 17) {
            timeError.setText("l'heure doit être entre 8h et 17h");
            timeError.setVisible(true);
            valid = false;
        }

        if (!valid) return;

        // Update rdv
        rendezVous.setDateDon(dateValue.atTime(hour, minuteSpinner.getValue()));
        rendezVous.setEntite_id(entiteCombo.getValue().getId());

        new RendezVousService().modifier(rendezVous);

        // Go back to list
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeRdvAdmin.fxml"));
        Parent root = loader.load();
        confirmerBtn.getScene().setRoot(root);
    }

    public void handleAnnuler() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeRdvAdmin.fxml"));
        Parent root = loader.load();
        annulerBtn.getScene().setRoot(root);
    }

}
