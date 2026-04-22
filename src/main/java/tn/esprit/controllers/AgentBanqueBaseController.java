package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class AgentBanqueBaseController {

    @FXML private HBox menuOverlay;
    @FXML private Label sessionEmailLabel;
    @FXML private StackPane contentArea;
    
    private tn.esprit.entities.User currentUser;
    
    private static AgentBanqueBaseController instance;

    @FXML
    public void initialize() {
        instance = this;
        // Load the default page (Mes Demandes)
        loadView("/AgentBanqueDemande.fxml");
    }

    public static AgentBanqueBaseController getInstance() {
        return instance;
    }

    public void initData(tn.esprit.entities.User user) {
        this.currentUser = user;
        if (sessionEmailLabel != null && user != null) {
            sessionEmailLabel.setText(user.getEmail());
        }
    }

    public void loadView(String fxmlPath) {
        if (contentArea == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public void loadView(Parent root) {
        if (contentArea != null && root != null) {
            contentArea.getChildren().setAll(root);
        }
    }

    @FXML
    void handleMenuToggle(ActionEvent event) {
        if (menuOverlay != null) {
            boolean isVisible = menuOverlay.isVisible();
            menuOverlay.setVisible(!isVisible);
            menuOverlay.setManaged(!isVisible);
        }
    }

    @FXML
    void handleMenuClose(javafx.scene.input.MouseEvent event) {
        if (menuOverlay != null) {
            menuOverlay.setVisible(false);
            menuOverlay.setManaged(false);
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToAccueil(ActionEvent event) {
        // Implement when page exists
        System.out.println("Go to Accueil");
    }

    @FXML
    public void goToDemandes(ActionEvent event) {
        loadView("/AgentBanqueDemande.fxml");
        handleMenuClose(null);
    }

    @FXML
    public void goToTransferts(ActionEvent event) {
        loadView("/AgentBanqueTransfert.fxml");
        handleMenuClose(null);
    }
    
    @FXML
    public void goToProfil(ActionEvent event) {
        // Implement when page exists
        System.out.println("Go to Profil");
    }
}
