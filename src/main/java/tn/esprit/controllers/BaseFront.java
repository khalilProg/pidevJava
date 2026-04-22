package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BaseFront {

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    public void goToAccueil(ActionEvent event) {
        switchScene(event, "/Home.fxml");
    }

    @FXML
    public void goToHistorique(ActionEvent event) {
        switchScene(event, "/Liste.fxml");
    }

    @FXML
    public void goToCampagnes(ActionEvent event) {
        switchScene(event, "/ListeCampagnes.fxml");
    }

    @FXML
    public void goToQuestionnaires(ActionEvent event) {
        switchScene(event, "/ListeQuestAdmin.fxml");
    }

    @FXML
    public void goToRendezVous(ActionEvent event) {
        switchScene(event, "/ListeRdvAdmin.fxml");
    }
}
