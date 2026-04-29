package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.entities.Demande;
import tn.esprit.entities.Banque;
import tn.esprit.services.DemandeService;
import tn.esprit.services.BanqueService;
import tn.esprit.services.GeminiService;
import tn.esprit.services.StockService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;

public class AgentBanqueDemandeController {

    @FXML private VBox cardsContainer;
    @FXML private Label lblDemandeCount;
    @FXML private VBox emptyStateContainer;
    @FXML private VBox tableContainer;
    @FXML private Button predictButton;
    // Filtres et Recherche
    @FXML private TextField searchField;
    @FXML private ComboBox<String> urgencyFilter;
    @FXML private ComboBox<String> statusFilter;

    private final DemandeService service = new DemandeService();
    private final BanqueService banqueService = new BanqueService();

    private final ObservableList<Demande> masterData = FXCollections.observableArrayList();
    private FilteredList<Demande> filteredDemandes;
    private final Map<Integer, String> bankNamesMap = new HashMap<>();

    @FXML
    public void initialize() {
        // 1. Initialisation des filtres (ComboBox)
        urgencyFilter.getItems().addAll("Toutes urgences", "URGENT", "NORMAL");
        statusFilter.getItems().addAll("Tous les statuts", "En attente", "Confirme", "Annulée");
        urgencyFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();

        // 2. Chargement des noms des banques
        loadBankNames();

        // 3. Chargement initial des données depuis la DB
        loadData();

        // 4. Liaison de la liste filtrée à la source de données
        filteredDemandes = new FilteredList<>(masterData, p -> true);

        // 5. Listeners pour la recherche et les filtres
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilters());
        urgencyFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateFilters());

        // 6. Premier rendu visuel
        refreshView();
    }

    private void loadBankNames() {
        try {
            List<Banque> banques = banqueService.recuperer();
            for (Banque b : banques) {
                bankNamesMap.put(b.getId(), b.getNom());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        try {
            masterData.setAll(service.recuperer());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateFilters() {
        String searchText = (searchField.getText() == null) ? "" : searchField.getText().toLowerCase().trim();
        String urgency = urgencyFilter.getValue();
        String status = statusFilter.getValue();

        filteredDemandes.setPredicate(demande -> {
            // Vérification du texte (Type de sang ou Statut)
            boolean matchesSearch = searchText.isEmpty() ||
                (demande.getTypeSang() != null && demande.getTypeSang().toLowerCase().contains(searchText)) ||
                (demande.getStatus() != null && demande.getStatus().toLowerCase().contains(searchText));

            // Vérification de l'urgence
            boolean matchesUrgency = (urgency == null || urgency.equals("Toutes urgences")) ||
                (demande.getUrgence() != null && demande.getUrgence().equalsIgnoreCase(urgency));

            // Vérification du statut
            boolean matchesStatus = (status == null || status.equals("Tous les statuts")) ||
                (demande.getStatus() != null && demande.getStatus().equalsIgnoreCase(status));

            return matchesSearch && matchesUrgency && matchesStatus;
        });

        refreshView();
    }

    private void refreshView() {
        cardsContainer.getChildren().clear();

        if (filteredDemandes.isEmpty()) {
            tableContainer.setVisible(false);
            tableContainer.setManaged(false);
            emptyStateContainer.setVisible(true);
            emptyStateContainer.setManaged(true);
        } else {
            emptyStateContainer.setVisible(false);
            emptyStateContainer.setManaged(false);
            tableContainer.setVisible(true);
            tableContainer.setManaged(true);

            for (Demande d : filteredDemandes) {
                cardsContainer.getChildren().add(createDemandeCard(d));
            }

            if (lblDemandeCount != null) {
                lblDemandeCount.setText(filteredDemandes.size() + " demande(s)");
            }
        }
    }

    private javafx.scene.Node createDemandeCard(Demande d) {
        HBox card = new HBox(25);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #111111; -fx-border-color: #222222; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15 25;");

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #444444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15 25; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #111111; -fx-border-color: #222222; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15 25;"));

        // Col 1 : ID
        Label lblId = new Label("#" + d.getId());
        lblId.setStyle("-fx-background-color: #222222; -fx-text-fill: #aaaaaa; -fx-padding: 5 12; -fx-background-radius: 6; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblId.setMinWidth(60);

        // Col 2 : Banque
        HBox bankBox = new HBox(8);
        bankBox.setAlignment(Pos.CENTER_LEFT);
        bankBox.setPrefWidth(220);
        SVGPath iconBank = new SVGPath();
        iconBank.setContent("M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z");
        iconBank.setFill(Color.web("#555555"));
        iconBank.setScaleX(0.8); iconBank.setScaleY(0.8);
        Label lblBank = new Label(bankNamesMap.getOrDefault(d.getBanque(), "Banque ID: " + d.getBanque()));
        lblBank.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");
        bankBox.getChildren().addAll(iconBank, lblBank);

        // Col 3 : Type
        Label lblType = new Label(d.getTypeSang() != null ? d.getTypeSang() : "N/A");
        lblType.setStyle("-fx-border-color: #E53935; -fx-border-radius: 12; -fx-text-fill: #E53935; -fx-font-weight: bold; -fx-padding: 4 12; -fx-font-size: 13px;");
        lblType.setPrefWidth(80); lblType.setAlignment(Pos.CENTER);

        // Col 4 : Quantité
        Label lblQty = new Label(d.getQuantite() + " ml");
        lblQty.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        lblQty.setPrefWidth(80);

        // Col 5 : Statut
        String statusText = (d.getStatus() != null) ? d.getStatus().toUpperCase() : "INCONNU";
        Label lblStatusBadge = new Label("⌚ " + statusText);
        String styleStatus = "-fx-padding: 6 15; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold; ";

        if (statusText.toLowerCase().contains("confirm")) {
            styleStatus += "-fx-background-color: rgba(40, 167, 69, 0.1); -fx-text-fill: #28a745;";
            lblStatusBadge.setText("✔ " + statusText);
        } else if (statusText.toLowerCase().contains("attente")) {
            styleStatus += "-fx-background-color: rgba(255, 193, 7, 0.1); -fx-text-fill: #ffc107;";
        } else {
            styleStatus += "-fx-background-color: #333333; -fx-text-fill: white;";
        }
        lblStatusBadge.setStyle(styleStatus);
        lblStatusBadge.setPrefWidth(140); lblStatusBadge.setAlignment(Pos.CENTER);

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Actions
        HBox actionBox = new HBox(12);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnView = createIconButton("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z", "#aaaaaa", "#444444", "#222222");
        Button btnEdit = createIconButton("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a.996.996 0 0 0 0-1.41l-2.34-2.34a.996.996 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z", "#ffc107", "#ffc107", "transparent");
        Button btnDelete = createIconButton("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z", "#E53935", "#442a2a", "#2a1111");

        btnView.setOnAction(e -> openViewDetails(d));
        btnEdit.setOnAction(e -> openEditForm(d));
        btnDelete.setOnAction(e -> {
            try {
                service.supprimer(d);
                masterData.remove(d);
                refreshView();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        actionBox.getChildren().addAll(btnView, btnEdit, btnDelete);
        card.getChildren().addAll(lblId, bankBox, lblType, lblQty, lblStatusBadge, spacer, actionBox);

        return card;
    }

    private Button createIconButton(String svgPath, String iconColor, String borderColor, String bgColor) {
        Button btn = new Button();
        SVGPath path = new SVGPath();
        path.setContent(svgPath);
        path.setFill(Color.web(iconColor));
        path.setScaleX(0.7); path.setScaleY(0.7);
        btn.setGraphic(path);
        btn.setStyle(String.format("-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 15; -fx-background-radius: 15; -fx-min-width: 32; -fx-min-height: 32; -fx-cursor: hand;", bgColor, borderColor));
        return btn;
    }

    private void openEditForm(Demande selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgentBanqueEditDemande.fxml"));
            Parent root = loader.load();
            AgentBanqueEditDemandeController controller = loader.getController();
            controller.initData(selected);
            AgentBanqueBaseController.getInstance().loadView(root);
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void openViewDetails(Demande selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgentBanqueViewDemande.fxml"));
            Parent root = loader.load();
            AgentBanqueViewDemandeController controller = loader.getController();
            controller.initData(selected);
            AgentBanqueBaseController.getInstance().loadView(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void openNewDemande() {
        AgentBanqueBaseController.getInstance().loadView("/AgentBanqueAddDemande.fxml");
    }

    @FXML public void runAIPrediction() {
        // 1. Désactiver le bouton pendant le calcul
        predictButton.setDisable(true);

        // 2. Récupérer les données réelles de la base de données
        StockService stockService = new StockService();
        String tempStock = "Stock vide";
        try {
            Map<String, Integer> stockStats = stockService.getStockStats();
            if (!stockStats.isEmpty()) {
                StringBuilder sbStock = new StringBuilder();
                for (Map.Entry<String, Integer> entry : stockStats.entrySet()) {
                    sbStock.append(entry.getKey()).append(": ").append(entry.getValue()).append("ml, ");
                }
                tempStock = sbStock.substring(0, sbStock.length() - 2);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            tempStock = "Erreur de récupération du stock";
        }

        StringBuilder sbDemandes = new StringBuilder();
        for (Demande d : masterData) {
            sbDemandes.append("- Demande ID ").append(d.getId())
                      .append(" : ").append(d.getQuantite()).append("ml de type ").append(d.getTypeSang() != null ? d.getTypeSang() : "inconnu")
                      .append(" (Urgence : ").append(d.getUrgence() != null ? d.getUrgence() : "N/A")
                      .append(", Statut : ").append(d.getStatus() != null ? d.getStatus() : "N/A").append(")\n");
        }
        String tempDemandes = sbDemandes.length() > 0 ? sbDemandes.toString() : "Aucune demande en cours.";

        final String finalEtatStock = tempStock;
        final String finalHistorique = tempDemandes;

        // 3. Lancer la tâche en arrière-plan
        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override
            protected String call() throws Exception {
                return new GeminiService().predireBesoins(finalEtatStock, finalHistorique);
            }
        };

        // 4. Gérer le résultat
        task.setOnSucceeded(e -> {
            String analyse = task.getValue();
            afficherResultatIA(analyse);
            predictButton.setDisable(false);
        });

        task.setOnFailed(e -> {
            System.err.println("Erreur IA : " + task.getException().getMessage());
            predictButton.setDisable(false);
        });

        new Thread(task).start();
    }

    private void afficherResultatIA(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AIResultModal.fxml"));
            Parent root = loader.load();

            AIResultController controller = loader.getController();
            controller.setResultText(message);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT); // Important pour le look custom

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);

            // Centrer la modal par rapport à la fenêtre principale
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            // Fallback en cas d'erreur de chargement du FXML
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
            alert.show();
        }
    }

    @FXML public void exportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(searchField.getScene().getWindow());

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Couleurs du thème
                BaseColor primaryColor = new BaseColor(229, 57, 53); // #E53935 (Rouge vif)
                BaseColor bgColor = new BaseColor(34, 34, 34); // #222222 (Gris foncé/Noir)
                BaseColor textColor = new BaseColor(255, 255, 255); // Blanc
                BaseColor rowColor = new BaseColor(245, 245, 245); // Gris très clair pour les lignes
                BaseColor darkRowColor = new BaseColor(230, 230, 230); // Gris clair altérné

                // Polices
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, primaryColor);
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, textColor);
                Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

                // Titre
                Paragraph title = new Paragraph("Rapport des Demandes de Sang", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Tableau
                PdfPTable pdfTable = new PdfPTable(5);
                pdfTable.setWidthPercentage(100);
                pdfTable.setSpacingBefore(10f);
                pdfTable.setSpacingAfter(10f);

                // Définition des largeurs des colonnes
                float[] columnWidths = {1f, 3f, 2f, 2f, 2f};
                pdfTable.setWidths(columnWidths);

                // En-têtes du tableau
                String[] headers = {"ID", "Banque", "Type Sang", "Quantité", "Statut"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setBackgroundColor(bgColor);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(10);
                    cell.setBorderColor(BaseColor.WHITE);
                    pdfTable.addCell(cell);
                }

                // Données du tableau
                boolean isAlternate = false;
                for (Demande d : filteredDemandes) {
                    BaseColor currentRowColor = isAlternate ? darkRowColor : rowColor;

                    PdfPCell idCell = new PdfPCell(new Phrase(String.valueOf(d.getId()), cellFont));
                    PdfPCell banqueCell = new PdfPCell(new Phrase(bankNamesMap.getOrDefault(d.getBanque(), String.valueOf(d.getBanque())), cellFont));
                    PdfPCell typeCell = new PdfPCell(new Phrase(d.getTypeSang() != null ? d.getTypeSang() : "N/A", cellFont));
                    PdfPCell qteCell = new PdfPCell(new Phrase(d.getQuantite() + " ml", cellFont));
                    PdfPCell statusCell = new PdfPCell(new Phrase(d.getStatus() != null ? d.getStatus() : "INCONNU", cellFont));

                    PdfPCell[] cells = {idCell, banqueCell, typeCell, qteCell, statusCell};
                    for (PdfPCell cell : cells) {
                        cell.setBackgroundColor(currentRowColor);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setPadding(8);
                        cell.setBorderColor(BaseColor.WHITE);
                        pdfTable.addCell(cell);
                    }
                    isAlternate = !isAlternate;
                }

                document.add(pdfTable);
                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Exportation PDF réussie !");
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de l'exportation PDF : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML public void showMyStock() {
        StockService stockService = new StockService();
        try {
            Map<String, Integer> stockStats = stockService.getStockStats();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            VBox root = new VBox(20);
            root.setStyle("-fx-background-color: #0d0d0d; -fx-border-color: #222222; -fx-border-width: 2; -fx-background-radius: 15; -fx-border-radius: 15; -fx-padding: 30;");
            root.setPrefWidth(450);

            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);

            StackPane iconPane = new StackPane();
            iconPane.setStyle("-fx-background-color: #2a1111; -fx-background-radius: 10; -fx-padding: 10;");
            SVGPath icon = new SVGPath();
            icon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z");
            icon.setFill(Color.web("#E53935"));
            icon.setScaleX(1.2); icon.setScaleY(1.2);
            iconPane.getChildren().add(icon);

            VBox titleBox = new VBox();
            Label subtitle = new Label("INVENTAIRE");
            subtitle.setStyle("-fx-text-fill: #E53935; -fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 2px;");
            Label title = new Label("Mon Stock de Sang");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            titleBox.getChildren().addAll(subtitle, title);

            header.getChildren().addAll(iconPane, titleBox);

            VBox content = new VBox(10);
            if (stockStats.isEmpty()) {
                Label emptyLabel = new Label("Votre stock est actuellement vide.");
                emptyLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
                content.getChildren().add(emptyLabel);
            } else {
                for (Map.Entry<String, Integer> entry : stockStats.entrySet()) {
                    HBox row = new HBox();
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle("-fx-background-color: #111111; -fx-border-color: #222222; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15;");

                    Label typeLabel = new Label(entry.getKey());
                    typeLabel.setStyle("-fx-text-fill: #E53935; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-color: #E53935; -fx-border-radius: 12; -fx-padding: 4 12;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label qteLabel = new Label(entry.getValue() + " ml");
                    qteLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

                    row.getChildren().addAll(typeLabel, spacer, qteLabel);
                    content.getChildren().add(row);
                }
            }

            HBox footer = new HBox();
            footer.setAlignment(Pos.CENTER_RIGHT);
            Button closeBtn = new Button("Fermer");
            closeBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 8 20; -fx-cursor: hand;");
            closeBtn.setOnAction(e -> stage.close());
            footer.getChildren().add(closeBtn);

            root.getChildren().addAll(header, content, footer);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
