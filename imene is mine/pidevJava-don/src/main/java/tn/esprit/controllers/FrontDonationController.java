package tn.esprit.controllers;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.esprit.entities.Don;
import tn.esprit.services.AIService;
import tn.esprit.services.ServiceDon;
import tn.esprit.tools.MockSession;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FrontDonationController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private TableView<Don> donationTable;
    @FXML private TableColumn<Don, Integer> colId, colEntityId;
    @FXML private TableColumn<Don, String> colType;
    @FXML private TableColumn<Don, Float> colQte;

    @FXML private Label aiImpactLabel, totalVolumeLabel, rankLabel;
    @FXML private BarChart<String, Number> yieldChart;
    @FXML private TextField searchField;
    @FXML private ImageView qrCodeImage; // ONLY ONE DECLARATION

    @FXML private VBox navMenuPanel;
    @FXML private Region navBackdrop;
    private boolean isMenuOpen = false;

    private ServiceDon service = new ServiceDon();
    private AIService aiService = new AIService();
    private ObservableList<Don> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEntityId.setCellValueFactory(new PropertyValueFactory<>("id_entite"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type_don"));
        colQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        loadAndProcessData();
        setupSearch();
    }

    private void loadAndProcessData() {
        try {
            int clientId = MockSession.getClientId();
            List<Don> myDons = service.afficherAll().stream()
                    .filter(d -> d.getId_client() == clientId)
                    .collect(Collectors.toList());

            masterData.setAll(myDons);

            float sum = (float) myDons.stream().mapToDouble(Don::getQuantite).sum();
            totalVolumeLabel.setText(String.format(Locale.US, "%.1f", sum));

            // Execute logic
            calculateHeroTier(sum);
            generateQRCode(clientId, sum);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Volume Extracted");
            for (int i = 0; i < myDons.size(); i++) {
                series.getData().add(new XYChart.Data<>("Batch " + (i + 1), myDons.get(i).getQuantite()));
            }
            yieldChart.getData().clear();
            yieldChart.getData().add(series);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateHeroTier(float totalVolume) {
        // Safe guard for rankLabel
        if (rankLabel == null) return;

        if (totalVolume >= 2000) {
            rankLabel.setText("PLATINUM ARCHITECT");
            rankLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-weight: 900;");
        } else if (totalVolume >= 1000) {
            rankLabel.setText("GOLD TIER");
            rankLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold;");
        } else if (totalVolume >= 400) {
            rankLabel.setText("SILVER TIER");
            rankLabel.setStyle("-fx-text-fill: #c0c0c0; -fx-font-weight: bold;");
        } else {
            rankLabel.setText("BRONZE INITIATE");
            rankLabel.setStyle("-fx-text-fill: #cd7f32;");
        }
    }

    private void generateQRCode(int clientId, float totalVolume) {
        try {
            String qrData = "BLOODLINK VITA-PASSPORT\nDonor ID: " + clientId + "\nStatus: Verified\nTotal Yield: " + String.format(Locale.US, "%.1f", totalVolume) + "ml";
            String encodedData = URLEncoder.encode(qrData, StandardCharsets.UTF_8);
            String url = "https://quickchart.io/qr?text=" + encodedData + "&size=150&dark=e63939";

            new Thread(() -> {
                Image img = new Image(url, true);
                Platform.runLater(() -> {
                    // CRITICAL: Always use a null check for @FXML elements
                    if (qrCodeImage != null) {
                        qrCodeImage.setImage(img);
                    }
                });
            }).start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupSearch() {
        FilteredList<Don> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, newValue) -> {
            filteredData.setPredicate(don -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return don.getType_don().toLowerCase().contains(newValue.toLowerCase());
            });
        });
        SortedList<Don> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(donationTable.comparatorProperty());
        donationTable.setItems(sortedData);
    }

    @FXML void toggleMenu() {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.3), navMenuPanel);
        if (!isMenuOpen) {
            transition.setToX(0);
            navBackdrop.setOpacity(1.0);
            navBackdrop.setMouseTransparent(false);
        } else {
            transition.setToX(450);
            navBackdrop.setOpacity(0.0);
            navBackdrop.setMouseTransparent(true);
        }
        transition.play();
        isMenuOpen = !isMenuOpen;
    }

    @FXML void runImpactAnalysis(ActionEvent event) {
        aiImpactLabel.setText("🧠 AI Analyzing Legacy...");
        new Thread(() -> {
            try {
                String response = aiService.getVitaSphereSynthesis(masterData);
                Platform.runLater(() -> aiImpactLabel.setText(response));
            } catch (Exception e) {
                Platform.runLater(() -> aiImpactLabel.setText("Connection failed. Check API Key."));
            }
        }).start();
    }

    @FXML void goToMyProfile(ActionEvent event) {
        try {
            URL loc = getClass().getResource("/FrontMedicalProfile.fxml");
            if (loc == null) return;
            rootPane.getScene().setRoot(FXMLLoader.load(loc));
        } catch (Exception e) { e.printStackTrace(); }
    }
}