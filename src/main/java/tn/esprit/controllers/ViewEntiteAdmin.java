package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.entities.EntiteDeCollecte;

import java.io.IOException;

public class ViewEntiteAdmin {

    @FXML private Label idLabel;
    @FXML private Label nomLabel;
    @FXML private Label adresseLabel;
    @FXML private Label villeLabel;
    @FXML private Label telephoneLabel;

    private EntiteDeCollecte currentEntite;

    public void setEntite(EntiteDeCollecte e) {
        this.currentEntite = e;
        idLabel.setText("#" + e.getId());
        nomLabel.setText(e.getNom());
        adresseLabel.setText(e.getAdresse());
        villeLabel.setText("📍 " + e.getVille());
        telephoneLabel.setText(e.getTel());
    }

    @FXML
    void handleRetour(ActionEvent event) {
        navigateTo(event, "/ListeEntitesAdmin.fxml");
    }

    @FXML
    void handleNavigateModifier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEntiteAdmin.fxml"));
            Parent root = loader.load();
            ModifierEntiteAdmin controller = loader.getController();
            controller.setEntiteToEdit(currentEntite);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
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
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
