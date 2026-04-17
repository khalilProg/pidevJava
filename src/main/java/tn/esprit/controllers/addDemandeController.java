package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.entities.Demande;
import tn.esprit.entities.Banque;
import tn.esprit.entities.Transfert;
import tn.esprit.services.DemandeService;
import tn.esprit.services.BanqueService;
import tn.esprit.services.StockService;
import tn.esprit.services.TransfertService;

import java.time.LocalDateTime;
import java.util.List;

public class addDemandeController {

    @FXML private ComboBox<Banque> cbBanque;
    @FXML private Label banqueError;

    @FXML private ComboBox<String> comboType;
    @FXML private Label typeError;

    @FXML private TextField txtQuantite;
    @FXML private Label quantiteError;

    @FXML private ComboBox<String> comboUrgence;
    @FXML private Label urgenceError;

    private final DemandeService demandeService = new DemandeService();
    private final BanqueService banqueService = new BanqueService();
    private final StockService stockService = new StockService();
    private final TransfertService transfertService = new TransfertService();

    @FXML
    public void initialize() {
        populateBanques();
        setupBanqueComboBox();
    }

    private void populateBanques() {
        try {
            List<Banque> banques = banqueService.recuperer();
            cbBanque.setItems(FXCollections.observableArrayList(banques));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupBanqueComboBox() {
        cbBanque.setConverter(new StringConverter<Banque>() {
            @Override
            public String toString(Banque b) {
                return (b != null) ? b.getNom() : "";
            }

            @Override
            public Banque fromString(String string) {
                return null; 
            }
        });
    }

    @FXML
    public void ajouterDemande() {
        if (!validateForm()) return;

        try {
            // 1. Créer la Demande
            Demande d = new Demande();
            Banque selectedBanque = cbBanque.getValue();
            
            d.setBanque(selectedBanque.getId());
            d.setTypeSang(comboType.getValue());
            d.setQuantite(Integer.parseInt(txtQuantite.getText()));
            d.setUrgence(comboUrgence.getValue());
            d.setStatus("CONFIRME"); // Automatiquement confirmée car stock vérifié
            d.setCreatedAt(LocalDateTime.now());
            d.setUpdatedAt(LocalDateTime.now());

            demandeService.ajouter(d);

            // 2. Créer le Transfert associé (EN_ATTENTE de validation de dates)
            Transfert t = new Transfert();
            t.setDemande(d);
            t.setFromOrgId(1); // BloodLink Central
            t.setFromOrg("BloodLink Central");
            t.setToOrgId(selectedBanque.getId());
            t.setToOrg(selectedBanque.getNom());
            t.setQuantite(d.getQuantite());
            t.setStatus("EN_ATTENTE");
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            
            // On peut mettre un stock_id par défaut ou chercher le premier stock du type
            // Pour l'instant on laisse à 1 (ou 0) car le choix final se fera à l'acceptation
            t.setStock(1); 

            transfertService.ajouter(t);

            System.out.println("Demande et Transfert créés avec succès");
            goBack();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        resetErrors();

        // VAL: Banque
        if (cbBanque.getValue() == null) {
            showError(banqueError, cbBanque, "Veuillez sélectionner une banque");
            isValid = false;
        }

        // VAL: Type Sang
        if (comboType.getValue() == null) {
            showError(typeError, comboType, "Type de sang obligatoire");
            isValid = false;
        }

        // VAL: Quantité
        String qStr = txtQuantite.getText();
        if (qStr == null || qStr.trim().isEmpty()) {
            showError(quantiteError, txtQuantite, "Quantité obligatoire");
            isValid = false;
        } else {
            try {
                int q = Integer.parseInt(qStr);
                if (q <= 0) {
                    showError(quantiteError, txtQuantite, "La quantité doit être positive");
                    isValid = false;
                } else if (comboType.getValue() != null) {
                    // VERIF STOCK
                    int available = stockService.getAvailableQuantity(comboType.getValue());
                    if (q > available) {
                        showError(quantiteError, txtQuantite, "Stock insuffisant (Disponible: " + available + " UNITÉS)");
                        isValid = false;
                    }
                }
            } catch (NumberFormatException e) {
                showError(quantiteError, txtQuantite, "Format invalide");
                isValid = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // VAL: Urgence
        if (comboUrgence.getValue() == null) {
            showError(urgenceError, comboUrgence, "Niveau d'urgence obligatoire");
            isValid = false;
        }

        return isValid;
    }

    private void showError(Label errorLabel, Control field, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        if (!field.getStyleClass().contains("text-field-error")) {
            field.getStyleClass().add("text-field-error");
        }
    }

    private void resetErrors() {
        Label[] labels = {banqueError, typeError, quantiteError, urgenceError};
        Control[] fields = {cbBanque, comboType, txtQuantite, comboUrgence};

        for (Label l : labels) {
            if (l != null) {
                l.setVisible(false);
                l.setManaged(false);
            }
        }
        for (Control c : fields) {
            if (c != null) {
                c.getStyleClass().remove("text-field-error");
            }
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DemandeBackView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtQuantite.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
