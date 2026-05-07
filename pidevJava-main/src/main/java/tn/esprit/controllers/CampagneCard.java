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
import javafx.event.ActionEvent;
import javafx.scene.Node;
import tn.esprit.services.CampagneService;
import tn.esprit.tools.AnimationUtils;

public class CampagneCard {
    @FXML private Label campaignName;
    @FXML private Label campaignDates;
    @FXML private Label bloodTypes;
    @FXML private Label campaignDescription;
    @FXML private Button participateButton;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    private Campagne campagne;
    private final ClientService clientService = new ClientService();

    public void setData(Campagne c) {
        this.campagne = c;
        campaignName.setText(c.getTitre());
        campaignDates.setText("Du " + c.getDateDebut() + " au " + c.getDateFin());
        bloodTypes.setText(c.getTypeSang().replace("[", "").replace("]", "").replace("\"", ""));
        campaignDescription.setText(c.getDescription());
        if (btnModifier != null) AnimationUtils.applyHoverAnimation(btnModifier);
        if (btnSupprimer != null) AnimationUtils.applyHoverAnimation(btnSupprimer);
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


    @FXML public void handleModifier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCampagneFront.fxml"));
            Parent root = loader.load();
            tn.esprit.controllers.ModifierCampagneFrontController controller = loader.getController();
            controller.setCampagneToEdit(campagne);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public void handleSupprimer(ActionEvent event) {
        // Confirmation avant suppression
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer cette campagne ?");
        confirm.setContentText("Cette action est irréversible. Voulez-vous vraiment supprimer \"" + campagne.getTitre() + "\" ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                CampagneService service = new CampagneService();
                try {
                    service.supprimer(campagne);
                    // Rafraîchir la liste
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/cnts_agent_home.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
                } catch (RuntimeException ex) {
                    Alert erreur = new Alert(Alert.AlertType.ERROR);
                    erreur.setTitle("Suppression impossible");
                    erreur.setHeaderText("Impossible de supprimer cette campagne");
                    erreur.setContentText("Cette campagne est liée à des données existantes (rendez-vous, questionnaires, dons...). Veuillez d'abord supprimer ces données associées.");
                    erreur.showAndWait();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
