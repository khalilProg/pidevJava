package tn.esprit.controllers; // 1. Package must always be first

// 2. Imports come second
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import tn.esprit.entities.Client;
import tn.esprit.entities.User;
import tn.esprit.services.ClientService;
import tn.esprit.entities.Don;
import tn.esprit.services.AIService;
import tn.esprit.services.ServiceDon;
import tn.esprit.tools.SessionManager;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

// 3. Class definition comes third
public class FrontDonationController implements Initializable {

    // ═══ FXML Fields ═══
    @FXML private StackPane rootPane;
    @FXML private Button btnThemeToggle; // Ensure this matches fx:id in FXML [cite: 54, 132]
    @FXML private TableView<Don> donationTable;
    @FXML private TableColumn<Don, Integer> colId, colEntityId;
    @FXML private TableColumn<Don, String> colType;
    @FXML private TableColumn<Don, Float> colQte;

    @FXML private Label totalVolumeLabel, rankLabel;
    @FXML private BarChart<String, Number> yieldChart;
    @FXML private TextField searchField;
    @FXML private ImageView qrCodeImage;

    @FXML private VBox navMenuPanel;
    @FXML private Region navBackdrop;
    @FXML private HBox menuOverlay;
    @FXML private StackPane aiOverlay;
    @FXML private Label overlayAiText;

    // ═══ Properties ═══
    private boolean isMenuOpen = false;
    private boolean isDarkTheme = true;
    private final ServiceDon service = new ServiceDon();
    private final AIService aiService = new AIService();
    private final ClientService clientService = new ClientService();
    private final ObservableList<Don> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEntityId.setCellValueFactory(new PropertyValueFactory<>("id_entite"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type_don"));
        colQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        loadAndProcessData();
        setupSearch();
    }

    // ═══ The Method That Was Causing Errors ═══
    // It is now correctly placed INSIDE the class
    @FXML
    private void handleThemeToggle(ActionEvent event) {
        if (btnThemeToggle != null && btnThemeToggle.getScene() != null) {
            var scene = btnThemeToggle.getScene();
            scene.getStylesheets().clear();

            if (isDarkTheme) {
                scene.getStylesheets().add(getClass().getResource("/commande-light.css").toExternalForm());
                btnThemeToggle.setText("Dark Mode");
            } else {
                scene.getStylesheets().add(getClass().getResource("/commande.css").toExternalForm());
                btnThemeToggle.setText("Light Mode");
            }
            isDarkTheme = !isDarkTheme;
        }
    }
    @FXML
    void goToAccueil(ActionEvent event) {
        try {
            rootPane.getScene().setRoot(FXMLLoader.load(getClass().getResource("/Home.fxml")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadAndProcessData() {
        try {
            int currentClientId = 1;
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
            myDons.forEach(d -> series.getData().add(new XYChart.Data<>("ID:" + d.getId(), d.getQuantite())));
            yieldChart.getData().setAll(series);

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void calculateHeroTier(float totalVolume) {
        if (totalVolume >= 2000) rankLabel.setText("PLATINUM");
        else if (totalVolume >= 1000) rankLabel.setText("GOLD");
        else if (totalVolume >= 400) rankLabel.setText("SILVER");
        else rankLabel.setText("BRONZE");
    }

    private void generateQRCode(int clientId, float totalVolume) {
        try {
            String qrData = "DONOR ID: " + clientId + "\nYIELD: " + String.format(Locale.US, "%.1f", totalVolume) + "ml";
            String url = "https://quickchart.io/qr?text=" + URLEncoder.encode(qrData, StandardCharsets.UTF_8);
            qrCodeImage.setImage(new Image(url, true));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupSearch() {
        FilteredList<Don> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, newValue) -> {
            filteredData.setPredicate(don -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return don.getType_don().toLowerCase().contains(lower) || String.valueOf(don.getId()).contains(lower);
            });
        });
        SortedList<Don> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(donationTable.comparatorProperty());
        donationTable.setItems(sortedData);
    }

    @FXML
    void runDeepImpactAnalysis(ActionEvent event) {
        aiOverlay.setVisible(true);
        new Thread(() -> {
            try {
                String response = aiService.getVitaSphereSynthesis(masterData);
                Platform.runLater(() -> overlayAiText.setText(response));
            } catch (Exception e) {
                Platform.runLater(() -> overlayAiText.setText("AI Analysis Offline."));
            }
        }).start();
    }

    @FXML void closeAIOverlay(ActionEvent event) { aiOverlay.setVisible(false); }

    @FXML
    void toggleMenu() {
        if (menuOverlay != null) {
            menuOverlay.setVisible(!isMenuOpen);
            menuOverlay.setManaged(!isMenuOpen);
            isMenuOpen = !isMenuOpen;
        }
    }

    @FXML void goToAccueil() { /* Logic */ }
    @FXML void goToHistorique() { /* Logic */ }
    @FXML void goToMyDonations() { /* Logic */ }

    @FXML
    void goToMyProfile(ActionEvent event) {
        try { rootPane.getScene().setRoot(FXMLLoader.load(getClass().getResource("/FrontMedicalProfile.fxml"))); }
        catch (Exception e) { e.printStackTrace(); }
    }
}