package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminAfficherCommandes.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("BLOODLINK — BackOffice");
        primaryStage.setScene(scene);
        primaryStage.show();

        Stage secondaryStage = new Stage();
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/AfficherCommandes.fxml"));
        Parent root2 = loader2.load();
        Scene scene2 = new Scene(root2, 1000, 700);
        secondaryStage.setTitle("BLOODLINK — FrontOffice");
        secondaryStage.setScene(scene2);
        secondaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
