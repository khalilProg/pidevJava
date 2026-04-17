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

import java.time.LocalDate;
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

    private DemandeController mainController;
    private Demande demandeToEdit;
    private boolean isEditMode = false;

    private final DemandeService demandeService = new DemandeService();
    private final BanqueService banqueService = new BanqueService();
    private final StockService stockService = new StockService();
    private final TransfertService transfertService = new TransfertService();

    @FXML
    public void initialize() {
        populateBanques();
        setupBanqueComboBox();
    }

    public void setMainController(DemandeController controller) {
        this.mainController = controller;
    }

    public void setEditData(Demande d) {
        this.demandeToEdit = d;
        this.isEditMode = true;

        if (d != null) {
            // Trouver la banque correspondante dans la combo
            for (Banque b : cbBanque.getItems()) {
                if (b.getId() == d.getBanque()) {
                    cbBanque.setValue(b);
                    break;
                }
            }
            comboType.setValue(d.getTypeSang());
            txtQuantite.setText(String.valueOf(d.getQuantite()));
            comboUrgence.setValue(d.getUrgence());
        }
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
            Demande d = isEditMode ? demandeToEdit : new Demande();
            Banque selectedBanque = cbBanque.getValue();
            
            d.setBanque(selectedBanque.getId());
            d.setTypeSang(comboType.getValue());
            d.setQuantite(Integer.parseInt(txtQuantite.getText()));
            d.setUrgence(comboUrgence.getValue());
            d.setUpdatedAt(LocalDateTime.now());

            if (isEditMode) {
                demandeService.modifier(d);
                System.out.println("Demande modifiée avec succès");
            } else {
                d.setStatus("EN_ATTENTE"); 
                d.setCreatedAt(LocalDateTime.now());
                demandeService.ajouter(d);
                System.out.println("Demande créée avec succès (En Attente)");
            }

            if (mainController != null) mainController.loadData();
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
