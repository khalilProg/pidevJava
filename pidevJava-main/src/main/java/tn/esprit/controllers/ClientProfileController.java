package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import tn.esprit.entities.Client;
import tn.esprit.entities.User;
import tn.esprit.services.ClientService;
import tn.esprit.services.UserService;
import tn.esprit.tools.SessionManager;
import tn.esprit.tools.ThemeManager;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ClientProfileController extends BaseFront implements Initializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private TextField tfTelephone;
    @FXML private ComboBox<String> cbTypeSang;
    @FXML private DatePicker dpDernierDon;
    @FXML private Label lblStatus;
    @FXML private Label lblProfileName;
    @FXML private Label lblProfileEmail;
    @FXML private Button btnThemeToggle;

    private final UserService userService = new UserService();
    private final ClientService clientService = new ClientService();
    private final ThemeManager themeManager = ThemeManager.getInstance();

    private User currentUser;
    private Client currentClient;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applySessionUser();
        cbTypeSang.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        loadProfile();

        javafx.application.Platform.runLater(() -> {
            themeManager.applyTheme(tfNom.getScene());
            themeManager.updateToggleButton(btnThemeToggle);
        });
    }

    @FXML
    private void handleThemeToggle() {
        themeManager.toggleTheme(btnThemeToggle.getScene());
        themeManager.updateToggleButton(btnThemeToggle);
    }

    @FXML
    private void handleSaveProfile() {
        clearStatus();
        if (!validateForm()) {
            return;
        }

        try {
            currentUser.setNom(tfNom.getText().trim());
            currentUser.setPrenom(tfPrenom.getText().trim());
            currentUser.setEmail(tfEmail.getText().trim());
            currentUser.setTel(tfTelephone.getText().trim());
            userService.modifier(currentUser);

            String typeSang = cbTypeSang.getValue();
            LocalDate dernierDon = dpDernierDon.getValue();
            if (currentClient != null) {
                currentClient.setTypeSang(typeSang);
                currentClient.setDernierDon(dernierDon);
                clientService.modifier(currentClient);
            } else if (typeSang != null && dernierDon != null) {
                currentClient = new Client(typeSang, dernierDon, currentUser);
                clientService.ajouter(currentClient);
            }

            SessionManager.setCurrentUser(currentUser);
            applySessionUser();
            updateHeaderSummary();
            showStatus("Profil mis a jour avec succes.", false);
        } catch (SQLException e) {
            showStatus("Erreur lors de la mise a jour: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleCancel() {
        loadProfile();
        showStatus("Modifications annulees.", false);
    }

    private void loadProfile() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showError("Session introuvable. Veuillez vous reconnecter.");
            return;
        }

        try {
            currentClient = clientService.getByUserId(currentUser.getId());
        } catch (SQLException e) {
            showStatus("Impossible de charger les informations client: " + e.getMessage(), true);
        }

        tfNom.setText(value(currentUser.getNom()));
        tfPrenom.setText(value(currentUser.getPrenom()));
        tfEmail.setText(value(currentUser.getEmail()));
        tfTelephone.setText(value(currentUser.getTel()));

        if (currentClient != null) {
            cbTypeSang.setValue(currentClient.getTypeSang());
            dpDernierDon.setValue(currentClient.getDernierDon());
        } else {
            cbTypeSang.setValue(null);
            dpDernierDon.setValue(null);
        }

        updateHeaderSummary();
    }

    private boolean validateForm() {
        if (currentUser == null) {
            showStatus("Session introuvable. Veuillez vous reconnecter.", true);
            return false;
        }
        if (tfNom.getText() == null || tfNom.getText().trim().isEmpty()) {
            showStatus("Le nom est obligatoire.", true);
            return false;
        }
        if (tfPrenom.getText() == null || tfPrenom.getText().trim().isEmpty()) {
            showStatus("Le prenom est obligatoire.", true);
            return false;
        }
        if (tfEmail.getText() == null || !EMAIL_PATTERN.matcher(tfEmail.getText().trim()).matches()) {
            showStatus("Email invalide.", true);
            return false;
        }
        if (tfTelephone.getText() == null || tfTelephone.getText().trim().isEmpty()) {
            showStatus("Le telephone est obligatoire.", true);
            return false;
        }
        if ((cbTypeSang.getValue() == null) != (dpDernierDon.getValue() == null)) {
            showStatus("Renseignez le type sanguin et la date du dernier don ensemble.", true);
            return false;
        }
        return true;
    }

    private void updateHeaderSummary() {
        if (currentUser == null) {
            return;
        }
        String name = (value(currentUser.getPrenom()) + " " + value(currentUser.getNom())).trim();
        lblProfileName.setText(name.isBlank() ? "Utilisateur" : name);
        lblProfileEmail.setText(value(currentUser.getEmail()));
    }

    private void clearStatus() {
        lblStatus.setText("");
        lblStatus.getStyleClass().removeAll("status-success", "status-error");
    }

    private void showStatus(String message, boolean error) {
        lblStatus.getStyleClass().removeAll("status-success", "status-error");
        lblStatus.getStyleClass().add(error ? "status-error" : "status-success");
        lblStatus.setText(message);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Profil");
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        themeManager.styleDialog(dialogPane);
        alert.showAndWait();
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
