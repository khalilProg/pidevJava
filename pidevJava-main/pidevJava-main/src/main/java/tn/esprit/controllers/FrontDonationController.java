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

import tn.esprit.entities.Client;      // ⬅️ TEAM INTEGRATION
import tn.esprit.entities.User;        // ⬅️ TEAM INTEGRATION
import tn.esprit.services.ClientService; // ⬅️ TEAM INTEGRATION
import tn.esprit.entities.Don;
import tn.esprit.services.AIService;
import tn.esprit.services.ServiceDon;
import tn.esprit.tools.SessionManager;

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

    @FXML private Label totalVolumeLabel, rankLabel;
    @FXML private BarChart<String, Number> yieldChart;
    @FXML private TextField searchField;
    @FXML private ImageView qrCodeImage;

    // DRAWER Menu fields
    @FXML private VBox navMenuPanel;
    @FXML private Region navBackdrop;
    private boolean isMenuOpen = false;

    // AI Overlay fields!
    @FXML private StackPane aiOverlay;
    @FXML private Label overlayAiText;

    private ServiceDon service = new ServiceDon();
    private AIService aiService = new AIService();
    private ClientService clientService = new ClientService(); // ⬅️ TEAM INTEGRATION
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
            // ⬅️ TEAM INTEGRATION: Fetch the logged-in User, then find their Client ID
            int currentClientId = 1; // Default fallback for dev testing
            User loggedUser = SessionManager.getCurrentUser();

            if (loggedUser != null) {
                Client loggedClient = clientService.getByUserId(loggedUser.getId());
                if (loggedClient != null) {
                    currentClientId = loggedClient.getId();
                }
            }

            final int searchId = currentClientId;

            List<Don> myDons = service.afficherAll().stream()
                    .filter(d -> d.getId_client() == searchId)
                    .collect(Collectors.toList());

            masterData.setAll(myDons);

            float sum = (float) myDons.stream().mapToDouble(Don::getQuantite).sum();
            totalVolumeLabel.setText(String.format(Locale.US, "%.1f", sum));

            calculateHeroTier(sum);
            generateQRCode(searchId, sum);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (int i = 0; i < myDons.size(); i++) {
                series.getData().add(new XYChart.Data<>("ID:" + myDons.get(i).getId(), myDons.get(i).getQuantite()));
            }
            yieldChart.getData().clear();
            yieldChart.getData().add(series);

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void calculateHeroTier(float totalVolume) {
        if (totalVolume >= 2000) {
            rankLabel.setText("PLATINUM");
            rankLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 20; -fx-font-weight: 900;");
        } else if (totalVolume >= 1000) {
            rankLabel.setText("GOLD");
            rankLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 20; -fx-font-weight: 900;");
        } else if (totalVolume >= 400) {
            rankLabel.setText("SILVER");
            rankLabel.setStyle("-fx-text-fill: #c0c0c0; -fx-font-size: 20; -fx-font-weight: 900;");
        } else {
            rankLabel.setText("BRONZE");
            rankLabel.setStyle("-fx-text-fill: #cd7f32; -fx-font-size: 20; -fx-font-weight: 900;");
        }
    }

    private void generateQRCode(int clientId, float totalVolume) {
        try {
            String qrData = "DONOR ID: " + clientId + "\nYIELD: " + String.format(Locale.US, "%.1f", totalVolume) + "ml";
            String url = "https://quickchart.io/qr?text=" + URLEncoder.encode(qrData, StandardCharsets.UTF_8) + "&size=140&margin=0";

            Thread qrTask = new Thread(() -> {
                Image img = new Image(url, true);
                Platform.runLater(() -> qrCodeImage.setImage(img));
            });
            qrTask.setDaemon(true);
            qrTask.start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupSearch() {
        FilteredList<Don> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, newValue) -> {
            filteredData.setPredicate(don -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return don.getType_don().toLowerCase().contains(newValue.toLowerCase()) || String.valueOf(don.getId()).contains(newValue);
            });
        });
        SortedList<Don> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(donationTable.comparatorProperty());
        donationTable.setItems(sortedData);
    }

    // THE OVERLAY AI METHOD!!
    @FXML
    void runDeepImpactAnalysis(ActionEvent event) {
        if(masterData.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No extraction data found on the ledger. Submit a bio-fluid pack first!").show();
            return;
        }

        aiOverlay.setMouseTransparent(false);
        aiOverlay.setVisible(true);
        overlayAiText.setText("⚡ Routing through VitaSphere databanks... cross referencing patient outcomes... hold please...");

        Thread aiTask = new Thread(() -> {
            try {
                // Generates deep narrative using AI backend!
                String response = aiService.getVitaSphereSynthesis(masterData);
                Platform.runLater(() -> overlayAiText.setText(response));
            } catch (Exception e) {
                Platform.runLater(() -> overlayAiText.setText("Network interruption. Impact Synthesis currently offline."));
            }
        });
        aiTask.setDaemon(true);
        aiTask.start();
    }

    @FXML
    void closeAIOverlay(ActionEvent event) {
        aiOverlay.setMouseTransparent(true);
        aiOverlay.setVisible(false);
    }

    // ANIMATES AND TRIGGERS MENU OPENING
    @FXML
    void toggleMenu() {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.3), navMenuPanel);
        transition.setToX(!isMenuOpen ? 0 : 450);
        navBackdrop.setOpacity(!isMenuOpen ? 1.0 : 0.0);
        navBackdrop.setMouseTransparent(isMenuOpen);
        transition.play();
        isMenuOpen = !isMenuOpen;
    }

    @FXML
    void goToMyProfile(ActionEvent event) {
        try { rootPane.getScene().setRoot(FXMLLoader.load(getClass().getResource("/FrontMedicalProfile.fxml"))); }
        catch (Exception e) { e.printStackTrace(); }
    }
}