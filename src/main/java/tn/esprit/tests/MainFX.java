package tn.esprit.tests;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminAfficherCommandes.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("BLOODLINK — BackOffice");
        primaryStage.setScene(scene);
        Platform.runLater(() -> primaryStage.setMaximized(true));
        primaryStage.show();;

        Stage secondaryStage = new Stage();
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/AfficherCommandes.fxml"));
        Parent root2 = loader2.load();
        Scene scene2 = new Scene(root2);
        secondaryStage.setTitle("BLOODLINK — FrontOffice");
        secondaryStage.setScene(scene2);
        Platform.runLater(() -> secondaryStage.setMaximized(true));
        secondaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
