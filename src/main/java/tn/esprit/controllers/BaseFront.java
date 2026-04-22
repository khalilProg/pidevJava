package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;

public class BaseFront {

    @FXML protected HBox menuOverlay;
    @FXML protected Button menuToggleBtn;
    @FXML protected Label sessionEmailLabel;
    @FXML protected Button userNameBtn;

    protected void switchScene(javafx.event.Event event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    void handleMenuToggle(ActionEvent event) {
        if (menuOverlay != null) {
            boolean isVisible = menuOverlay.isVisible();
            menuOverlay.setVisible(!isVisible);
            menuOverlay.setManaged(!isVisible);
            if (menuToggleBtn != null) {
                menuToggleBtn.setText(isVisible ? "MENU ☰" : "FERMER ✕");
            }
        }
    }

    @FXML
    void handleMenuClose(javafx.scene.input.MouseEvent event) {
        if (menuOverlay != null) {
            menuOverlay.setVisible(false);
            menuOverlay.setManaged(false);
            if (menuToggleBtn != null) {
                menuToggleBtn.setText("MENU ☰");
            }
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        switchScene(event, "/login.fxml");
    }

    @FXML
    public void goToAccueil(javafx.event.Event event) {
        switchScene(event, "/client_home.fxml");
    }

    @FXML
    public void goToCampagnes(javafx.event.Event event) {
        switchScene(event, "/CampagneFront.fxml");
    }

    @FXML
    public void goToCalendar(javafx.event.Event event) {
        switchScene(event, "/CampagneCalendar.fxml");
    }

    @FXML
    public void goToHistorique(javafx.event.Event event) {
        switchScene(event, "/Liste.fxml");
    }
}
