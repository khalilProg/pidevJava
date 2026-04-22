package tn.esprit.tests;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class TestFxml extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Testing AdminAfficherCommandes.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminAfficherCommandes.fxml"));
            Parent root = loader.load();
            System.out.println("SUCCESS Commandes");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Testing AfficherStocks.fxml...");
            FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/AfficherStocks.fxml"));
            Parent root2 = loader2.load();
            System.out.println("SUCCESS Stocks");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
    public static void main(String[] args) {
        launch(args);
    }
}
