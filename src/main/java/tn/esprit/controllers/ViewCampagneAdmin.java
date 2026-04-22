package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.EntiteDeCollecte;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ViewCampagneAdmin {

    @FXML private Label idLabel;
    @FXML private Label titreLabel;
    @FXML private Label datesLabel;
    @FXML private Label descLabel;
    @FXML private FlowPane bloodTypesFlowPane;
    @FXML private FlowPane entitesFlowPane;

    private Campagne currentCampagne;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setCampagne(Campagne c) {
        this.currentCampagne = c;
        idLabel.setText("#" + c.getId());
        titreLabel.setText(c.getTitre());
        
        String debut = c.getDateDebut() != null ? FORMATTER.format(c.getDateDebut()) : "...";
        String fin = c.getDateFin() != null ? FORMATTER.format(c.getDateFin()) : "...";
        datesLabel.setText("📅 " + debut + " — " + fin);
        
        descLabel.setText(c.getDescription());

        // Blood Types
        bloodTypesFlowPane.getChildren().clear();
        if (c.getTypeSang() != null && !c.getTypeSang().isEmpty()) {
            String[] types = c.getTypeSang().split(",\\s*");
            for (String t : types) {
                if (!t.isEmpty()) {
                    Label badge = new Label(t);
                    badge.getStyleClass().add("badge-groupe");
                    bloodTypesFlowPane.getChildren().add(badge);
                }
            }
        }

        // Entites
        entitesFlowPane.getChildren().clear();
        if (c.getEntiteDeCollectes() != null) {
            for (EntiteDeCollecte e : c.getEntiteDeCollectes()) {
                Label badge = new Label(e.getNom());
                badge.getStyleClass().add("badge-campagne"); // styled Pills
                entitesFlowPane.getChildren().add(badge);
            }
        }
    }

    @FXML
    void handleRetour(ActionEvent event) {
        navigateTo(event, "/ListeCampagnesAdmin.fxml");
    }

    @FXML
    void handleNavigateModifier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCampagneAdmin.fxml"));
            Parent root = loader.load();
            ModifierCampagneAdmin controller = loader.getController();
            controller.setCampagneToEdit(currentCampagne);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Navigation Handlers ──

    @FXML void handleLogout(ActionEvent event) { navigateTo(event, "/login.fxml"); }
    @FXML void handleNavigateDashboard(ActionEvent event) { navigateTo(event, "/admin_dashboard.fxml"); }
    @FXML void handleNavigateUsers(ActionEvent event) { navigateTo(event, "/admin_users.fxml"); }
    @FXML void handleNavigateDemandes(ActionEvent event) { navigateTo(event, "/DemandeBackView.fxml"); }
    @FXML void handleNavigateTransferts(ActionEvent event) { navigateTo(event, "/TransfertBackView.fxml"); }
    @FXML void handleNavigateQuestionnaires(ActionEvent event) { navigateTo(event, "/ListeQuestAdmin.fxml"); }
    @FXML void handleNavigateRendezVous(ActionEvent event) { navigateTo(event, "/ListeRdvAdmin.fxml"); }
    @FXML void handleNavigateCampagnes(ActionEvent event) { navigateTo(event, "/ListeCampagnesAdmin.fxml"); }
    @FXML void handleNavigateCollectes(ActionEvent event) { navigateTo(event, "/ListeEntitesAdmin.fxml"); }

    private void navigateTo(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
