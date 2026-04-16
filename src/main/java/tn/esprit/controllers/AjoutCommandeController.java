package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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

    private final CommandeService commandeService = new CommandeService();

    // Maps banque display name -> actual DB id
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

    @FXML
    private void handleSubmit() {
        // Clear previous status
        lblStatus.setText("");
        lblStatus.getStyleClass().removeAll("status-success", "status-error");

        // Validate fields
        if (tfReference.getText().isEmpty() || tfQuantite.getText().isEmpty()
                || cbTypeSang.getValue() == null || cbPriorite.getValue() == null
                || cbBanque.getValue() == null) {
            showError("⚠ Veuillez remplir tous les champs.");
            return;
        }

        int reference;
        int quantite;
        try {
            reference = Integer.parseInt(tfReference.getText().trim());
            quantite = Integer.parseInt(tfQuantite.getText().trim());
        } catch (NumberFormatException e) {
            showError("⚠ Référence et quantité doivent être des nombres.");
            return;
        }

        if (quantite <= 0) {
            showError("⚠ La quantité doit être supérieure à 0.");
            return;
        }

        // Get real banque ID from the map
        int banqueId = banqueIdMap.getOrDefault(cbBanque.getValue(), 0);
        if (banqueId == 0) {
            showError("⚠ Banque invalide.");
            return;
        }

        // Create commande (client_id=1, stock_id=1 as defaults)
        Commande commande = new Commande(
                banqueId,
                1, // client_id
                1, // stock_id
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
            stage.setScene(new javafx.scene.Scene(root, 1000, 700));
            stage.setTitle("BLOODLINK — Mes Commandes");
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
