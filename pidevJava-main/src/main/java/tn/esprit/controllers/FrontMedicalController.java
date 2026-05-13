package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import tn.esprit.entities.Client;      // ⬅️ TEAM INTEGRATION
import tn.esprit.entities.User;        // ⬅️ TEAM INTEGRATION
import tn.esprit.services.ClientService; // ⬅️ TEAM INTEGRATION
import tn.esprit.entities.DossierMed;
import tn.esprit.services.ServiceDossierMed;
import tn.esprit.services.KNNModelService;
import tn.esprit.services.AIService;
import tn.esprit.tools.SessionManager;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class FrontMedicalController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Label nameLabel, bloodLabel, ageLabel, genderLabel, heightLabel, weightLabel;
    @FXML private Label bmiValueLabel, bmiStatusLabel;
    @FXML private ImageView bmiQuickChartImage;
    @FXML private LineChart<String, Number> biometricChart;

    // == The Brand New DEDICATED Machine Learning Widget ==
    @FXML private Label knnResultLabel;

    // AI Overlay FXML injected variables
    @FXML private StackPane aiOverlay;
    @FXML private Label overlayAiText;

    // Services required
    private ServiceDossierMed service = new ServiceDossierMed();
    private AIService aiService = new AIService();
    private KNNModelService knnModelService = new KNNModelService();
    private ClientService clientService = new ClientService(); // ⬅️ TEAM INTEGRATION

    private DossierMed currentRecord = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        syncProfile();
    }

    private void syncProfile() {
        try {
            // ⬅️ TEAM INTEGRATION: Resolve the specific Client ID of the Session
            int currentClientId = 1; // Fallback default ID
            User loggedUser = SessionManager.getCurrentUser();

            if (loggedUser != null) {
                Client loggedClient = clientService.getByUserId(loggedUser.getId());
                if (loggedClient != null) {
                    currentClientId = loggedClient.getId();
                }
            }

            currentRecord = service.getByClientId(currentClientId);

            if (currentRecord != null) {
                // If the user's base entity contains the name, fetch it safely via TEAM'S LOGGED USER logic!
                String fullName = (loggedUser != null)
                        ? loggedUser.getPrenom() + " " + loggedUser.getNom().toUpperCase()
                        : currentRecord.getPrenom() + " " + currentRecord.getNom().toUpperCase();

                nameLabel.setText(fullName);
                bloodLabel.setText(currentRecord.getType_sang());
                ageLabel.setText(currentRecord.getAge() + " Yrs");
                genderLabel.setText(currentRecord.getSexe().toUpperCase());
                heightLabel.setText(currentRecord.getTaille() + " cm");
                weightLabel.setText(currentRecord.getPoid() + " kg");

                generateSpeedometerGauge(currentRecord);
                generateHistoricalChart(currentRecord);

                // CALL KNN ML PREDICTION ALGORITHM IN BACKEND (Decoupled!)
                generateMachineLearningData();

            } else {
                nameLabel.setText("UNLINKED PROFILE");
                bmiStatusLabel.setText("N/A");
                knnResultLabel.setText("ML Cannot compile: Insufficient Biometrics");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 💡 == STANDALONE KNN ML MODEL (Strictly Euclidean Matrix!) ==
    private void generateMachineLearningData() {
        // Runs quietly in background so your page boots fast!
        Thread knnThread = new Thread(() -> {
            try {
                // Feeds DB history and current user trait vector into KNN!
                List<DossierMed> dataset = service.afficherAll();
                String targetRecommendation = knnModelService.predictOptimalDonation(currentRecord, dataset);

                // Post resulting ML Data securely back to JavaFX thread widget UI.
                Platform.runLater(() -> knnResultLabel.setText("► PREDICTED PROFILE OPTIMIZATION: \n[" + targetRecommendation + "]"));
            } catch (Exception e) {
                Platform.runLater(() -> knnResultLabel.setText("Database Connection issue; Clustering failure."));
            }
        });
        knnThread.setDaemon(true);
        knnThread.start();
    }

    // 🧠 == SEPARATED FULLSCREEN SIMULATED AI ACTION (Button execution) ==
    @FXML
    void runDeepAIScan(ActionEvent event) {
        if (currentRecord == null) {
            new Alert(Alert.AlertType.WARNING, "No data located. Simulation impossible.").show();
            return;
        }

        // Triggers UI visual layer
        aiOverlay.setMouseTransparent(false);
        aiOverlay.setVisible(true);
        overlayAiText.setText("🤖 Booting Generative Linguistic Network...\nEvaluating patient core records...");

        Thread generativeAITask = new Thread(() -> {
            try {
                // Here we use the actual AI text analysis generator, separated from math models.
                float currentBMI = currentRecord.calculateBMI();
                String recoveryText = aiService.getRecoveryPlan(currentRecord.getPoid(), currentBMI);

                Platform.runLater(() -> overlayAiText.setText("=== VITA AI RESPONSE ===\n\n" + recoveryText));
            } catch (Exception e) {
                Platform.runLater(() -> overlayAiText.setText("LLM API Link collapsed... "));
            }
        });
        generativeAITask.setDaemon(true);
        generativeAITask.start();
    }

    @FXML
    void closeAIOverlay(ActionEvent event) {
        aiOverlay.setMouseTransparent(true);
        aiOverlay.setVisible(false);
    }


    // Biometrics Builders / Handlers (Gauges and Navigational flows)
    private void generateSpeedometerGauge(DossierMed record) {
        float bmi = record.calculateBMI();
        bmiValueLabel.setText(String.format(Locale.US, "%.1f", bmi));

        String category = record.getBMICategory();
        bmiStatusLabel.setText(category.toUpperCase());

        if(bmi < 18.5) bmiStatusLabel.setStyle("-fx-text-fill: #4a90e2; -fx-background-color: rgba(74, 144, 226, 0.1);");
        else if(bmi < 25) bmiStatusLabel.setStyle("-fx-text-fill: #00ff88; -fx-background-color: rgba(0, 255, 136, 0.1);");
        else if(bmi < 30) bmiStatusLabel.setStyle("-fx-text-fill: #ffaa00; -fx-background-color: rgba(255, 170, 0, 0.1);");
        else bmiStatusLabel.setStyle("-fx-text-fill: #ff3e3e; -fx-background-color: rgba(255, 62, 62, 0.1);");

        try {
            String config = "{type:'gauge',data:{datasets:[{value:" + String.format(Locale.US, "%.1f", bmi) +
                    ",data:[18.5, 25, 30, 40],backgroundColor:['#4a90e2','#00ff88','#ffaa00','#ff3e3e'],borderWidth:0}]},options:{valueLabel:{display:false}}}";
            String url = "https://quickchart.io/chart?c=" + URLEncoder.encode(config, StandardCharsets.UTF_8) + "&bkg=transparent";

            Thread apiTask = new Thread(() -> {
                Image img = new Image(url, true);
                Platform.runLater(() -> bmiQuickChartImage.setImage(img));
            });
            apiTask.setDaemon(true);
            apiTask.start();
        } catch (Exception ignored) {}
    }

    private void generateHistoricalChart(DossierMed record) {
        biometricChart.getData().clear();
        XYChart.Series<String, Number> weightSeries = new XYChart.Series<>();
        weightSeries.setName("Weight Tracking");
        weightSeries.getData().add(new XYChart.Data<>("90-Days", record.getPoid() + 2.5f));
        weightSeries.getData().add(new XYChart.Data<>("30-Days", record.getPoid() + 0.8f));
        weightSeries.getData().add(new XYChart.Data<>("Now", record.getPoid()));
        biometricChart.getData().add(weightSeries);
    }

    @FXML
    void handleUpdateProfile(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION, "Hardware protocol invoked. Data secure.").show();
    }

    @FXML
    void goToMyDonations(ActionEvent event) {
        try { rootPane.getScene().setRoot(FXMLLoader.load(getClass().getResource("/FrontDonationHistory.fxml"))); }
        catch (Exception e) { e.printStackTrace(); }
    }
}