package tn.esprit.controllers;

import javafx.fxml.FXML;
import tn.esprit.entities.User;

public class CntsAgentHomeController extends CampagneFrontController {

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        if (user != null) {
            if (userNameBtn != null) {
                userNameBtn.setText("User " + user.getPrenom() + " " + user.getNom());
            }
            if (sessionEmailLabel != null) {
                sessionEmailLabel.setText("Session: " + user.getEmail());
            }
        }
    }

    @FXML
    @Override
    public void goToAccueil(javafx.event.Event event) {
        switchScene(event, "/cnts_agent_home.fxml");
    }

    @FXML
    @Override
    public void goToCampagnes(javafx.event.Event event) {
        switchScene(event, "/cnts_agent_home.fxml");
    }
}
