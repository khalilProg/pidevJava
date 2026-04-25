package tn.esprit.mains;

import javafx.application.Application;

public class AdminLauncher {
    public static void main(String[] args) {
        // Set the mode to Admin before launching
        Main.IS_ADMIN_MODE = true;
        Application.launch(Main.class, args);
    }
}