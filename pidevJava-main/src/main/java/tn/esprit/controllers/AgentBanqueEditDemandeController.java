package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import tn.esprit.entities.Banque;
import tn.esprit.entities.Demande;
import tn.esprit.services.BanqueService;
import tn.esprit.services.DemandeService;
import java.sql.SQLException;

public class AgentBanqueEditDemandeController {

    @FXML private Label lblTitle;
    @FXML private ComboBox<Banque> comboBanque;
    @FXML private ComboBox<String> comboType;
    @FXML private TextField txtQuantite;
    @FXML private ComboBox<String> comboUrgence;
    
    @FXML private Label errBanque;
    @FXML private Label errType;
    @FXML private Label errQuantite;
    @FXML private Label errUrgence;

    private Demande currentDemande;
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

    public void initData(Demande d) {
        this.currentDemande = d;
        lblTitle.setText("Modifier Demande #" + d.getId());
        
        // Match existing bank by ID
        for (Banque b : comboBanque.getItems()) {
            if (b.getId() == d.getBanque()) {
                comboBanque.setValue(b);
                break;
            }
        }
        
        comboType.setValue(d.getTypeSang());
        txtQuantite.setText(String.valueOf(d.getQuantite()));
        comboUrgence.setValue(d.getUrgence());
    }

    @FXML
    void handleUpdate(ActionEvent event) {
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
            if (comboBanque.getValue() != null) {
                currentDemande.setBanque(comboBanque.getValue().getId());
            }
            currentDemande.setTypeSang(comboType.getValue());
            currentDemande.setQuantite(quantite);
            currentDemande.setUrgence(comboUrgence.getValue());
            
            service.modifier(currentDemande);
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
