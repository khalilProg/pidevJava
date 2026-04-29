package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Home implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Home page initialization logic can go here
    }

    @FXML
    public void goToDonation(ActionEvent event) {
        loadPage(event, "/AjouterRendezVous.fxml");
    }

    @FXML
    public void goToCampagnes(ActionEvent event) {
        loadPage(event, "/ListeCampagnes.fxml");
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath);
            e.printStackTrace();
        }
    }
}
