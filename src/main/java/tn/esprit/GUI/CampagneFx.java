package tn.esprit.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CampagneFx extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/GUI/CampagneBack.fxml"));
        //Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/GUI/CampagneFront.fxml"));

         //Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/GUI/EntiteCollecteFront.fxml"));
         //Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/GUI/EntiteCollecteBack.fxml"));

        primaryStage.setTitle("Test Interface");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
