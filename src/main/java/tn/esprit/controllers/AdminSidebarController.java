package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import tn.esprit.tools.ThemeManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AdminSidebarController {

    @FXML private Button btnThemeToggle;
    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnCommandes;
    @FXML private Button btnStocks;
    @FXML private Button btnDemandes;
    @FXML private Button btnTransferts;
    @FXML private Button btnQuestionnaires;
    @FXML private Button btnRendezVous;
    @FXML private Button btnCampagnes;
    @FXML private Button btnCollectes;

    private final ThemeManager themeManager = ThemeManager.getInstance();
    private static String currentPath = "/admin_dashboard.fxml";

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (btnThemeToggle != null && btnThemeToggle.getScene() != null) {
                themeManager.applyTheme(btnThemeToggle.getScene());
                themeManager.updateToggleButton(btnThemeToggle);
            }
            updateActiveNavigation();
        });
    }

    @FXML
    void handleThemeToggle() {
        if (btnThemeToggle != null && btnThemeToggle.getScene() != null) {
            themeManager.toggleTheme(btnThemeToggle.getScene());
            themeManager.updateToggleButton(btnThemeToggle);
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        navigateTo(event, "/login.fxml");
    }

    @FXML
    void handleNavigateDashboard(ActionEvent event) {
        navigateTo(event, "/admin_dashboard.fxml");
    }

    @FXML
    void handleNavigateUsers(ActionEvent event) {
        navigateTo(event, "/admin_users.fxml");
    }

    @FXML
    void handleNavigateCommandes(ActionEvent event) {
        navigateTo(event, "/AdminAfficherCommandes.fxml");
    }

    @FXML
    void handleNavigateStocks(ActionEvent event) {
        navigateTo(event, "/AfficherStocks.fxml");
    }

    @FXML
    void handleNavigateDemandes(ActionEvent event) {
        navigateTo(event, "/DemandeBackView.fxml");
    }

    @FXML
    void handleNavigateTransferts(ActionEvent event) {
        navigateTo(event, "/TransfertBackView.fxml");
    }

    @FXML
    void handleNavigateQuestionnaires(ActionEvent event) {
        navigateTo(event, "/ListeQuestAdmin.fxml");
    }

    @FXML
    void handleNavigateRendezVous(ActionEvent event) {
        navigateTo(event, "/ListeRdvAdmin.fxml");
    }

    @FXML
    void handleNavigateCampagnes(ActionEvent event) {
        navigateTo(event, "/ListeCampagnesAdmin.fxml");
    }

    @FXML
    void handleNavigateCollectes(ActionEvent event) {
        navigateTo(event, "/ListeEntitesAdmin.fxml");
    }

    private void navigateTo(ActionEvent event, String path) {
        try {
            currentPath = path;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            themeManager.applyTheme(stage.getScene());
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateActiveNavigation() {
        List<Button> buttons = Arrays.asList(
                btnDashboard,
                btnUsers,
                btnCommandes,
                btnStocks,
                btnDemandes,
                btnTransferts,
                btnQuestionnaires,
                btnRendezVous,
                btnCampagnes,
                btnCollectes
        );

        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("sidebar-nav-item-active");
                if (!button.getStyleClass().contains("sidebar-nav-item")) {
                    button.getStyleClass().add("sidebar-nav-item");
                }
            }
        }

        Button activeButton = getActiveButton();
        if (activeButton != null) {
            activeButton.getStyleClass().remove("sidebar-nav-item");
            if (!activeButton.getStyleClass().contains("sidebar-nav-item-active")) {
                activeButton.getStyleClass().add("sidebar-nav-item-active");
            }
        }
    }

    private Button getActiveButton() {
        if (currentPath == null) return btnDashboard;

        if (currentPath.contains("admin_users")) return btnUsers;
        if (currentPath.contains("Commande")) return btnCommandes;
        if (currentPath.contains("Stock")) return btnStocks;
        if (currentPath.contains("Demande")) return btnDemandes;
        if (currentPath.contains("Transfert")) return btnTransferts;
        if (currentPath.contains("Quest")) return btnQuestionnaires;
        if (currentPath.contains("Rdv") || currentPath.contains("RendezVous")) return btnRendezVous;
        if (currentPath.contains("Campagne")) return btnCampagnes;
        if (currentPath.contains("Entite") || currentPath.contains("Collecte")) return btnCollectes;
        return btnDashboard;
    }
}
