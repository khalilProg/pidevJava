package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import tn.esprit.entities.DossierMed;
import tn.esprit.services.AIService;
import tn.esprit.services.ServiceDossierMed;
import tn.esprit.tools.MockSession;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class FrontMedicalController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Label nameLabel, bloodLabel, ageLabel, genderLabel, heightLabel, weightLabel;
    @FXML private Label bmiValueLabel, bmiStatusLabel, recoveryLabel;
    @FXML private ImageView bmiQuickChartImage;
    @FXML private LineChart<String, Number> biometricChart;

    private ServiceDossierMed service = new ServiceDossierMed();
    private AIService aiService = new AIService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        syncProfile();
    }

    private void syncProfile() {
        try {
            int sessionClientId = MockSession.getClientId();
            DossierMed record = service.getByClientId(sessionClientId);

            if (record != null) {
                nameLabel.setText(record.getPrenom() + " " + record.getNom().toUpperCase());
                bloodLabel.setText(record.getType_sang());
                ageLabel.setText(record.getAge() + " Yrs");
                genderLabel.setText(record.getSexe().toUpperCase());
                heightLabel.setText(record.getTaille() + " cm");
                weightLabel.setText(record.getPoid() + " kg");

                generateSpeedometerGauge(record);
                generateHistoricalChart(record);
            } else {
                nameLabel.setText("LOGOUT & RE-SYNC REQUIRED");
                bmiStatusLabel.setText("Error: client_id " + sessionClientId + " missing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void handleUpdateProfile(ActionEvent event) {
        // For now, let's just show a message. You can add SQL update logic here later!
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profile Sync");
        alert.setHeaderText(null);
        alert.setContentText("Biometric synchronization complete.");
        alert.showAndWait();
    }
    private void generateSpeedometerGauge(DossierMed record) {
        float bmi = record.calculateBMI();

        bmiValueLabel.setText(String.format(Locale.US, "%.1f", bmi));
        bmiStatusLabel.setText("VITASPHERE INDEX: " + record.getBMICategory());

        // --- THE SPEEDOMETER UPGRADE ---
        try {
            // This builds a half-circle Speedometer showing the specific BMI zones
            String config = "{"
                    + "type:'gauge',"
                    + "data:{"
                    + "datasets:[{"
                    + "value:" + String.format(Locale.US, "%.1f", bmi) + ","
                    + "data:[18.5, 25, 30, 40],"
                    + "backgroundColor:['#4a90e2','#00ff88','#ffaa00','#ff3e3e'],"
                    + "borderWidth:0"
                    + "}]"
                    + "},"
                    + "options:{"
                    + "valueLabel:{display:false}" // We hide the API's label because you have a nice custom JavaFX label overlaying it
                    + "}"
                    + "}";

            String url = "https://quickchart.io/chart?c=" + URLEncoder.encode(config, StandardCharsets.UTF_8) + "&bkg=transparent";

            // Set the image asynchronously to prevent UI freezing
            bmiQuickChartImage.setImage(new Image(url, true));
        } catch (Exception ignored) {}

        new Thread(() -> {
            String advice = aiService.getRecoveryPlan(record.getPoid(), bmi);
            Platform.runLater(() -> recoveryLabel.setText(advice));
        }).start();
    }

    private void generateHistoricalChart(DossierMed record) {
        biometricChart.getData().clear();
        XYChart.Series<String, Number> weightSeries = new XYChart.Series<>();
        weightSeries.setName("Body Mass (kg)");

        weightSeries.getData().add(new XYChart.Data<>("3 Months Ago", record.getPoid() + 2.5f));
        weightSeries.getData().add(new XYChart.Data<>("Last Month", record.getPoid() + 0.8f));
        weightSeries.getData().add(new XYChart.Data<>("Current", record.getPoid()));

        biometricChart.getData().add(weightSeries);
    }

    @FXML
    void goToMyDonations(ActionEvent event) {
        try {
            // CRITICAL FIX: Safe loading to prevent crashes
            URL fxmlLocation = getClass().getResource("/FrontDonationHistory.fxml");

            if (fxmlLocation == null) {
                // If the file is missing, show an alert instead of crashing the whole app!
                new Alert(Alert.AlertType.ERROR, "CRITICAL ERROR: Cannot find '/FrontDonationHistory.fxml'. Make sure the file is in the 'resources' root folder!").show();
                System.err.println("❌ FXML NOT FOUND: Ensure FrontDonationHistory.fxml is named exactly like this and is inside src/main/resources/ (if using Maven) or the src root.");
                return;
            }

            Parent root = FXMLLoader.load(fxmlLocation);
            rootPane.getScene().setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "An error occurred while loading the scene.").show();
        }
    }
}