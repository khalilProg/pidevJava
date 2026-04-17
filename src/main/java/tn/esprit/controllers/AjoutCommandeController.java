package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.scene.control.*;
import tn.esprit.entities.Commande;
import tn.esprit.services.CommandeService;
import tn.esprit.tools.MyDatabase;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AjoutCommandeController implements Initializable {

    @FXML private TextField tfReference;
    @FXML private TextField tfQuantite;
    @FXML private ComboBox<String> cbTypeSang;
    @FXML private ComboBox<String> cbPriorite;
    @FXML private ComboBox<String> cbBanque;
    @FXML private Label lblStatus;

    // Inline error labels
    @FXML private Label lblErrorReference;
    @FXML private Label lblErrorQuantite;
    @FXML private Label lblErrorTypeSang;
    @FXML private Label lblErrorPriorite;
    @FXML private Label lblErrorBanque;

    private final CommandeService commandeService = new CommandeService();

    private final Map<String, Integer> banqueIdMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Populate Type Sang
        cbTypeSang.setItems(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        ));

        // Populate Priorité
        cbPriorite.setItems(FXCollections.observableArrayList(
                "Haute", "Moyenne", "Basse"
        ));

        // Load Banques from DB
        loadBanques();
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
            System.out.println("❌ Error loading banques: " + e.getMessage());
        }
        cbBanque.setItems(banqueNames);
    }

    private void clearErrors() {
        lblErrorReference.setText("");
        lblErrorQuantite.setText("");
        lblErrorTypeSang.setText("");
        lblErrorPriorite.setText("");
        lblErrorBanque.setText("");
        lblStatus.setText("");
        lblStatus.getStyleClass().removeAll("status-success", "status-error");
    }

    private boolean validateForm() {
        boolean valid = true;

        // Reference
        if (tfReference.getText() == null || tfReference.getText().trim().isEmpty()) {
            lblErrorReference.setText("La référence est obligatoire.");
            valid = false;
        } else {
            try {
                Integer.parseInt(tfReference.getText().trim());
            } catch (NumberFormatException e) {
                lblErrorReference.setText("La référence doit être un nombre.");
                valid = false;
            }
        }

        // Quantité
        if (tfQuantite.getText() == null || tfQuantite.getText().trim().isEmpty()) {
            lblErrorQuantite.setText("La quantité est obligatoire.");
            valid = false;
        } else {
            try {
                int q = Integer.parseInt(tfQuantite.getText().trim());
                if (q <= 0) {
                    lblErrorQuantite.setText("La quantité doit être supérieure à 0.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                lblErrorQuantite.setText("La quantité doit être un nombre.");
                valid = false;
            }
        }

        // Type Sang
        if (cbTypeSang.getValue() == null) {
            lblErrorTypeSang.setText("Veuillez sélectionner un type de sang.");
            valid = false;
        }

        // Priorité
        if (cbPriorite.getValue() == null) {
            lblErrorPriorite.setText("Veuillez sélectionner une priorité.");
            valid = false;
        }

        // Banque
        if (cbBanque.getValue() == null) {
            lblErrorBanque.setText("Veuillez sélectionner une banque.");
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

        int reference = Integer.parseInt(tfReference.getText().trim());
        int quantite = Integer.parseInt(tfQuantite.getText().trim());
        int banqueId = banqueIdMap.getOrDefault(cbBanque.getValue(), 0);

        if (banqueId == 0) {
            lblErrorBanque.setText("Banque invalide.");
            return;
        }

        Commande commande = new Commande(
                banqueId,
                1,
                1,
                reference,
                quantite,
                cbPriorite.getValue(),
                cbTypeSang.getValue(),
                "En attente"
        );

        try {
            commandeService.ajouter(commande);
            navigateBack();
        } catch (SQLException e) {
            showError("❌ Erreur: " + e.getMessage());
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
            javafx.stage.Stage stage = (javafx.stage.Stage) tfReference.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("BLOODLINK — BackOffice");
        } catch (java.io.IOException e) {
            showError("❌ Error returning to list: " + e.getMessage());
        }
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
