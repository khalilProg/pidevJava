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
import javafx.stage.Stage;
import tn.esprit.entities.Commande;
import tn.esprit.services.CommandeService;
import tn.esprit.tools.MyDatabase;

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

    @FXML
    private void handleSubmit() {
        try {
            lblStatus.setText("");
            lblStatus.setStyle("-fx-text-fill: white;");
            
            if (tfReference.getText().isEmpty() || tfQuantite.getText().isEmpty() ||
                cbTypeSang.getValue() == null || cbPriorite.getValue() == null || 
                cbStatut.getValue() == null || cbBanque.getValue() == null) {
                
                lblStatus.setText("⚠ Veuillez remplir tous les champs.");
                lblStatus.setStyle("-fx-text-fill: #e74c3c;"); // error red
                return;
            }

            int quantite = Integer.parseInt(tfQuantite.getText());
            int reference = Integer.parseInt(tfReference.getText());
            int banqueId = banqueIdMap.getOrDefault(cbBanque.getValue(), 0);

            if (quantite <= 0) {
                lblStatus.setText("⚠ Quantité invalide.");
                lblStatus.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            currentCommande.setReference(reference);
            currentCommande.setQuantite(quantite);
            currentCommande.setTypeSang(cbTypeSang.getValue());
            currentCommande.setPriorite(cbPriorite.getValue());
            currentCommande.setStatus(cbStatut.getValue());
            currentCommande.setBanqueId(banqueId);

            commandeService.modifier(currentCommande);

            navigateBack();
        } catch (NumberFormatException e) {
            lblStatus.setText("⚠ Saisie invalide pour Quantité ou Référence.");
            lblStatus.setStyle("-fx-text-fill: #e74c3c;");
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
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("BLOODLINK — Gestion des Commandes");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to navigate to AdminAfficherCommandes.fxml");
        }
    }
}
