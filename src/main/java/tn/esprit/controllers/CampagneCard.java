package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.Client;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.User;
import tn.esprit.services.ClientService;
import tn.esprit.services.QuestionnaireService;
import tn.esprit.services.RendezVousService;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class CampagneCard {
    @FXML private Label campaignName;
    @FXML private Label campaignDates;
    @FXML private Label bloodTypes;
    @FXML private Label campaignDescription;
    @FXML private Button participateButton;
    private Campagne campagne;
    private final ClientService clientService = new ClientService();

    public void setData(Campagne c) {
        this.campagne = c;
        campaignName.setText(c.getTitre());
        campaignDates.setText("Du " + c.getDateDebut() + " au " + c.getDateFin());
//        bloodTypes.setText(c.getTypeSang());
        bloodTypes.setText(c.getTypeSang().replace("[", "").replace("]", "").replace("\"", ""));
        campaignDescription.setText(c.getDescription());

    }
    @FXML public void openQuestionnaire() throws SQLException {
        try {

            Client currentClient = resolveCurrentClient();
            if (currentClient == null) {
                showMissingClientProfileAlert();
                return;
            }

            int clientId = currentClient.getId();
            int campagneId = campagne.getId();

            RendezVousService rdvService = new RendezVousService();

            if (rdvService.hasRendezVous(clientId, campagneId) && rdvService.recuperer().stream()
                    .anyMatch(r -> {
                                try {
                                    Questionnaire q = new QuestionnaireService().getQuestionnaireById(r.getQuestionnaire_id());
                                    return q.getClientId() == clientId &&
                                            q.getCampagneId() == campagneId &&
                                            !r.getStatus().equalsIgnoreCase("annulé");
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                    }
            )) {
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
            Stage stage = (Stage) participateButton.getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);

        } catch (IOException | SQLException ex) {
        ex.printStackTrace();
    }
    }

    private Client resolveCurrentClient() throws SQLException {
        User sessionUser = SessionManager.getCurrentUser();
        return sessionUser == null ? null : clientService.getByUserId(sessionUser.getId());
    }

    private void showMissingClientProfileAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Profil client requis");
        alert.setHeaderText(null);
        alert.setContentText("Connectez-vous avec un compte client ayant un profil complet avant de prendre un rendez-vous.");
        alert.showAndWait();
    }
}
