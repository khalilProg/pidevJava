package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import tn.esprit.entities.Commande;
import tn.esprit.services.CommandeService;
import tn.esprit.tools.ThemeManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AfficherCommandesController implements Initializable {

    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbStatutFiltre;
    @FXML private ComboBox<String> cbPrioriteFiltre;
    @FXML private Label lblCount;
    @FXML private Button btnThemeToggle;
    
    @FXML private TableView<Commande> tableCommandes;
    @FXML private TableColumn<Commande, Integer> colReference;
    @FXML private TableColumn<Commande, String> colTypeSang;
    @FXML private TableColumn<Commande, Integer> colQuantite;
    @FXML private TableColumn<Commande, String> colPriorite;
    @FXML private TableColumn<Commande, String> colStatut;
    @FXML private TableColumn<Commande, Commande> colActions;

    private final CommandeService commandeService = new CommandeService();
    private final ThemeManager themeManager = ThemeManager.getInstance();
    private ObservableList<Commande> commandesList;
    private FilteredList<Commande> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        setupTableColumns();
        loadDonnees();

        // Apply theme
        javafx.application.Platform.runLater(() -> {
            themeManager.applyTheme(tfSearch.getScene());
            themeManager.updateToggleButton(btnThemeToggle);
            refreshInlineStyles();
        });

        // Add animations
        tn.esprit.tools.AnimationUtils.animateNode(tfSearch, 100);
        tn.esprit.tools.AnimationUtils.animateNode(cbStatutFiltre, 200);
        tn.esprit.tools.AnimationUtils.animateNode(cbPrioriteFiltre, 300);
        tn.esprit.tools.AnimationUtils.animateNode(tableCommandes, 400);
    }

    @FXML
    private void handleThemeToggle() {
        themeManager.toggleTheme(btnThemeToggle.getScene());
        themeManager.updateToggleButton(btnThemeToggle);
        refreshInlineStyles();
        tableCommandes.refresh();
    }

    private void refreshInlineStyles() {
        lblCount.setStyle(themeManager.getFrontCountStyle());
    }

    private void setupFilters() {
        cbStatutFiltre.setItems(FXCollections.observableArrayList("Tous", "En attente", "Validée", "Annulée"));
        cbStatutFiltre.setValue("Tous");

        cbPrioriteFiltre.setItems(FXCollections.observableArrayList("Toutes", "Haute", "Moyenne", "Basse"));
        cbPrioriteFiltre.setValue("Toutes");

        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbStatutFiltre.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbPrioriteFiltre.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void setupTableColumns() {
        colReference.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colReference.setCellFactory(column -> new TableCell<Commande, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lbl = new Label("#" + item);
                    lbl.setStyle(themeManager.getTableBoldStyle());
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colQuantite.setCellFactory(column -> new TableCell<Commande, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lbl = new Label(String.valueOf(item));
                    lbl.setStyle(themeManager.getTableBoldStyle());
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        colTypeSang.setCellValueFactory(new PropertyValueFactory<>("typeSang"));
        colTypeSang.setCellFactory(column -> new TableCell<Commande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().addAll("badge", "badge-red");
                    setGraphic(lbl);
                }
            }
        });

        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colPriorite.setCellFactory(column -> new TableCell<Commande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.getStyleClass().addAll("badge", "badge-yellow");
                    if ("Haute".equals(item)) {
                        lbl.getStyleClass().add("badge-red");
                    }
                    setGraphic(lbl);
                }
            }
        });

        colStatut.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatut.setCellFactory(column -> new TableCell<Commande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label("🕘 " + item);
                    lbl.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold; -fx-font-size: 12px;");
                    setGraphic(lbl);
                }
            }
        });

        colActions.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        colActions.setCellFactory(param -> new TableCell<Commande, Commande>() {
            private final Button btnPdf = new Button("📄 Reçu PDF");

            {
                btnPdf.getStyleClass().add("submit-button");
                btnPdf.setStyle("-fx-padding: 5 10 5 10; -fx-font-size: 11px; -fx-cursor: hand;");
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnPdf);
                btnPdf.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    generatePDFReceipt(commande);
                });
            }

            @Override
            protected void updateItem(Commande commande, boolean empty) {
                super.updateItem(commande, empty);
                if (empty || commande == null) {
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(8);
                    hBox.setAlignment(javafx.geometry.Pos.CENTER);
                    hBox.getChildren().addAll(btnPdf);
                    setGraphic(hBox);
                }
            }
        });
    }

    private void loadDonnees() {
        try {
            List<Commande> list = commandeService.recuperer();
            commandesList = FXCollections.observableArrayList(list);
            filteredData = new FilteredList<>(commandesList, p -> true);
            tableCommandes.setItems(filteredData);
            updateCount();
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors du chargement des commandes: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (filteredData == null) return;
        
        String searchText = tfSearch.getText() != null ? tfSearch.getText().toLowerCase() : "";
        String statut = cbStatutFiltre.getValue();
        String priorite = cbPrioriteFiltre.getValue();

        filteredData.setPredicate(commande -> {
            // Check Priorité
            if (priorite != null && !priorite.equals("Toutes") && !commande.getPriorite().equalsIgnoreCase(priorite)) {
                return false;
            }
            // Check Statut
            if (statut != null && !statut.equals("Tous") && !commande.getStatus().equalsIgnoreCase(statut)) {
                return false;
            }
            // Check Search Text
            if (searchText.isEmpty()) {
                return true;
            }
            String bloodType = commande.getTypeSang() != null ? commande.getTypeSang().toLowerCase() : "";
            String ref = String.valueOf(commande.getReference());
            return bloodType.contains(searchText) || ref.contains(searchText);
        });
        
        updateCount();
    }

    private void updateCount() {
        if (filteredData != null) {
            lblCount.setText(filteredData.size() + " commande(s)");
        }
    }

    private void generatePDFReceipt(Commande commande) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le reçu PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Recu_Commande_" + commande.getReference() + ".pdf");

        Stage stage = (Stage) tableCommandes.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Colors matches HTML css
                BaseColor primaryRed = new BaseColor(217, 83, 79); // #d9534f
                BaseColor bgLight = new BaseColor(248, 249, 250);  // #f8f9fa
                BaseColor borderColor = new BaseColor(221, 221, 221); // #ddd

                // Fonts matches HTML styling
                Font logoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, primaryRed);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
                Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.DARK_GRAY);
                Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
                Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);

                // --- HEADER ---
                Paragraph logo = new Paragraph("BLOODLINK", logoFont);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);

                Paragraph subtitle = new Paragraph("Récapitulatif de Commande", normalFont);
                subtitle.setAlignment(Element.ALIGN_CENTER);
                subtitle.setSpacingAfter(20);
                document.add(subtitle);

                // Add a border bottom (LineSeparator)
                LineSeparator ls = new LineSeparator();
                ls.setLineColor(borderColor);
                document.add(new Chunk(ls));
                document.add(new Paragraph(" ")); // Spacing

                // --- 1. DETAILS DE LA COMMANDE ---
                document.add(createSectionTitle("Détails de la Commande #" + commande.getReference(), boldFont, bgLight, primaryRed));
                
                PdfPTable detailsTable = new PdfPTable(2);
                detailsTable.setWidthPercentage(100);
                try { detailsTable.setWidths(new float[]{3, 7}); } catch (Exception e) {}
                
                addRow(detailsTable, "Référence", "#" + commande.getReference(), boldFont, normalFont, bgLight, borderColor);
                String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                addRow(detailsTable, "Date", currentDate, boldFont, normalFont, bgLight, borderColor);
                addRow(detailsTable, "Client ID", String.valueOf(commande.getClientId()), boldFont, normalFont, bgLight, borderColor);
                document.add(detailsTable);

                // --- 2. PRODUITS ---
                document.add(createSectionTitle("Produits", boldFont, bgLight, primaryRed));
                
                PdfPTable productsTable = new PdfPTable(4);
                productsTable.setWidthPercentage(100);
                
                // Headings
                String[] productHeaders = {"Type Sang", "Quantité", "Priorité", "Statut"};
                for (String header : productHeaders) {
                    PdfPCell headingCell = new PdfPCell(new Phrase(header, boldFont));
                    headingCell.setBackgroundColor(bgLight);
                    headingCell.setBorderColor(borderColor);
                    headingCell.setPadding(8f);
                    productsTable.addCell(headingCell);
                }
                
                // Row
                PdfPCell bloodTypeCell = new PdfPCell(new Phrase(commande.getTypeSang(), boldFont));
                bloodTypeCell.setBorderColor(borderColor); bloodTypeCell.setPadding(8f);
                productsTable.addCell(bloodTypeCell);
                
                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(commande.getQuantite()), normalFont));
                qtyCell.setBorderColor(borderColor); qtyCell.setPadding(8f);
                productsTable.addCell(qtyCell);
                
                PdfPCell priCell = new PdfPCell(new Phrase(commande.getPriorite(), normalFont));
                priCell.setBorderColor(borderColor); priCell.setPadding(8f);
                productsTable.addCell(priCell);
                
                PdfPCell statusCell = new PdfPCell(new Phrase(commande.getStatus(), normalFont));
                statusCell.setBorderColor(borderColor); statusCell.setPadding(8f);
                productsTable.addCell(statusCell);

                document.add(productsTable);

                // --- 3. BANQUE ASSOCIÉE ---
                document.add(createSectionTitle("Banque Associée", boldFont, bgLight, primaryRed));
                
                PdfPTable bankTable = new PdfPTable(2);
                bankTable.setWidthPercentage(100);
                try { bankTable.setWidths(new float[]{3, 7}); } catch (Exception e) {}
                addRow(bankTable, "Identifiant Banque", String.valueOf(commande.getBanqueId()), boldFont, normalFont, bgLight, borderColor);
                document.add(bankTable);

                // --- FOOTER ---
                document.add(new Paragraph(" ")); // Spacing
                document.add(new Chunk(ls));
                Paragraph footer = new Paragraph("Ce document est généré automatiquement par la plateforme BloodLink.", smallFont);
                footer.setAlignment(Element.ALIGN_CENTER);
                footer.setSpacingBefore(20);
                document.add(footer);

                document.close();
                showSuccess("PDF généré avec succès !");

            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur lors de la génération du PDF.");
            }
        }
    }

    private PdfPTable createSectionTitle(String titleText, Font font, BaseColor bgColor, BaseColor borderColor) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(15);
        table.setSpacingAfter(10);
        PdfPCell cell = new PdfPCell(new Phrase(titleText, font));
        cell.setBackgroundColor(bgColor);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new BaseColor(255, 255, 255)); // hide 3 sides
        cell.setBorderWidthLeft(5f);
        cell.setBorderColorLeft(borderColor);
        cell.setPadding(10f);
        table.addCell(cell);
        return table;
    }

    private void addRow(PdfPTable table, String col1, String col2, Font boldFont, Font normalFont, BaseColor bgLight, BaseColor borderColor) {
        PdfPCell c1 = new PdfPCell(new Phrase(col1, boldFont));
        c1.setBackgroundColor(bgLight);
        c1.setBorderColor(borderColor);
        c1.setPadding(8f);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(col2, normalFont));
        c2.setBorderColor(borderColor);
        c2.setPadding(8f);
        table.addCell(c2);
    }


    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        
        DialogPane dialogPane = alert.getDialogPane();
        themeManager.styleDialog(dialogPane);
        
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        
        DialogPane dialogPane = alert.getDialogPane();
        themeManager.styleDialog(dialogPane);
        
        alert.showAndWait();
    }

    @FXML
    private void handleNouvelleCommande() {
        switchScene("/AjoutCommande.fxml", "BLOODLINK — Créer une Commande");
    }

    private void switchScene(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) tfSearch.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            System.out.println("❌ Erreur de navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
