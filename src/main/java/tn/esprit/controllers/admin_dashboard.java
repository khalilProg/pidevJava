package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import tn.esprit.services.CampagneService;
import tn.esprit.services.DemandeService;
import tn.esprit.services.RendezVousService;
import tn.esprit.services.StockService;
import tn.esprit.services.UserService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class admin_dashboard {
    @FXML private Label totalUsersLabel;
    @FXML private Label totalDemandesLabel;
    @FXML private Label totalStockLabel;
    @FXML private PieChart stockPieChart;
    @FXML private PieChart rdvPieChart;
    @FXML private BarChart<String, Number> demandeBarChart;
    @FXML private BarChart<String, Number> campagneBarChart;

    private final UserService userService = new UserService();
    private final DemandeService demandeService = new DemandeService();
    private final StockService stockService = new StockService();
    private final CampagneService campagneService = new CampagneService();
    private final RendezVousService rendezVousService = new RendezVousService();

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            // 1. KPI Counters
            totalUsersLabel.setText(String.valueOf(userService.getTotalUsers()));
            totalDemandesLabel.setText(String.valueOf(demandeService.getTotalDemandes()));
            totalStockLabel.setText(stockService.getTotalQuantity() + " L");

            // 2. Stock PieChart — distribution by blood type
            Map<String, Integer> stockStats = stockService.getStockStats();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : stockStats.entrySet()) {
                pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + "L)", entry.getValue()));
            }
            stockPieChart.setData(pieData);
            stockPieChart.setTitle("Par groupe sanguin");

            // 3. Demande BarChart — count by status
            demandeBarChart.setAnimated(false);
            Map<String, Integer> demandStats = demandeService.getStatusStats();
            XYChart.Series<String, Number> demandeSeries = new XYChart.Series<>();
            demandeSeries.setName("Demandes");
            for (Map.Entry<String, Integer> entry : demandStats.entrySet()) {
                if (entry.getKey() != null) {
                    demandeSeries.getData().add(new XYChart.Data<>(entry.getKey(), (Number) entry.getValue()));
                }
            }
            demandeBarChart.getData().clear();
            demandeBarChart.getData().add(demandeSeries);

            // 4. Campagnes BarChart — count per month
            campagneBarChart.setAnimated(false);
            Map<String, Integer> campagneStats = campagneService.getCampagnesParMois();
            XYChart.Series<String, Number> campagneSeries = new XYChart.Series<>();
            campagneSeries.setName("Campagnes");
            for (Map.Entry<String, Integer> entry : campagneStats.entrySet()) {
                if (entry.getKey() != null) {
                    campagneSeries.getData().add(new XYChart.Data<>(entry.getKey(), (Number) entry.getValue()));
                }
            }
            campagneBarChart.getData().clear();
            campagneBarChart.getData().add(campagneSeries);

            // 5. Rendez-vous PieChart — count by status
            Map<String, Integer> rdvStats = rendezVousService.getStatusStats();
            ObservableList<PieChart.Data> rdvPieData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : rdvStats.entrySet()) {
                rdvPieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
            rdvPieChart.setData(rdvPieData);
            rdvPieChart.setTitle("Par statut");

        } catch (SQLException e) {
            System.err.println("Dashboard error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(Event event) {
        navigateTo(event, "/login.fxml");
    }

    @FXML
    void handleNavigateDashboard(Event event) {
        // We are already on the Dashboard, do nothing
    }

    @FXML
    void handleNavigateUsers(Event event) {
        navigateTo(event, "/admin_users.fxml");
    }

    @FXML
    void handleNavigateDemandes(Event event) {
        navigateTo(event, "/DemandeBackView.fxml");
    }

    @FXML
    void handleNavigateTransferts(Event event) {
        navigateTo(event, "/TransfertBackView.fxml");
    }

    @FXML
    void handleNavigateQuestionnaires(Event event) {
        navigateTo(event, "/ListeQuestAdmin.fxml");
    }

    @FXML
    void handleNavigateRendezVous(Event event) {
        navigateTo(event, "/ListeRdvAdmin.fxml");
    }

    @FXML
    void handleNavigateCampagnes(Event event) {
        navigateTo(event, "/ListeCampagnesAdmin.fxml");
    }

    @FXML
    void handleNavigateCollectes(Event event) {
        navigateTo(event, "/ListeEntitesAdmin.fxml");
    }


    private void navigateTo(Event event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
