package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.beans.property.SimpleStringProperty;
import tn.esprit.entities.Demande;
import tn.esprit.services.DemandeService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tn.esprit.entities.Banque;
import tn.esprit.services.BanqueService;

public class AgentBanqueDemandeController {

    // Container for dynamic cards instead of table
    @FXML private VBox cardsContainer;
    @FXML private Label lblDemandeCount;

    @FXML private VBox emptyStateContainer;
    @FXML private VBox tableContainer;

    private DemandeService service = new DemandeService();
    private BanqueService banqueService = new BanqueService();
    private ObservableList<Demande> list = FXCollections.observableArrayList();
    private Map<Integer, String> bankNamesMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadBankNames();
        loadData();
    }

    private javafx.scene.Node createDemandeCard(Demande d) {
        HBox card = new HBox(25);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("glass-card");
        card.setStyle("-fx-padding: 15 25; -fx-cursor: hand;");
        
        // 1. ID Pill
        Label lblId = new Label("#" + d.getId());
        lblId.setStyle("-fx-padding: 5 12; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblId.getStyleClass().add("card-subtitle");
        
        // 2. Bank Name with Icon
        HBox bankBox = new HBox(8);
        bankBox.setAlignment(Pos.CENTER_LEFT);
        bankBox.setPrefWidth(220); // fixed width for alignment
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent("M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z");
        icon.setFill(javafx.scene.paint.Color.web("#888888"));
        icon.setScaleX(0.8); icon.setScaleY(0.8);
        Label lblBank = new Label(bankNamesMap.getOrDefault(d.getBanque(), "ID: " + d.getBanque()));
        lblBank.getStyleClass().add("card-subtitle");
        lblBank.setStyle("-fx-font-size: 13px;");
        bankBox.getChildren().addAll(icon, lblBank);
        
        // 3. Blood Type
        Label lblType = new Label(d.getTypeSang() != null ? d.getTypeSang() : "N/A");
        lblType.setStyle("-fx-border-color: #E53935; -fx-border-radius: 12; -fx-text-fill: #E53935; -fx-font-weight: bold; -fx-padding: 4 12; -fx-font-size: 13px;");
        lblType.setPrefWidth(80);
        lblType.setAlignment(Pos.CENTER);
        
        // 4. Quantity
        Label lblQty = new Label(d.getQuantite() + " ml");
        lblQty.getStyleClass().add("card-title");
        lblQty.setStyle("-fx-font-size: 13px;");
        lblQty.setPrefWidth(80);
        
        // 5. Status
        String statusText = d.getStatus() != null ? d.getStatus().toUpperCase() : "INCONNU";
        Label lblStatus = new Label("⌚ " + statusText);
        String styleStatus = "-fx-padding: 6 15; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold; ";
        if (statusText.toLowerCase().contains("attente")) {
            styleStatus += "-fx-background-color: rgba(255, 193, 7, 0.2); -fx-text-fill: #ffc107;";
        } else if (statusText.toLowerCase().contains("confirm")) {
            styleStatus += "-fx-background-color: rgba(40, 167, 69, 0.2); -fx-text-fill: #28a745;";
            lblStatus.setText("✔ " + statusText);
        } else {
            styleStatus += "-fx-background-color: rgba(128, 128, 128, 0.2); -fx-text-fill: #888888;";
        }
        lblStatus.setStyle(styleStatus);
        lblStatus.setPrefWidth(140);
        lblStatus.setAlignment(Pos.CENTER);
        
        // Region Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // 6. Action Buttons
        HBox actionBox = new HBox(12);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnView = createIconButton("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z", "#007bff", "rgba(0,123,255,0.2)", "transparent");
        Button btnEdit = createIconButton("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a.996.996 0 0 0 0-1.41l-2.34-2.34a.996.996 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z", "#ffc107", "rgba(255,193,7,0.2)", "transparent");
        Button btnDelete = createIconButton("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z", "#E53935", "rgba(229,57,53,0.2)", "transparent");
        
        btnView.setOnAction(e -> openViewDetails(d));
        btnEdit.setOnAction(e -> openEditForm(d));
        btnDelete.setOnAction(e -> {
            try {
                service.supprimer(d);
                loadData();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        
        actionBox.getChildren().addAll(btnView, btnEdit, btnDelete);
        
        card.getChildren().addAll(lblId, bankBox, lblType, lblQty, lblStatus, spacer, actionBox);
        return card;
    }

    private Button createIconButton(String svgPath, String iconColor, String borderColor, String bgColor) {
        Button btn = new Button();
        javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
        path.setContent(svgPath);
        path.setFill(javafx.scene.paint.Color.web(iconColor));
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
        } catch (java.io.IOException ex) { ex.printStackTrace(); }
    }

    private void openViewDetails(Demande selected) {
        // We will implement ViewDemandeController with an initData method
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgentBanqueViewDemande.fxml"));
            Parent root = loader.load();
            
            AgentBanqueViewDemandeController controller = loader.getController();
            controller.initData(selected);
            
            AgentBanqueBaseController.getInstance().loadView(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
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
            list.clear();
            list.addAll(service.recuperer());

            if (list.isEmpty()) {
                emptyStateContainer.setVisible(true);
                emptyStateContainer.setManaged(true);
                tableContainer.setVisible(false);
                tableContainer.setManaged(false);
            } else {
                // Render custom cards
                cardsContainer.getChildren().clear();
                for (Demande d : list) {
                    cardsContainer.getChildren().add(createDemandeCard(d));
                }
                
                if (lblDemandeCount != null) {
                    lblDemandeCount.setText(list.size() + " demande(s)");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openNewDemande() {
        AgentBanqueBaseController.getInstance().loadView("/AgentBanqueAddDemande.fxml");
    }

    @FXML
    public void runAIPrediction() {
        System.out.println("Running AI Prediction...");
    }

    @FXML
    public void exportPDF() {
        System.out.println("Exporting to PDF...");
    }
}
