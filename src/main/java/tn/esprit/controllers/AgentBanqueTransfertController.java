package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Cell;
import tn.esprit.entities.Stock;
import tn.esprit.entities.Transfert;
import tn.esprit.services.StockService;
import tn.esprit.services.TransfertService;
import javafx.collections.transformation.FilteredList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.scene.chart.*;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AgentBanqueTransfertController {

    @FXML private VBox cardsContainer;
    @FXML private Label lblCount;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> comboStatusFilter;

    private final TransfertService service = new TransfertService();
    private final ObservableList<Transfert> masterData = FXCollections.observableArrayList();
    private FilteredList<Transfert> filteredData;

    @FXML
    public void initialize() {
        // Initialize FilteredList
        filteredData = new FilteredList<>(masterData, p -> true);

        // Setup ComboBox
        comboStatusFilter.getItems().addAll("Tous les statuts", "En cours", "Reçu", "Annulé");
        comboStatusFilter.getSelectionModel().selectFirst();

        // Add listeners
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> updateFilters());
        comboStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilters());

        loadData();
    }

    private void updateFilters() {
        String searchText = (txtSearch.getText() == null) ? "" : txtSearch.getText().toLowerCase().trim();
        String statusFilter = comboStatusFilter.getValue();

        filteredData.setPredicate(transfert -> {
            // Filter by Status
            boolean matchesStatus = (statusFilter == null || statusFilter.equals("Tous les statuts")) ||
                    (transfert.getStatus() != null && transfert.getStatus().equalsIgnoreCase(statusFilter));

            // Filter by Search Text (Destination or Status)
            boolean matchesSearch = searchText.isEmpty() ||
                    (transfert.getToOrg() != null && transfert.getToOrg().toLowerCase().contains(searchText)) ||
                    (transfert.getStatus() != null && transfert.getStatus().toLowerCase().contains(searchText)) ||
                    (String.valueOf(transfert.getId()).contains(searchText));

            return matchesStatus && matchesSearch;
        });

        renderCards();
    }

    private void renderCards() {
        cardsContainer.getChildren().clear();
        for (Transfert t : filteredData) {
            cardsContainer.getChildren().add(createTransfertCard(t));
        }
        if (lblCount != null) {
            lblCount.setText(filteredData.size() + " transfert(s)");
        }
    }

    /**
     * Charge les données depuis la base et rafraîchit l'affichage des cartes.
     */
    public void loadData() {
        try {
            masterData.setAll(service.recuperer());
            renderCards();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Crée une carte visuelle pour un transfert spécifique.
     */
    private javafx.scene.Node createTransfertCard(Transfert t) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #111111; -fx-border-color: #222222; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15 25;");

        // Effet Hover
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #444444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15 25; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #111111; -fx-border-color: #222222; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15 25;"));

        // 1. ID Pill
        Label lblId = new Label("#" + t.getId());
        lblId.setStyle("-fx-background-color: #222222; -fx-text-fill: #aaaaaa; -fx-padding: 5 12; -fx-background-radius: 6; -fx-font-size: 13px; -fx-font-weight: bold;");

        // 2. Lien Demande
        int dId = (t.getDemande() != null) ? t.getDemande().getId() : 0;
        Label lblDemande = new Label("DEM-" + dId);
        lblDemande.setStyle("-fx-background-color: transparent; -fx-border-color: #444444; -fx-border-radius: 12; -fx-text-fill: #cccccc; -fx-font-weight: bold; -fx-padding: 4 12; -fx-font-size: 11px;");

        // 3. Flux (Origine -> Destination)
        HBox flowMap = new HBox(8);
        flowMap.setAlignment(Pos.CENTER_LEFT);
        flowMap.setPrefWidth(250);
        Label lblOrigine = new Label(t.getFromOrg() != null ? t.getFromOrg().toUpperCase() : "N/A");
        lblOrigine.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px; -fx-font-weight: bold;");
        SVGPath arrow = new SVGPath();
        arrow.setContent("M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z");
        arrow.setFill(Color.web("#E53935"));
        arrow.setScaleX(0.7); arrow.setScaleY(0.7);
        Label lblDest = new Label(t.getToOrg() != null ? t.getToOrg().toUpperCase() : "N/A");
        lblDest.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        flowMap.getChildren().addAll(lblOrigine, arrow, lblDest);

        // 4. Quantité & Date
        Label lblQty = new Label(t.getQuantite() + " ml");
        lblQty.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        lblQty.setPrefWidth(70);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Label lblDate = new Label(t.getDateEnvoie() != null ? t.getDateEnvoie().format(formatter) : "--/--/----");
        lblDate.setStyle("-fx-text-fill: #777777; -fx-font-size: 13px;");
        lblDate.setPrefWidth(90);

        // 5. Statut Badge
        String statusText = t.getStatus() != null ? t.getStatus().toUpperCase() : "EN COURS";
        Label lblStatus = new Label();
        String styleStatus = "-fx-padding: 6 15; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold; ";

        boolean isPending = statusText.contains("COURS") || statusText.contains("ATTENTE");

        if (statusText.contains("REÇU") || statusText.contains("RECU")) {
            styleStatus += "-fx-background-color: rgba(40, 167, 69, 0.1); -fx-text-fill: #28a745;";
            lblStatus.setText("✔ REÇU");
        } else if (isPending) {
            styleStatus += "-fx-background-color: rgba(255, 193, 7, 0.1); -fx-text-fill: #ffc107;";
            lblStatus.setText("🚚 EN COURS");
        } else {
            styleStatus += "-fx-background-color: #333333; -fx-text-fill: white;";
            lblStatus.setText(statusText);
        }
        lblStatus.setStyle(styleStatus);
        lblStatus.setPrefWidth(110);
        lblStatus.setAlignment(Pos.CENTER);

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 6. Zone d'Actions
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        // AJOUT DES ICÔNES DE RÉCEPTION (Si en attente)
        if (isPending) {
            // Bouton de validation (Check)
            Button btnConfirm = createSmallIconButton("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z", "#28a745", "Confirmer la réception");
            btnConfirm.setOnAction(e -> handleUpdateStatus(t, "REÇU"));

            // Bouton d'annulation (Cross)
            Button btnCancel = createSmallIconButton("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z", "#E53935", "Annuler le transfert");
            btnCancel.setOnAction(e -> handleUpdateStatus(t, "ANNULÉ"));

            actionBox.getChildren().addAll(btnConfirm, btnCancel);
        }

        // Bouton Voir Détails (Toujours présent)
        Button btnView = createSmallIconButton("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z", "#aaaaaa", "Voir les détails");
        btnView.setOnAction(e -> openViewDetails(t));

        actionBox.getChildren().add(btnView);

        card.getChildren().addAll(lblId, lblDemande, flowMap, lblQty, lblDate, lblStatus, spacer, actionBox);
        return card;
    }

    /**
     * Gère la mise à jour du statut dans la DB et rafraîchit l'UI.
     */
    private void handleUpdateStatus(Transfert t, String newStatus) {
        try {
            t.setStatus(newStatus);
            service.modifier(t); // Appel au service pour mettre à jour la base

            // 🔥 Si le transfert est validé comme REÇU, on augmente le stock de la banque
            if ("REÇU".equalsIgnoreCase(newStatus)) {
                StockService stockService = new StockService();
                String typeSang = (t.getDemande() != null) ? t.getDemande().getTypeSang() : "Inconnu";
                
                // On crée une entrée dans le stock pour cet arrivage
                Stock newStock = new Stock();
                newStock.setTypeOrgid(t.getToOrgId()); // ID de l'organisation destination
                newStock.setTypeOrg("Banque");        // Type d'organisation
                newStock.setTypeSang(typeSang);
                newStock.setQuantite(t.getQuantite());
                newStock.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                newStock.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                
                stockService.ajouter(newStock);
                
                // Alerte de succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Stock Mis à Jour");
                alert.setHeaderText(null);
                alert.setContentText("Le transfert a été réceptionné et le stock a été augmenté de " + t.getQuantite() + " ml (" + typeSang + ").");
                alert.showAndWait();
            }

            loadData(); // Rafraîchissement automatique
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la mise à jour du statut ou du stock.");
            alert.showAndWait();
        }
    }

    /**
     * Utilitaire pour créer un bouton icône stylisé.
     */
    private Button createSmallIconButton(String svgPath, String color, String tooltipText) {
        Button btn = new Button();
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setFill(Color.web(color));
        icon.setScaleX(0.6); icon.setScaleY(0.6);

        btn.setGraphic(icon);
        btn.setTooltip(new Tooltip(tooltipText));

        // Design du bouton (Sombre et moderne)
        btn.setStyle("-fx-background-color: #222222; -fx-border-color: #333333; -fx-border-radius: 12; -fx-background-radius: 12; -fx-min-width: 34; -fx-min-height: 34; -fx-cursor: hand;");

        // Animation simple au survol
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #333333; -fx-border-color: " + color + "; -fx-border-radius: 12; -fx-background-radius: 12; -fx-min-width: 34; -fx-min-height: 34; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #222222; -fx-border-color: #333333; -fx-border-radius: 12; -fx-background-radius: 12; -fx-min-width: 34; -fx-min-height: 34; -fx-cursor: hand;"));

        return btn;
    }

    @FXML
    public void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx"));
        File file = fileChooser.showSaveDialog(txtSearch.getScene().getWindow());

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Transferts");

                // Header Style
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font font = workbook.createFont();
                font.setColor(IndexedColors.WHITE.getIndex());
                font.setBold(true);
                headerStyle.setFont(font);

                // Create Header Row
                Row headerRow = sheet.createRow(0);
                String[] columns = {"ID", "Demande ID", "Origine", "Destination", "Quantité", "Statut", "Date Envoie"};
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Fill Data
                int rowNum = 1;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (Transfert t : filteredData) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(t.getId());
                    row.createCell(1).setCellValue(t.getDemande() != null ? t.getDemande().getId() : 0);
                    row.createCell(2).setCellValue(t.getFromOrg());
                    row.createCell(3).setCellValue(t.getToOrg());
                    row.createCell(4).setCellValue(t.getQuantite());
                    row.createCell(5).setCellValue(t.getStatus());
                    row.createCell(6).setCellValue(t.getDateEnvoie() != null ? t.getDateEnvoie().format(formatter) : "N/A");
                }

                // Auto-size columns
                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Exportation réussie");
                alert.setHeaderText(null);
                alert.setContentText("Le fichier Excel a été généré avec succès !");
                alert.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur d'exportation");
                alert.setContentText("Une erreur est survenue lors de la création du fichier Excel.");
                alert.showAndWait();
            }
        }
    }
    private void openViewDetails(Transfert selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgentBanqueViewTransfert.fxml"));
            Parent root = loader.load();
            AgentBanqueViewTransfertController controller = loader.getController();
            controller.initData(selected);
            AgentBanqueBaseController.getInstance().loadView(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void showStats() {
        if (masterData.isEmpty()) return;

        // Group by Demand ID and sum quantities
        Map<String, Integer> stats = masterData.stream()
                .filter(t -> t.getDemande() != null)
                .collect(Collectors.groupingBy(
                        t -> "DEM-" + t.getDemande().getId(),
                        Collectors.summingInt(Transfert::getQuantite)
                ));

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #0d0d0d; -fx-border-color: #222222; -fx-border-width: 2; -fx-background-radius: 15; -fx-border-radius: 15; -fx-padding: 30;");
        root.setPrefWidth(600);
        root.setPrefHeight(500);

        // Header
        Label title = new Label("STATISTIQUES DE TRANSFERT");
        title.setStyle("-fx-text-fill: #E53935; -fx-font-size: 14px; -fx-font-weight: bold; -fx-letter-spacing: 2px;");
        Label subtitle = new Label("Quantité totale transférée par demande");
        subtitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Demande");
        xAxis.setTickLabelFill(Color.web("#888888"));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Quantité (ml)");
        yAxis.setTickLabelFill(Color.web("#888888"));

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);
        }

        barChart.getData().add(series);

        // Apply dark theme to chart nodes via lookups
        barChart.lookupAll(".chart-plot-background").forEach(n -> n.setStyle("-fx-background-color: transparent;"));

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-padding: 8 20; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());

        HBox footer = new HBox(closeBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, subtitle, barChart, footer);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
