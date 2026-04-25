package tn.esprit.mains;

import javafx.application.Application;

public class ClientLauncher {
    public static void main(String[] args) {
        // Set the mode to Client before launching
        Main.IS_ADMIN_MODE = false;
        Application.launch(Main.class, args);
    }
}