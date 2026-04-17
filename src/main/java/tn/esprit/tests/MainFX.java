package tn.esprit.tests;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    private static Stage primaryStage;
    private static Stage secondaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        loadScene(primaryStage, "/AdminAfficherCommandes.fxml", "BLOODLINK — BackOffice");

        secondaryStage = new Stage();
        loadScene(secondaryStage, "/AfficherCommandes.fxml", "BLOODLINK — FrontOffice");
    }

    public static void loadScene(Stage stage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(MainFX.class.getResource(fxmlPath));
            Parent root = loader.load();

            boolean wasMaximized = stage.isMaximized();

            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);

            if (!stage.isShowing()) {
                stage.show();
            }

            Platform.runLater(() -> {
                if (wasMaximized || !stage.equals(secondaryStage)) {
                    stage.setMaximized(true);
                }
                stage.centerOnScreen();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Stage getSecondaryStage() {
        return secondaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}