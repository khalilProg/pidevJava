package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.entities.*;
import tn.esprit.services.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AjouterQuestAdmin {

    @FXML private TextField ageField;
    @FXML private ComboBox<String> sexeCombo;
    @FXML private TextField poidsField;
    @FXML private TextArea autresField;
    @FXML private ComboBox<Campagne> campagneCombo;
    @FXML private TextField phoneField;
    @FXML private Button continuerBtn;
    @FXML private Button annulerBtn;
    @FXML private Text ageError;
    @FXML private Text sexeError;
    @FXML private Text poidsError;
    @FXML private Text campagneError;
    @FXML private Text phoneError;

    private Campagne selectedCampagne;
    private client client;

    @FXML
    public void initialize() throws SQLException {
        // --- Initialize sexes ---
        sexeCombo.setItems(FXCollections.observableArrayList("Homme", "Femme"));

        // --- Load campaigns ---
        CampagneService cs = new CampagneService();
        List<Campagne> campagneList = cs.recuperer();
        campagneCombo.setItems(FXCollections.observableArrayList(campagneList));

        // Display only the title
        campagneCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Campagne item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitre());
            }
        });
        campagneCombo.setButtonCell(campagneCombo.getCellFactory().call(null));
    }

    @FXML
    private void handleContinuer() throws SQLException, IOException {
        boolean valid = true;

        // --- Reset errors ---
        ageError.setVisible(false);
        sexeError.setVisible(false);
        poidsError.setVisible(false);
        campagneError.setVisible(false);
        phoneError.setVisible(false);

        // --- Age validation ---
        int ageValue = 0;
        if (ageField.getText().isEmpty()) {
            ageError.setText("L'âge ne peut pas être vide");
            ageError.setVisible(true);
            valid = false;
        } else {
            try {
                ageValue = Integer.parseInt(ageField.getText());
                if (ageValue < 18 || ageValue > 70) {
                    ageError.setText("L'âge doit être entre 18 et 70 ans");
                    ageError.setVisible(true);
                    valid = false;
                }
            } catch (NumberFormatException e) {
                ageError.setText("L'âge doit être un nombre valide");
                ageError.setVisible(true);
                valid = false;
            }
        }

        // --- Sexe validation ---
        if (sexeCombo.getValue() == null || sexeCombo.getValue().isEmpty()) {
            sexeError.setText("Le sexe ne peut pas être vide");
            sexeError.setVisible(true);
            valid = false;
        }

        // --- Poids validation ---
        double poidsValue = 0;
        if (poidsField.getText().isEmpty()) {
            poidsError.setText("Le poids ne peut pas être vide");
            poidsError.setVisible(true);
            valid = false;
        } else {
            try {
                poidsValue = Double.parseDouble(poidsField.getText());
                if (poidsValue < 50 || poidsValue > 100) {
                    poidsError.setText("Le poids doit être entre 50 et 100 kg");
                    poidsError.setVisible(true);
                    valid = false;
                }
            } catch (NumberFormatException e) {
                poidsError.setText("Le poids doit être un nombre valide");
                poidsError.setVisible(true);
                valid = false;
            }
        }

        // --- Phone validation ---
        if (phoneField.getText().isEmpty()) {
            phoneError.setText("Le numéro de téléphone ne peut pas être vide");
            phoneError.setVisible(true);
            valid = false;
        }

        // --- Campaign validation ---
        selectedCampagne = campagneCombo.getValue();
        if (selectedCampagne == null) {
            campagneError.setText("Veuillez choisir une campagne");
            campagneError.setVisible(true);
            valid = false;
        }

        if (!valid) return; // stop if validation fails

        // --- Fetch client by phone ---
        client = new ClientService().getByPhone(phoneField.getText());
        if (client == null) {
            phoneError.setText("client introuvable pour ce numéro");
            phoneError.setVisible(true);
            return;
        }

        // --- Create Questionnaire ---
        Questionnaire q = new Questionnaire(
                client.getUser().getNom(),
                client.getUser().getPrenom(),
                ageValue,
                sexeCombo.getValue(),
                poidsValue,
                autresField.getText(),
                client.getId(),
                selectedCampagne.getId(),
                LocalDateTime.now(),
                client.getTypeSang()
        );

        // --- Load AjouterRendezVous page ---
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezVousAdmin.fxml"));
        Parent root = loader.load();
        AjouterRendezVous rdvController = loader.getController();
        rdvController.setCampagne(selectedCampagne);

        // Fetch entities linked to campagne
        List<EntiteDeCollecte> entities = new EntiteCollecteService().getByCampagneId(selectedCampagne.getId());
        rdvController.setEntities(entities);
        rdvController.setQuestionnaire(q);

        // Show new scene
        continuerBtn.getScene().setRoot(root);
    }

    // ── Navigation Handlers ──

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

    @FXML
    void handleNavigateQuestionnaires(ActionEvent event) {
        navigateTo(event, "/ListeQuestAdmin.fxml");
    }

    @FXML
    void handleNavigateRendezVous(ActionEvent event) {
        navigateTo(event, "/ListeRdvAdmin.fxml");
    }

    @FXML
    void handleNavigateCampagnes(ActionEvent event) {
        navigateTo(event, "/ListeCampagnesAdmin.fxml");
    }

    @FXML
    void handleNavigateCollectes(ActionEvent event) {
        navigateTo(event, "/ListeEntitesAdmin.fxml");
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