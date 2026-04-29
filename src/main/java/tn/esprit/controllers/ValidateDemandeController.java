package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Demande;
import tn.esprit.entities.Transfert;
import tn.esprit.services.DemandeService;
import tn.esprit.services.EmailService;
import tn.esprit.services.TransfertService;
import tn.esprit.services.BanqueService;
import tn.esprit.entities.Banque;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ValidateDemandeController {

    @FXML private Label lblId, lblType, lblQuantite, lblBanque;
    @FXML private DatePicker dateEnvoi, dateReception;
    @FXML private Label errorEnvoi, errorReception;

    private Demande selectedDemande;
    private Banque selectedBanque;
    private final DemandeService demandeService = new DemandeService();
    private final TransfertService transfertService = new TransfertService();
    private final BanqueService banqueService = new BanqueService();
    private final EmailService emailService = new EmailService();

    @FXML
    public void initialize() {
        dateEnvoi.setValue(LocalDate.now());
        dateReception.setValue(LocalDate.now().plusDays(1));
    }

    public void setDemandeData(Demande d) {
        this.selectedDemande = d;
        lblId.setText("#" + d.getId());
        lblType.setText(d.getTypeSang());
        lblQuantite.setText(d.getQuantite() + " UNITÉS");

        // Fetch bank name and cache the Banque object
        try {
            for (Banque b : banqueService.recuperer()) {
                if (b.getId() == d.getBanque()) {
                    lblBanque.setText(b.getNom());
                    this.selectedBanque = b; // cache for email
                    break;
                }
            }
        } catch (Exception e) {
            lblBanque.setText("Banque ID: " + d.getBanque());
        }
    }

    @FXML
    public void handleValidate() {
        System.out.println("Bouton Valider cliqué...");
        if (!validateDates()) {
            System.out.println("Dates invalides !");
            return;
        }

        try {
            System.out.println("Tentative de validation de la demande #" + selectedDemande.getId());
            // 1. Mettre à jour la Demande
            selectedDemande.setStatus("VALIDEE");
            selectedDemande.setUpdatedAt(LocalDateTime.now());
            demandeService.modifier(selectedDemande);

            // 2. Créer le Transfert
            Transfert t = new Transfert();
            t.setDemande(selectedDemande);
            t.setFromOrgId(1);
            t.setFromOrg("BloodLink Central");
            t.setToOrgId(selectedDemande.getBanque());
            t.setToOrg(lblBanque.getText());
            t.setQuantite(selectedDemande.getQuantite());
            t.setStatus("EN_COURS"); // Initial status for shipping
            t.setDateEnvoie(dateEnvoi.getValue());
            t.setDateReception(dateReception.getValue());
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            t.setStock(1);

            transfertService.ajouter(t);

            // 3. Envoyer l'email de notification au responsable de la banque
            if (selectedBanque != null && selectedBanque.getUser() != null) {
                String userEmail = selectedBanque.getUser().getEmail();
                String userName = selectedBanque.getUser().getPrenom() + " " + selectedBanque.getUser().getNom();
                String banqueName = selectedBanque.getNom();
                String dateEnvoiStr = dateEnvoi.getValue() != null ? dateEnvoi.getValue().toString() : "N/A";
                String dateRecepStr = dateReception.getValue() != null ? dateReception.getValue().toString() : "N/A";


                new Thread(() -> emailService.sendDemandeValideeEmail(
                        userEmail, userName, banqueName,
                        selectedDemande.getId(), selectedDemande.getTypeSang(),
                        selectedDemande.getQuantite(), dateEnvoiStr, dateRecepStr
                )).start();

                System.out.println("Email de notification envoyé à : " + userEmail);
            }

            System.out.println("Demande validée et Transfert créé !");
            handleBack();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateDates() {
        boolean valid = true;
        LocalDate start = dateEnvoi.getValue();
        LocalDate end = dateReception.getValue();

        if (start == null) {
            errorEnvoi.setVisible(true);
            valid = false;
        } else {
            errorEnvoi.setVisible(false);
        }

        if (end == null || (start != null && end.isBefore(start))) {
            errorReception.setVisible(true);
            valid = false;
        } else {
            errorReception.setVisible(false);
        }

        return valid;
    }

    @FXML
    public void handleBack() {
        System.out.println("Retour au dashboard des demandes...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DemandeBackView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblId.getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNavigateDashboard(javafx.event.ActionEvent event) {
        navigateTo(event, "/admin_dashboard.fxml");
    }

    @FXML
    public void handleNavigateUsers(javafx.event.ActionEvent event) {
        navigateTo(event, "/admin_users.fxml");
    }

    @FXML
    public void handleNavigateDemandes(javafx.event.ActionEvent event) {
        navigateTo(event, "/DemandeBackView.fxml");
    }

    @FXML
    public void handleNavigateTransferts(javafx.event.ActionEvent event) {
        navigateTo(event, "/TransfertBackView.fxml");
    }

    @FXML
    public void handleLogout(javafx.event.ActionEvent event) {
        navigateTo(event, "/login.fxml");
    }

    private void navigateTo(javafx.event.ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
