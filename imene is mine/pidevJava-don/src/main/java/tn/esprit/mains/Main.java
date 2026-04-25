package tn.esprit.mains;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {

    // This variable determines which UI starts.
    // It is set by the specific Launcher you run.
    public static boolean IS_ADMIN_MODE = true;

    @Override
    public void start(Stage stage) {
        try {
            String fxmlFile = IS_ADMIN_MODE ? "/DossierMedList.fxml" : "/FrontMedicalProfile.fxml";
            String title = IS_ADMIN_MODE ? "BLOODLINK - Administrator Portal" : "BLOODLINK - Health Passport";

            System.out.println("🚀 System booting in " + (IS_ADMIN_MODE ? "ADMIN" : "CLIENT") + " mode...");

            URL url = getClass().getResource(fxmlFile);
            if (url == null) {
                System.err.println("❌ FXML Not Found: " + fxmlFile);
                return;
            }

            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root, 1400, 800);

            stage.setTitle(title);
            stage.setScene(scene);
            stage.setMinWidth(1200);
            stage.setMinHeight(750);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }
}