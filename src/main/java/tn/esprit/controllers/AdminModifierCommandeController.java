package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.stage.Stage;
import tn.esprit.entities.Commande;
import tn.esprit.services.CommandeService;
import tn.esprit.tools.MyDatabase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AdminModifierCommandeController {

    @FXML private TextField tfReference;
    @FXML private ComboBox<String> cbTypeSang;
    @FXML private TextField tfQuantite;
    @FXML private ComboBox<String> cbPriorite;
    @FXML private ComboBox<String> cbStatut;
    @FXML private ComboBox<String> cbBanque;
    @FXML private Label lblStatus;

    // Inline error labels
    @FXML private Label lblErrorReference;
    @FXML private Label lblErrorQuantite;
    @FXML private Label lblErrorTypeSang;
    @FXML private Label lblErrorPriorite;
    @FXML private Label lblErrorStatut;
    @FXML private Label lblErrorBanque;

    private final CommandeService commandeService = new CommandeService();
    private Commande currentCommande;
    private final Connection cnx = MyDatabase.getInstance().getCnx();
    private final Map<String, Integer> banqueIdMap = new HashMap<>();

    @FXML
    public void initialize() {
        cbTypeSang.setItems(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        ));
        cbPriorite.setItems(FXCollections.observableArrayList(
                "Haute", "Moyenne", "Basse"
        ));
        cbStatut.setItems(FXCollections.observableArrayList(
                "En attente", "Validée", "Refusée", "Annulée"
        ));
        loadBanques();
    }

    private void loadBanques() {
        ObservableList<String> banqueNames = FXCollections.observableArrayList();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT id, nom FROM banque");
            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                banqueNames.add(nom);
                banqueIdMap.put(nom, id);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur chargement des banques: " + e.getMessage());
        }
        cbBanque.setItems(banqueNames);
    }

    public void initData(Commande commande) {
        this.currentCommande = commande;
        tfReference.setText(String.valueOf(commande.getReference()));
        tfQuantite.setText(String.valueOf(commande.getQuantite()));
        cbTypeSang.setValue(commande.getTypeSang());
        cbPriorite.setValue(commande.getPriorite());
        cbStatut.setValue(commande.getStatus());

        for (Map.Entry<String, Integer> entry : banqueIdMap.entrySet()) {
            if (entry.getValue() == commande.getBanqueId()) {
                cbBanque.setValue(entry.getKey());
                break;
            }
        }
    }

    private void clearErrors() {
        lblErrorReference.setText("");
        lblErrorQuantite.setText("");
        lblErrorTypeSang.setText("");
        lblErrorPriorite.setText("");
        lblErrorStatut.setText("");
        lblErrorBanque.setText("");
        lblStatus.setText("");
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

        // Statut
        if (cbStatut.getValue() == null) {
            lblErrorStatut.setText("Veuillez sélectionner un statut.");
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

        try {
            int quantite = Integer.parseInt(tfQuantite.getText().trim());
            int reference = Integer.parseInt(tfReference.getText().trim());
            int banqueId = banqueIdMap.getOrDefault(cbBanque.getValue(), 0);

            currentCommande.setReference(reference);
            currentCommande.setQuantite(quantite);
            currentCommande.setTypeSang(cbTypeSang.getValue());
            currentCommande.setPriorite(cbPriorite.getValue());
            currentCommande.setStatus(cbStatut.getValue());
            currentCommande.setBanqueId(banqueId);

            commandeService.modifier(currentCommande);
            navigateBack();
        } catch (SQLException e) {
            lblStatus.setText("⚠ Erreur Base de Données.");
            lblStatus.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        navigateBack();
    }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminAfficherCommandes.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tfReference.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("BLOODLINK — Gestion des Commandes");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to navigate to AdminAfficherCommandes.fxml");
        }
    }

    @FXML
    private void goToCommandes() {
        navigateBack();
    }

    @FXML
    private void goToStocks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherStocks.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tfReference.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("BLOODLINK — Inventaire des Stocks");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
