package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.entities.Client;
import tn.esprit.services.ClientService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AdminUsersCompleteClientController implements Initializable {

    @FXML
    private Label subtitleLabel;
    @FXML
    private ComboBox<String> groupeSangCombo;
    @FXML
    private DatePicker dernierDonPicker;

    @FXML
    private Label groupeSangError;
    @FXML
    private Label dernierDonError;

    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        if (this.currentUser != null) {
            subtitleLabel.setText("Informations de santé pour le compte #" + this.currentUser.getId() + ".");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> bloodTypes = FXCollections.observableArrayList(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        );
        groupeSangCombo.setItems(bloodTypes);
    }

    @FXML
    void handleConfirm(ActionEvent event) {
        clearErrors();

        String groupeSanguin = groupeSangCombo.getValue();
        LocalDate dernierDon = dernierDonPicker.getValue();

        boolean valid = true;

        if (groupeSanguin == null || groupeSanguin.isEmpty()) {
            showFieldError(groupeSangError, "Veuillez sélectionner un groupe sanguin.");
            valid = false;
        }

        if (dernierDon == null) {
            showFieldError(dernierDonError, "Veuillez sélectionner une date.");
            valid = false;
        }

        if (!valid) {
            return;
        }

        if (currentUser == null) {
            showFieldError(groupeSangError, "Erreur fatale: utilisateur non trouvé.");
            return;
        }

        Client nouveauClient = new Client(groupeSanguin, dernierDon, currentUser);
        ClientService clientService = new ClientService();

        try {
            clientService.ajouter(nouveauClient);
            navigateTo(event, "/admin_users.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showFieldError(groupeSangError, "Erreur d'ajout: " + e.getMessage());
        }
    }

    private void showFieldError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearErrors() {
        Label[] errors = { groupeSangError, dernierDonError };
        for (Label lbl : errors) {
            lbl.setText("");
            lbl.setVisible(false);
            lbl.setManaged(false);
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
    void handleNavigateDemandes(ActionEvent event) {
        navigateTo(event, "/DemandeBackView.fxml");
    }

    @FXML
    void handleNavigateTransferts(ActionEvent event) {
        navigateTo(event, "/TransfertBackView.fxml");
    }

    private void navigateTo(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
