package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import tn.esprit.entities.User;
import tn.esprit.tools.SessionManager;
import tn.esprit.tools.ThemeManager;

import java.io.IOException;

public class AgentBanqueBaseController extends BaseFront {

    @FXML private StackPane contentArea;

    private User currentUser;

    private static AgentBanqueBaseController instance;

    @Override
    public void initialize() {
        super.initialize();
        instance = this;
        // Load the default page (Mes Demandes)
        loadView("/AgentBanqueDemande.fxml");
    }

    public static AgentBanqueBaseController getInstance() {
        return instance;
    }

    public void initData(User user) {
        this.currentUser = user;
        SessionManager.setCurrentUser(user);
        applySessionUser();
    }

    public void loadView(String fxmlPath) {
        if (contentArea == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
            // Apply theme to the loaded sub-view
            if (contentArea.getScene() != null) {
                ThemeManager.getInstance().applyTheme(contentArea.getScene());
            }
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public void loadView(Parent root) {
        if (contentArea != null && root != null) {
            contentArea.getChildren().setAll(root);
            if (contentArea.getScene() != null) {
                ThemeManager.getInstance().applyTheme(contentArea.getScene());
            }
        }
    }

    // Override navigation to keep Agent Banque routing — do NOT navigate to client pages
    @Override
    public void goToAccueil(javafx.event.Event event) {
        // Agent Banque has no separate accueil — just go to demandes
        loadView("/AgentBanqueDemande.fxml");
        closeMenu();
    }

    @Override
    public void goToCampagnes(javafx.event.Event event) {
        // Not applicable for Agent Banque
    }

    @Override
    public void goToCommandes(javafx.event.Event event) {
        // Not applicable for Agent Banque
    }

    @Override
    public void goToProfile(javafx.event.Event event) {
        // Implement when profile page exists
        System.out.println("Go to Profil");
        closeMenu();
    }

    @FXML
    public void goToDemandes(ActionEvent event) {
        loadView("/AgentBanqueDemande.fxml");
        closeMenu();
    }

    @FXML
    public void goToTransferts(ActionEvent event) {
        loadView("/AgentBanqueTransfert.fxml");
        closeMenu();
    }

    @FXML
    public void goToProfil(ActionEvent event) {
        // Implement when profile page exists
        System.out.println("Go to Profil");
        closeMenu();
    }

    private void closeMenu() {
        if (menuOverlay != null) {
            menuOverlay.setVisible(false);
            menuOverlay.setManaged(false);
            if (menuToggleBtn != null) {
                menuToggleBtn.setText("MENU");
                tn.esprit.tools.IconUtils.decorateButton(menuToggleBtn);
            }
        }
    }
}
