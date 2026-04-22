package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Home implements Initializable {
    @FXML private VBox supportPanel;
    @FXML private Button supportBtn;
    @FXML private TextArea chatArea;
    @FXML private TextField inputField;
    @Override public void initialize(URL url, ResourceBundle resourceBundle) {
        // Home page initialization logic can go here
    }
    @FXML
    public void toggleSupportPanel() {
        boolean isVisible = supportPanel.isVisible();
        supportPanel.setVisible(!isVisible);
        supportPanel.setManaged(!isVisible);

        if (!isVisible && chatArea.getText().isEmpty()) {
            chatArea.appendText("Welcome to BloodLink Support.\n");
            chatArea.appendText("How can we help you today?\n\n");
        }
    }
    @FXML
    public void sendSupportMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            chatArea.appendText("You: " + message + "\n");
            inputField.clear();
            chatArea.setScrollTop(Double.MAX_VALUE);
        }
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
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath);
            e.printStackTrace();
        }
    }
}
