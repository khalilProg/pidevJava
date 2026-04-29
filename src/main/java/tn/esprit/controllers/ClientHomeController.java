package tn.esprit.controllers;

import tn.esprit.entities.User;
import tn.esprit.tools.SessionManager;

public class ClientHomeController extends BaseFront {

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        SessionManager.setCurrentUser(user);
        applySessionUser();
    }
}
