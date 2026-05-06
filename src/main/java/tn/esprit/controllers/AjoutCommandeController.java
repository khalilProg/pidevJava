package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import tn.esprit.entities.Client;
import tn.esprit.entities.Commande;
import tn.esprit.entities.Stock;
import tn.esprit.entities.User;
import tn.esprit.services.BloodCompatibilityService;
import tn.esprit.services.ClientService;
import tn.esprit.services.CommandeService;
import tn.esprit.services.EmailService;
import tn.esprit.services.StockService;
import tn.esprit.services.StripeCheckoutService;
import tn.esprit.services.UserService;
import tn.esprit.tools.MyDatabase;
import tn.esprit.tools.SessionManager;
import tn.esprit.tools.ThemeManager;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AjoutCommandeController extends BaseFront implements Initializable {

    @FXML private TextField tfQuantite;
    @FXML private ComboBox<String> cbTypeSang;
    @FXML private ComboBox<String> cbPriorite;
    @FXML private ComboBox<String> cbBanque;
    @FXML private Label lblStatus;
    @FXML private Label lblAutoFillInfo;
    @FXML private Label lblCompatibilityInfo;
    @FXML private Label lblPaymentInfo;
    @FXML private Button btnThemeToggle;

    @FXML private Label lblErrorQuantite;
    @FXML private Label lblErrorTypeSang;
    @FXML private Label lblErrorPriorite;
    @FXML private Label lblErrorBanque;

    private final CommandeService commandeService = new CommandeService();
    private final ClientService clientService = new ClientService();
    private final EmailService emailService = new EmailService();
    private final StockService stockService = new StockService();
    private final StripeCheckoutService stripeCheckoutService = new StripeCheckoutService();
    private final UserService userService = new UserService();
    private final ThemeManager themeManager = ThemeManager.getInstance();

    private final Map<String, Integer> banqueIdMap = new HashMap<>();
    private Commande pendingPaymentCommande;
    private User pendingPaymentUser;
    private String pendingPaymentClientName;
    private String pendingPaymentBanqueName;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applySessionUser();
        setupFeedbackVisibility();

        cbTypeSang.setItems(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        ));

        cbPriorite.setItems(FXCollections.observableArrayList(
                "Haute", "Moyenne", "Basse"
        ));

        loadBanques();
        setupComboPresentation();
        setupSmartAssistance();

        tn.esprit.tools.AnimationUtils.animateNode(tfQuantite, 200);
        tn.esprit.tools.AnimationUtils.animateNode(cbTypeSang, 300);
        tn.esprit.tools.AnimationUtils.animateNode(cbPriorite, 400);
        tn.esprit.tools.AnimationUtils.animateNode(cbBanque, 500);

        javafx.application.Platform.runLater(() -> {
            themeManager.applyTheme(tfQuantite.getScene());
            themeManager.updateToggleButton(btnThemeToggle);
        });
    }

    private void setupFeedbackVisibility() {
        configureOptionalFeedback(lblStatus);
        configureOptionalFeedback(lblPaymentInfo);
    }

    private void configureOptionalFeedback(Label label) {
        if (label == null) {
            return;
        }
        updateOptionalFeedbackVisibility(label, label.getText());
        label.textProperty().addListener((observable, oldValue, newValue) ->
                updateOptionalFeedbackVisibility(label, newValue)
        );
    }

    private void updateOptionalFeedbackVisibility(Label label, String text) {
        boolean hasMessage = text != null && !text.isBlank();
        label.setVisible(hasMessage);
        label.setManaged(hasMessage);
    }

    private void setupComboPresentation() {
        configureFrontCombo(cbPriorite);
        configureFrontCombo(cbTypeSang);
        configureFrontCombo(cbBanque);
    }

    private void configureFrontCombo(ComboBox<String> comboBox) {
        comboBox.setVisibleRowCount(6);
        comboBox.setCellFactory(listView -> createFrontComboCell("front-commande-dropdown-cell"));
        comboBox.setButtonCell(createFrontComboCell("front-commande-button-cell"));
    }

    private ListCell<String> createFrontComboCell(String styleClass) {
        return new ListCell<>() {
            {
                getStyleClass().add(styleClass);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setGraphic(null);
            }
        };
    }

    @FXML
    private void handleThemeToggle() {
        themeManager.toggleTheme(btnThemeToggle.getScene());
        themeManager.updateToggleButton(btnThemeToggle);
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
            System.out.println("Error loading banques: " + e.getMessage());
        }
        cbBanque.setItems(banqueNames);
    }

    private void clearErrors() {
        lblErrorQuantite.setText("");
        lblErrorTypeSang.setText("");
        lblErrorPriorite.setText("");
        lblErrorBanque.setText("");
        lblStatus.setText("");
        lblPaymentInfo.setText("");
        lblStatus.getStyleClass().removeAll("status-success", "status-error");
    }

    private boolean validateForm() {
        boolean valid = true;

        if (tfQuantite.getText() == null || tfQuantite.getText().trim().isEmpty()) {
            lblErrorQuantite.setText("La quantite est obligatoire.");
            valid = false;
        } else {
            try {
                int q = Integer.parseInt(tfQuantite.getText().trim());
                if (q <= 0) {
                    lblErrorQuantite.setText("La quantite doit etre superieure a 0.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                lblErrorQuantite.setText("La quantite doit etre un nombre.");
                valid = false;
            }
        }

        if (cbTypeSang.getValue() == null) {
            lblErrorTypeSang.setText("Veuillez selectionner un type de sang.");
            valid = false;
        }

        if (cbPriorite.getValue() == null) {
            lblErrorPriorite.setText("Veuillez selectionner une priorite.");
            valid = false;
        }

        if (cbBanque.getValue() == null) {
            lblErrorBanque.setText("Veuillez selectionner une banque.");
            valid = false;
        }

        return valid;
    }

    @FXML
    private void handleSubmit() {
        clearErrors();

        if (pendingPaymentCommande != null) {
            if (launchStripeCheckout(pendingPaymentCommande, pendingPaymentUser, pendingPaymentClientName, pendingPaymentBanqueName)) {
                navigateBack();
            } else {
                showError("Commande deja creee. Paiement Stripe toujours indisponible.");
            }
            return;
        }

        if (!validateForm()) {
            return;
        }

        int reference = new java.util.Random().nextInt(900000) + 100000;
        int quantite = Integer.parseInt(tfQuantite.getText().trim());
        int banqueId = banqueIdMap.getOrDefault(cbBanque.getValue(), 0);
        String requestedTypeSang = cbTypeSang.getValue();

        if (banqueId == 0) {
            lblErrorBanque.setText("Banque invalide.");
            return;
        }

        try {
            User user = SessionManager.getCurrentUser();
            Client client = resolveCurrentClient(user);
            if (client == null) {
                showError("Session client introuvable. Reconnectez-vous avant de creer une commande.");
                return;
            }

            Stock selectedStock = resolveStockForRequest(requestedTypeSang, quantite, banqueId);
            if (selectedStock == null) {
                return;
            }

            Commande commande = new Commande(
                    banqueId,
                    client.getId(),
                    selectedStock.getId(),
                    reference,
                    quantite,
                    cbPriorite.getValue(),
                    selectedStock.getTypeSang(),
                    "En attente"
            );

            commandeService.ajouterAndReturnId(commande);
            String clientName = buildClientName(user);
            boolean emailSent = emailService.sendCommandeCreatedEmail(
                    user.getEmail(),
                    clientName,
                    commande,
                    cbBanque.getValue()
            );
            if (!emailSent) {
                lblPaymentInfo.setText("Commande creee, mais l'email client n'a pas pu etre envoye.");
            }

            notifyAdmins(commande, user, clientName, cbBanque.getValue(), requestedTypeSang);
            pendingPaymentCommande = commande;
            pendingPaymentUser = user;
            pendingPaymentClientName = clientName;
            pendingPaymentBanqueName = cbBanque.getValue();
            if (launchStripeCheckout(commande, user, clientName, cbBanque.getValue())) {
                navigateBack();
            } else {
                showError("Commande creee, mais le paiement Stripe n'a pas ete lance. Vous pouvez revenir a la liste.");
            }
        } catch (SQLException e) {
            showError("Erreur: " + e.getMessage());
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
            Object controller = loader.getController();
            if (controller instanceof BaseFront frontController) {
                frontController.applySessionUser();
            }
            javafx.stage.Stage stage = (javafx.stage.Stage) tfQuantite.getScene().getWindow();
            themeManager.setScene(stage, root);
            stage.setTitle("BLOODLINK - Commandes");
        } catch (java.io.IOException e) {
            showError("Error returning to list: " + e.getMessage());
        }
    }

    private Client resolveCurrentClient(User user) throws SQLException {
        if (user == null) {
            return null;
        }
        return clientService.getByUserId(user.getId());
    }

    private String buildClientName(User user) {
        if (user == null) {
            return "Client";
        }
        String firstName = user.getPrenom() == null ? "" : user.getPrenom().trim();
        String lastName = user.getNom() == null ? "" : user.getNom().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? "Client" : fullName;
    }

    private void setupSmartAssistance() {
        cbTypeSang.valueProperty().addListener((observable, oldValue, newValue) -> refreshSmartAssistance());
        tfQuantite.textProperty().addListener((observable, oldValue, newValue) -> refreshSmartAssistance());
        cbBanque.valueProperty().addListener((observable, oldValue, newValue) -> refreshSmartAssistance());
        autoFillFromCurrentClient();
    }

    private void autoFillFromCurrentClient() {
        StringBuilder info = new StringBuilder();
        try {
            User user = SessionManager.getCurrentUser();
            Client client = resolveCurrentClient(user);
            if (client != null) {
                String profileBloodType = BloodCompatibilityService.normalizeBloodType(client.getTypeSang());
                if (!profileBloodType.isBlank() && cbTypeSang.getItems().contains(profileBloodType)) {
                    cbTypeSang.setValue(profileBloodType);
                    info.append("Type sanguin pre-rempli depuis votre profil. ");
                }
            }
        } catch (SQLException e) {
            info.append("Profil client indisponible. ");
        }

        if (tfQuantite.getText() == null || tfQuantite.getText().trim().isEmpty()) {
            tfQuantite.setText("1");
            info.append("Quantite initiale proposee: 1 ml. ");
        }

        if (cbPriorite.getValue() == null) {
            cbPriorite.setValue("Moyenne");
            info.append("Priorite proposee: Moyenne.");
        }

        lblAutoFillInfo.setText(info.toString().trim());
        refreshSmartAssistance();
    }

    private void refreshSmartAssistance() {
        if (lblCompatibilityInfo == null) {
            return;
        }

        String patientBloodType = cbTypeSang.getValue();
        int quantite = parsePositiveQuantity();
        if (patientBloodType == null || quantite <= 0) {
            lblCompatibilityInfo.setText("Selectionnez le type sanguin et la quantite pour voir les disponibilites.");
            return;
        }

        List<String> compatibleTypes = BloodCompatibilityService.compatibleDonorTypesFor(patientBloodType);
        if (compatibleTypes.isEmpty()) {
            lblCompatibilityInfo.setText("Type sanguin invalide.");
            return;
        }

        try {
            Integer selectedBanqueId = cbBanque.getValue() == null ? null : banqueIdMap.get(cbBanque.getValue());
            if (selectedBanqueId != null) {
                int exactStock = stockService.getAvailableQuantityForOrg(selectedBanqueId, "banque", patientBloodType);
                Stock suggestion = stockService.findBestCompatibleStockForBank(selectedBanqueId, patientBloodType, quantite);
                if (exactStock >= quantite) {
                    lblCompatibilityInfo.setText("Stock disponible pour " + patientBloodType + " dans " + cbBanque.getValue()
                            + ". Compatibles: " + BloodCompatibilityService.formatCompatibleTypes(patientBloodType));
                } else if (suggestion != null) {
                    lblCompatibilityInfo.setText(patientBloodType + " est insuffisant dans cette banque. Alternative compatible proposee: "
                            + suggestion.getTypeSang() + " (" + suggestion.getQuantite() + " ml disponibles).");
                } else {
                    lblCompatibilityInfo.setText("Stock insuffisant. Compatibles pour " + patientBloodType + ": "
                            + BloodCompatibilityService.formatCompatibleTypes(patientBloodType));
                }
                return;
            }

            Stock suggestion = stockService.findBestCompatibleStockForAnyBank(patientBloodType, quantite, banqueIdMap.values());
            if (suggestion != null) {
                String bankName = findBankNameById(suggestion.getTypeOrgid());
                if (bankName != null && cbBanque.getValue() == null) {
                    cbBanque.setValue(bankName);
                }
                lblCompatibilityInfo.setText("Banque proposee: " + (bankName == null ? "-" : bankName)
                        + " avec " + suggestion.getTypeSang() + " (" + suggestion.getQuantite() + " ml disponibles).");
            } else {
                lblCompatibilityInfo.setText("Aucune banque n'a assez de stock. Compatibles: "
                        + BloodCompatibilityService.formatCompatibleTypes(patientBloodType));
            }
        } catch (SQLException e) {
            lblCompatibilityInfo.setText("Impossible de verifier le stock: " + e.getMessage());
        }
    }

    private Stock resolveStockForRequest(String patientBloodType, int quantite, int banqueId) throws SQLException {
        Stock suggestion = stockService.findBestCompatibleStockForBank(banqueId, patientBloodType, quantite);
        if (suggestion == null) {
            lblErrorTypeSang.setText("Stock insuffisant. Compatibles: "
                    + BloodCompatibilityService.formatCompatibleTypes(patientBloodType));
            return null;
        }

        String requested = BloodCompatibilityService.normalizeBloodType(patientBloodType);
        String selected = BloodCompatibilityService.normalizeBloodType(suggestion.getTypeSang());
        if (!requested.equals(selected)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Alternative compatible");
            alert.setHeaderText(patientBloodType + " n'est pas disponible en quantite suffisante.");
            alert.setContentText("Utiliser " + suggestion.getTypeSang() + " a la place ?\nCompatibles: "
                    + BloodCompatibilityService.formatCompatibleTypes(patientBloodType));
            themeManager.styleDialog(alert.getDialogPane());
            if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return null;
            }
            cbTypeSang.setValue(suggestion.getTypeSang());
        }

        return suggestion;
    }

    private int parsePositiveQuantity() {
        try {
            int quantite = Integer.parseInt(tfQuantite.getText() == null ? "" : tfQuantite.getText().trim());
            return quantite > 0 ? quantite : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String findBankNameById(int banqueId) {
        for (Map.Entry<String, Integer> entry : banqueIdMap.entrySet()) {
            if (entry.getValue() == banqueId) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void notifyAdmins(Commande commande, User clientUser, String clientName, String banqueName, String requestedTypeSang) {
        try {
            for (User admin : userService.findAdmins()) {
                if (admin.getEmail() == null || admin.getEmail().isBlank()) {
                    continue;
                }
                emailService.sendAdminNewCommandeNotification(
                        admin.getEmail(),
                        buildClientName(admin),
                        clientName,
                        commande,
                        banqueName,
                        requestedTypeSang
                );
            }
        } catch (SQLException e) {
            lblPaymentInfo.setText("Notification admin non envoyee: " + e.getMessage());
        }
    }

    private boolean launchStripeCheckout(Commande commande, User user, String clientName, String banqueName) {
        if (!stripeCheckoutService.isConfigured()) {
            lblPaymentInfo.setText("Stripe non configure: ajoutez BLOODLINK_STRIPE_CHECKOUT_ENDPOINT.");
            return false;
        }

        try {
            String checkoutUrl = stripeCheckoutService.createCheckoutSessionUrl(commande, user, clientName, banqueName);
            StripeCheckoutService.CheckoutResult result = stripeCheckoutService.openCheckoutUrlInApp(
                    checkoutUrl,
                    tfQuantite.getScene() == null ? null : tfQuantite.getScene().getWindow()
            );
            if (result == StripeCheckoutService.CheckoutResult.SUCCESS) {
                lblPaymentInfo.setText("Paiement Stripe confirme.");
                return true;
            }
            if (result == StripeCheckoutService.CheckoutResult.CANCELLED) {
                lblPaymentInfo.setText("Paiement Stripe annule.");
            } else {
                lblPaymentInfo.setText("Paiement Stripe ferme avant confirmation.");
            }
            return false;
        } catch (Exception e) {
            lblPaymentInfo.setText("Paiement Stripe non lance: " + e.getMessage());
            return false;
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
