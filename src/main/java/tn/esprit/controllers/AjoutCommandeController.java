package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import tn.esprit.entities.Client;
import tn.esprit.entities.Commande;
import tn.esprit.entities.User;
import tn.esprit.services.ClientService;
import tn.esprit.services.CommandeService;
import tn.esprit.services.EmailService;
import tn.esprit.tools.MyDatabase;
import tn.esprit.tools.SessionManager;
import tn.esprit.tools.ThemeManager;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AjoutCommandeController extends BaseFront implements Initializable {

    @FXML private TextField tfQuantite;
    @FXML private ComboBox<String> cbTypeSang;
    @FXML private ComboBox<String> cbPriorite;
    @FXML private ComboBox<String> cbBanque;
    @FXML private Label lblStatus;
    @FXML private Button btnThemeToggle;

    @FXML private Label lblErrorQuantite;
    @FXML private Label lblErrorTypeSang;
    @FXML private Label lblErrorPriorite;
    @FXML private Label lblErrorBanque;

    private final CommandeService commandeService = new CommandeService();
    private final ClientService clientService = new ClientService();
    private final EmailService emailService = new EmailService();
    private final ThemeManager themeManager = ThemeManager.getInstance();

    private final Map<String, Integer> banqueIdMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applySessionUser();

        cbTypeSang.setItems(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        ));

        cbPriorite.setItems(FXCollections.observableArrayList(
                "Haute", "Moyenne", "Basse"
        ));

        loadBanques();

        tn.esprit.tools.AnimationUtils.animateNode(tfQuantite, 200);
        tn.esprit.tools.AnimationUtils.animateNode(cbTypeSang, 300);
        tn.esprit.tools.AnimationUtils.animateNode(cbPriorite, 400);
        tn.esprit.tools.AnimationUtils.animateNode(cbBanque, 500);

        javafx.application.Platform.runLater(() -> {
            themeManager.applyTheme(tfQuantite.getScene());
            themeManager.updateToggleButton(btnThemeToggle);
        });
    }

    @FXML
    private void handleThemeToggle() {
        themeManager.toggleTheme(btnThemeToggle.getScene());
        themeManager.updateToggleButton(btnThemeToggle);
    }

    private void loadBanques() {
        ObservableList<String> banqueNames = FXCollections.observableArrayList();
        try {
            Connection cnx = MyDatabase.getInstance().getCnx();
            ResultSet rs = cnx.createStatement().executeQuery("SELECT id, nom FROM banque");
            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                banqueNames.add(nom);
                banqueIdMap.put(nom, id);
            }
        } catch (SQLException e) {
            System.out.println("Error loading banques: " + e.getMessage());
        }
        cbBanque.setItems(banqueNames);
    }

    private void clearErrors() {
        lblErrorQuantite.setText("");
        lblErrorTypeSang.setText("");
        lblErrorPriorite.setText("");
        lblErrorBanque.setText("");
        lblStatus.setText("");
        lblStatus.getStyleClass().removeAll("status-success", "status-error");
    }

    private boolean validateForm() {
        boolean valid = true;

        if (tfQuantite.getText() == null || tfQuantite.getText().trim().isEmpty()) {
            lblErrorQuantite.setText("La quantite est obligatoire.");
            valid = false;
        } else {
            try {
                int q = Integer.parseInt(tfQuantite.getText().trim());
                if (q <= 0) {
                    lblErrorQuantite.setText("La quantite doit etre superieure a 0.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                lblErrorQuantite.setText("La quantite doit etre un nombre.");
                valid = false;
            }
        }

        if (cbTypeSang.getValue() == null) {
            lblErrorTypeSang.setText("Veuillez selectionner un type de sang.");
            valid = false;
        }

        if (cbPriorite.getValue() == null) {
            lblErrorPriorite.setText("Veuillez selectionner une priorite.");
            valid = false;
        }

        if (cbBanque.getValue() == null) {
            lblErrorBanque.setText("Veuillez selectionner une banque.");
            valid = false;
        }

        return valid;
    }

    @FXML
    private void handleSubmit() {
        clearErrors();

        if (!validateForm()) {
            return;
        }

        int reference = new java.util.Random().nextInt(900000) + 100000;
        int quantite = Integer.parseInt(tfQuantite.getText().trim());
        int banqueId = banqueIdMap.getOrDefault(cbBanque.getValue(), 0);

        if (banqueId == 0) {
            lblErrorBanque.setText("Banque invalide.");
            return;
        }

        try {
            User user = SessionManager.getCurrentUser();
            Client client = resolveCurrentClient(user);
            if (client == null) {
                showError("Session client introuvable. Reconnectez-vous avant de creer une commande.");
                return;
            }

            Commande commande = new Commande(
                    banqueId,
                    client.getId(),
                    1,
                    reference,
                    quantite,
                    cbPriorite.getValue(),
                    cbTypeSang.getValue(),
                    "En attente"
            );

            commandeService.ajouterAndReturnId(commande);
            boolean emailSent = emailService.sendCommandeCreatedEmail(
                    user.getEmail(),
                    buildClientName(user),
                    commande,
                    cbBanque.getValue()
            );
            if (!emailSent) {
                showError("Commande creee, mais l'email n'a pas pu etre envoye.");
                return;
            }

            navigateBack();
        } catch (SQLException e) {
            showError("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        navigateBack();
    }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherCommandes.fxml"));
            javafx.scene.Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof BaseFront frontController) {
                frontController.applySessionUser();
            }
            javafx.stage.Stage stage = (javafx.stage.Stage) tfQuantite.getScene().getWindow();
            themeManager.setScene(stage, root);
            stage.setTitle("BLOODLINK - Commandes");
        } catch (java.io.IOException e) {
            showError("Error returning to list: " + e.getMessage());
        }
    }

    private Client resolveCurrentClient(User user) throws SQLException {
        if (user == null) {
            return null;
        }
        return clientService.getByUserId(user.getId());
    }

    private String buildClientName(User user) {
        if (user == null) {
            return "Client";
        }
        String firstName = user.getPrenom() == null ? "" : user.getPrenom().trim();
        String lastName = user.getNom() == null ? "" : user.getNom().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? "Client" : fullName;
    }

    private void showSuccess(String message) {
        lblStatus.getStyleClass().removeAll("status-error");
        lblStatus.getStyleClass().add("status-success");
        lblStatus.setText(message);
    }

    private void showError(String message) {
        lblStatus.getStyleClass().removeAll("status-success");
        lblStatus.getStyleClass().add("status-error");
        lblStatus.setText(message);
    }
}
