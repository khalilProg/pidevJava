package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.CampagneService;
import tn.esprit.services.DemandeService;
import tn.esprit.services.RendezVousService;
import tn.esprit.services.StockService;
import tn.esprit.services.UserService;
import tn.esprit.tools.SessionManager;
import tn.esprit.tools.ThemeManager;

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
    @FXML private Label welcomeAdminLabel;

    private final UserService userService = new UserService();
    private final DemandeService demandeService = new DemandeService();
    private final StockService stockService = new StockService();
    private final CampagneService campagneService = new CampagneService();
    private final RendezVousService rendezVousService = new RendezVousService();
    private final ThemeManager themeManager = ThemeManager.getInstance();

    @FXML
    public void initialize() {
        applySessionUser();
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            if (totalUsersLabel != null) {
                totalUsersLabel.setText(String.valueOf(userService.getTotalUsers()));
            }
            if (totalDemandesLabel != null) {
                totalDemandesLabel.setText(String.valueOf(demandeService.getTotalDemandes()));
            }
            if (totalStockLabel != null) {
                totalStockLabel.setText(stockService.getTotalQuantity() + " L");
            }
            if (stockPieChart != null) {
                stockPieChart.setData(toPieData(stockService.getStockStats(), "L"));
                stockPieChart.setTitle("Par groupe sanguin");
            }
            if (demandeBarChart != null) {
                setBarData(demandeBarChart, "Demandes", demandeService.getStatusStats());
            }
            if (campagneBarChart != null) {
                setBarData(campagneBarChart, "Campagnes", campagneService.getCampagnesParMois());
            }
            if (rdvPieChart != null) {
                rdvPieChart.setData(toPieData(rendezVousService.getStatusStats(), ""));
                rdvPieChart.setTitle("Par statut");
            }
        } catch (SQLException e) {
            System.err.println("Dashboard error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ObservableList<PieChart.Data> toPieData(Map<String, Integer> stats, String suffix) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            String unit = suffix == null || suffix.isEmpty() ? "" : suffix;
            data.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + unit + ")", entry.getValue()));
        }
        return data;
    }

    private void setBarData(BarChart<String, Number> chart, String name, Map<String, Integer> stats) {
        chart.setAnimated(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            if (entry.getKey() != null) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        }
        chart.getData().clear();
        chart.getData().add(series);
    }

    @FXML
    void handleLogout(Event event) {
        SessionManager.clear();
        navigateTo(event, "/login.fxml");
    }

    @FXML
    void handleNavigateDashboard(Event event) {
        refreshDashboard();
    }

    @FXML
    void handleNavigateUsers(Event event) {
        navigateTo(event, "/admin_users.fxml");
    }

    @FXML
    void handleNavigateCommandes(Event event) {
        navigateTo(event, "/AdminAfficherCommandes.fxml");
    }

    @FXML
    void handleNavigateStocks(Event event) {
        navigateTo(event, "/AfficherStocks.fxml");
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

    @FXML
    void handleNavigateRendezVousClick(MouseEvent event) {
        navigateTo(event, "/ListeRdvAdmin.fxml");
    }

    @FXML
    void handleNavigateQuestionnairesClick(MouseEvent event) {
        navigateTo(event, "/ListeQuestAdmin.fxml");
    }

    @FXML
    void handleNavigateCollectesClick(MouseEvent event) {
        navigateTo(event, "/ListeEntitesAdmin.fxml");
    }

    @FXML
    void handleNavigateCampagnesClick(MouseEvent event) {
        navigateTo(event, "/ListeCampagnesAdmin.fxml");
    }

    private void navigateTo(Event event, String path) {
        try {
            AdminSidebarController.setCurrentPath(path);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            themeManager.setScene(stage, root);
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applySessionUser() {
        User user = SessionManager.getCurrentUser();
        if (user == null || welcomeAdminLabel == null) {
            return;
        }

        String firstName = user.getPrenom() == null ? "" : user.getPrenom().trim();
        String lastName = user.getNom() == null ? "" : user.getNom().trim();
        String fullName = (firstName + " " + lastName).trim();
        welcomeAdminLabel.setText("Bienvenue, " + (fullName.isEmpty() ? "Administrateur" : fullName));
    }
}
