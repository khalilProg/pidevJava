package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeAdmin.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Liste des Campagnes");
        primaryStage.setScene(scene);
        primaryStage.show();
        // Second Stage (new window)
        Stage secondaryStage = new Stage();
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/Home.fxml"));
        Parent root2 = loader2.load();
        Scene scene2 = new Scene(root2);
        secondaryStage.setTitle("Second Scene");
        secondaryStage.setScene(scene2);
        secondaryStage.show();
    }
}

