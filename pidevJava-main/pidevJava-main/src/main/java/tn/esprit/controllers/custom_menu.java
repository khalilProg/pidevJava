package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class custom_menu {

    @FXML
    private AnchorPane menuContainer;

    @FXML
    void handleCloseMenu(ActionEvent event) {
        // Fallback: routing to login scene mimics closing returning to last page.
        // In a true router you'd track the origin scene. For now, route back to login.
        navigateTo(event, "/login.fxml");
    }

    @FXML
    void handleGoToLogin(ActionEvent event) {
        navigateTo(event, "/login.fxml");
    }

    @FXML
    void handleGoToRegister(ActionEvent event) {
        navigateTo(event, "/register.fxml");
    }

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
