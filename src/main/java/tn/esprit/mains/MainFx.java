package tn.esprit.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("BloodLink - Connexion");
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error starting BloodLink: " + e.getMessage());
            Logger.getLogger(MainFx.class.getName()).log(Level.SEVERE, "Failed to load Main FXML", e);
        }
    }
}
