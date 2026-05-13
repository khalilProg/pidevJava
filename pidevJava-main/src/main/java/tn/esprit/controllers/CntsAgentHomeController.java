package tn.esprit.controllers;

import javafx.fxml.FXML;
import tn.esprit.entities.User;
import tn.esprit.tools.SessionManager;

public class CntsAgentHomeController extends CampagneFrontController {

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        SessionManager.setCurrentUser(user);
        applySessionUser();
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
