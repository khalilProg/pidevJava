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
import tn.esprit.entities.User;

import java.io.IOException;

public class ClientHomeController {

    @FXML
    private Button userNameBtn;

    @FXML
    private Button menuToggleBtn;

    @FXML
    private HBox menuOverlay;

    @FXML
    private Label sessionEmailLabel;

    private User currentUser;

    /**
     * Initialize the home page with the logged-in user's data.
     */
    public void initData(User user) {
        this.currentUser = user;
        if (user != null) {
            userNameBtn.setText("👤 " + user.getPrenom() + " " + user.getNom());
            sessionEmailLabel.setText("Session: " + user.getEmail());
        }
    }

    @FXML
    void handleMenuToggle(ActionEvent event) {
        boolean isVisible = menuOverlay.isVisible();
        menuOverlay.setVisible(!isVisible);
        menuOverlay.setManaged(!isVisible);
        menuToggleBtn.setText(isVisible ? "MENU ☰" : "FERMER ✕");
    }

    @FXML
    void handleMenuClose(javafx.scene.input.MouseEvent event) {
        menuOverlay.setVisible(false);
        menuOverlay.setManaged(false);
        menuToggleBtn.setText("MENU ☰");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BloodLink - Connexion");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
