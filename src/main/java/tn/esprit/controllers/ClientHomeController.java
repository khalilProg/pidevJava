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

public class ClientHomeController extends BaseFront {

    private User currentUser;

    /**
     * Initialize the home page with the logged-in user's data.
     */
    public void initData(User user) {
        this.currentUser = user;
        if (user != null) {
            if (userNameBtn != null) userNameBtn.setText("👤 " + user.getPrenom() + " " + user.getNom());
            if (sessionEmailLabel != null) sessionEmailLabel.setText("Session: " + user.getEmail());
        }
    }
}
