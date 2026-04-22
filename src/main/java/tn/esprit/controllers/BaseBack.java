package tn.esprit.controllers;
 
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
 
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
 
public class BaseBack implements Initializable {
 
    @FXML
    private Label dateLabel;
 
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set current date dynamically matching the screenshot's style (e.g., 15 Apr 2026)
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
        dateLabel.setText(now.format(formatter));
    }
 
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
    public void goToDashboard(ActionEvent event) {
        // Already on Dashboard, but can re-load if needed
        switchScene(event, "/HomeAdmin.fxml");
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
