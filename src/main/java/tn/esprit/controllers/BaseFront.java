package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.tools.SessionManager;
import tn.esprit.tools.ThemeManager;

import java.io.IOException;

public class BaseFront {

    @FXML protected HBox menuOverlay;
    @FXML protected Button menuToggleBtn;
    @FXML protected Label sessionEmailLabel;
    @FXML protected Button userNameBtn;

    protected void switchScene(javafx.event.Event event, String fxmlPath) {
        switchSceneFromNode((Node) event.getSource(), fxmlPath);
    }

    protected void switchSceneFromNode(Node source, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof BaseFront frontController) {
                frontController.applySessionUser();
            }

            Stage stage = (Stage) source.getScene().getWindow();
            ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    protected void applySessionUser() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }

        String displayName = buildDisplayName(user);
        Button displayButton = userNameBtn != null ? userNameBtn : findButtonWithStyleClass(resolveRootNode(), "user-badge");
        if (displayButton != null) {
            displayButton.setText("\uD83D\uDC64 " + displayName);
        }
        if (sessionEmailLabel != null) {
            String email = user.getEmail();
            sessionEmailLabel.setText(email == null || email.isBlank() ? "Session: " + displayName : "Session: " + email);
        }
    }

    private String buildDisplayName(User user) {
        String firstName = user.getPrenom() == null ? "" : user.getPrenom().trim();
        String lastName = user.getNom() == null ? "" : user.getNom().trim();
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isEmpty()) {
            return fullName;
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }
        return "Utilisateur";
    }

    private Node resolveRootNode() {
        Node node = userNameBtn != null ? userNameBtn : menuToggleBtn;
        if (node == null) {
            node = sessionEmailLabel;
        }
        while (node != null && node.getParent() != null) {
            node = node.getParent();
        }
        return node;
    }

    private Button findButtonWithStyleClass(Node node, String styleClass) {
        if (node instanceof Button button && button.getStyleClass().contains(styleClass)) {
            return button;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Button match = findButtonWithStyleClass(child, styleClass);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    @FXML
    protected void handleMenuToggle(ActionEvent event) {
        if (menuOverlay != null) {
            boolean isVisible = menuOverlay.isVisible();
            menuOverlay.setVisible(!isVisible);
            menuOverlay.setManaged(!isVisible);
            if (menuToggleBtn != null) {
                menuToggleBtn.setText(isVisible ? "MENU \u2630" : "FERMER \u2715");
            }
        }
    }

    @FXML
    protected void handleMenuClose(javafx.scene.input.MouseEvent event) {
        if (menuOverlay != null) {
            menuOverlay.setVisible(false);
            menuOverlay.setManaged(false);
            if (menuToggleBtn != null) {
                menuToggleBtn.setText("MENU \u2630");
            }
        }
    }

    @FXML
    protected void handleLogout(ActionEvent event) {
        SessionManager.clear();
        switchScene(event, "/login.fxml");
    }

    @FXML
    public void goToAccueil(javafx.event.Event event) {
        switchScene(event, "/client_home.fxml");
    }

    @FXML
    public void goToCampagnes(javafx.event.Event event) {
        switchScene(event, "/ListeCampagnes.fxml");
    }

    @FXML
    public void goToCommandes(javafx.event.Event event) {
        switchScene(event, "/AfficherCommandes.fxml");
    }

    @FXML
    public void goToCalendar(javafx.event.Event event) {
        switchScene(event, "/CampagneCalendar.fxml");
    }

    @FXML
    public void goToHistorique(javafx.event.Event event) {
        switchScene(event, "/Liste.fxml");
    }
}
