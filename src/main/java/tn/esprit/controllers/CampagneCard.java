package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.Client;
import tn.esprit.services.RendezVousService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class CampagneCard {
    @FXML private Text campaignName;
    @FXML private Text campaignDates;
    @FXML private Text bloodTypes;
    @FXML private Text campaignDescription;
    @FXML private Button participateButton;
    private Campagne campagne;
    private Client currentClient = new Client(2, "A-", LocalDate.of(2003, 10, 17));
    public void setData(Campagne c) {
        this.campagne = c;
        campaignName.setText(c.getTitre());
        campaignDates.setText(c.getDateDebut() + " - " + c.getDateFin());
//        bloodTypes.setText(c.getTypeSang());
        bloodTypes.setText(c.getTypeSang().replace("[", "").replace("]", "").replace("\"", ""));
        campaignDescription.setText(c.getDescription());

    }
    @FXML public void openQuestionnaire() {
        try {

            int clientId = currentClient.getId();
            int campagneId = campagne.getId();

            RendezVousService rdvService = new RendezVousService();

            if (rdvService.hasRendezVous(clientId, campagneId)) {
                // Show message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info");
                alert.setHeaderText(null);
                alert.setContentText("Vous avez déjà pris un rendez-vous pour cette campagne.");
                alert.showAndWait();
                return; // stop opening questionnaire
            }

            // Proceed to open the questionnaire if no existing rendez-vous
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterQuestionnaire.fxml"));
            Parent root = loader.load();
            AjouterQuestionnaire aq = loader.getController();
            aq.setCampagne(campagne);
            participateButton.getScene().setRoot(root);

        } catch (IOException | SQLException ex) {
        ex.printStackTrace();
    }
    }

}
