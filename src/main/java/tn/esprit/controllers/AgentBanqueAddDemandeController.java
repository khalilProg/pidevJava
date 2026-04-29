package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import java.sql.SQLException;
import java.time.LocalDateTime;

import tn.esprit.entities.Banque;
import tn.esprit.entities.Demande;
import tn.esprit.services.BanqueService;
import tn.esprit.services.DemandeService;

public class AgentBanqueAddDemandeController {

    @FXML private ComboBox<Banque> comboBanque;
    @FXML private ComboBox<String> comboType;
    @FXML private TextField txtQuantite;
    @FXML private ComboBox<String> comboUrgence;
    
    @FXML private Label errBanque;
    @FXML private Label errType;
    @FXML private Label errQuantite;
    @FXML private Label errUrgence;

    private DemandeService service = new DemandeService();
    private BanqueService banqueService = new BanqueService();

    @FXML
    public void initialize() {
        comboType.getItems().addAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
        comboUrgence.getItems().addAll("Normale", "Urgente");
        
        setupBanqueComboBox();
    }

    private void setupBanqueComboBox() {
        try {
            comboBanque.getItems().addAll(banqueService.recuperer());
            comboBanque.setConverter(new StringConverter<Banque>() {
                @Override
                public String toString(Banque banque) {
                    return banque == null ? "" : banque.getNom();
                }

                @Override
                public Banque fromString(String string) {
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleAddDemande(ActionEvent event) {
        boolean isValid = true;
        
        // Reset hiding
        errBanque.setVisible(false); errBanque.setManaged(false);
        errType.setVisible(false); errType.setManaged(false);
        errQuantite.setVisible(false); errQuantite.setManaged(false);
        errUrgence.setVisible(false); errUrgence.setManaged(false);

        if (comboBanque.getValue() == null) {
            errBanque.setText("Veuillez sélectionner une banque.");
            errBanque.setVisible(true); errBanque.setManaged(true);
            isValid = false;
        }
        
        if (comboType.getValue() == null) {
            errType.setText("Veuillez choisir un type de sang.");
            errType.setVisible(true); errType.setManaged(true);
            isValid = false;
        }
        
        if (comboUrgence.getValue() == null) {
            errUrgence.setText("Veuillez sélectionner l'urgence.");
            errUrgence.setVisible(true); errUrgence.setManaged(true);
            isValid = false;
        }

        int quantite = 0;
        if (txtQuantite.getText().isEmpty()) {
            errQuantite.setText("La quantité est requise.");
            errQuantite.setVisible(true); errQuantite.setManaged(true);
            isValid = false;
        } else {
            try {
                quantite = Integer.parseInt(txtQuantite.getText());
                if (quantite <= 0) {
                    errQuantite.setText("La quantité doit être supérieure à zéro.");
                    errQuantite.setVisible(true); errQuantite.setManaged(true);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                errQuantite.setText("Veuillez entrer un nombre entier valide.");
                errQuantite.setVisible(true); errQuantite.setManaged(true);
                isValid = false;
            }
        }
        
        if (!isValid) return;

        try {
            Demande d = new Demande();
            d.setBanque(comboBanque.getValue().getId());
            d.setTypeSang(comboType.getValue());
            d.setQuantite(quantite);
            d.setUrgence(comboUrgence.getValue());
            d.setStatus("En Attente");
            d.setCreatedAt(LocalDateTime.now());
            
            service.ajouter(d);
            
            // Navigate back
            goBack(event);
            
        } catch (SQLException e) {
            errBanque.setText("Erreur système: " + e.getMessage());
            errBanque.setVisible(true); errBanque.setManaged(true);
            e.printStackTrace();
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        AgentBanqueBaseController.getInstance().loadView("/AgentBanqueDemande.fxml");
    }
}
