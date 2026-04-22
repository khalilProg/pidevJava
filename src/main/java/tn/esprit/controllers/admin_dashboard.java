package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import tn.esprit.tools.ThemeManager;

import java.io.IOException;

public class admin_dashboard {

    private final ThemeManager themeManager = ThemeManager.getInstance();

    @FXML
    void handleNavigateUsers(ActionEvent event) {
        navigateTo(event, "/admin_users.fxml");
    }

    @FXML
    void handleNavigateCommandes(ActionEvent event) {
        navigateTo(event, "/AdminAfficherCommandes.fxml");
    }

    @FXML
    void handleNavigateStocks(ActionEvent event) {
        navigateTo(event, "/AfficherStocks.fxml");
    }

    // Mouse click variants for VBox quick actions
    @FXML void handleNavigateRendezVousClick(MouseEvent event) {
        navigateToMouse(event, "/ListeRdvAdmin.fxml");
    }
    @FXML void handleNavigateQuestionnairesClick(MouseEvent event) {
        navigateToMouse(event, "/ListeQuestAdmin.fxml");
    }
    @FXML void handleNavigateCollectesClick(MouseEvent event) {
        navigateToMouse(event, "/ListeEntitesAdmin.fxml");
    }
    @FXML void handleNavigateCampagnesClick(MouseEvent event) {
        navigateToMouse(event, "/ListeCampagnesAdmin.fxml");
    }

    private void navigateTo(ActionEvent event, String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            themeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToMouse(MouseEvent event, String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            themeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
