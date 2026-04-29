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
import tn.esprit.entities.Transfert;
import tn.esprit.services.TransfertService;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AgentBanqueTransfertController {

    @FXML private VBox cardsContainer;
    @FXML private Label lblCount;

    private TransfertService service = new TransfertService();
    private ObservableList<Transfert> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadData();
    }

    private javafx.scene.Node createTransfertCard(Transfert t) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("glass-card");
        card.setStyle("-fx-padding: 15 25; -fx-cursor: hand;");
        
        // 1. ID Pill
        Label lblId = new Label("#" + t.getId());
        lblId.setStyle("-fx-padding: 5 12; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblId.getStyleClass().add("card-subtitle");
        
        // 2. Demande Link Pill
        int dId = (t.getDemande() != null) ? t.getDemande().getId() : 0;
        Label lblDemande = new Label("DEM-" + dId);
        lblDemande.setStyle("-fx-background-color: transparent; -fx-border-color: #888888; -fx-border-radius: 12; -fx-padding: 4 12; -fx-font-size: 11px;");
        lblDemande.getStyleClass().add("card-title");
        
        // 3. Mini Flow Map (Origine -> Arrow -> Destination)
        HBox flowMap = new HBox(8);
        flowMap.setAlignment(Pos.CENTER_LEFT);
        flowMap.setPrefWidth(300);
        
        Label lblOrigine = new Label(t.getFromOrg() != null ? t.getFromOrg().toUpperCase() : "N/A");
        lblOrigine.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        lblOrigine.getStyleClass().add("card-subtitle");
        
        javafx.scene.shape.SVGPath arrow = new javafx.scene.shape.SVGPath();
        arrow.setContent("M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z");
        arrow.setFill(javafx.scene.paint.Color.web("#E53935"));
        arrow.setScaleX(0.7); arrow.setScaleY(0.7);
        
        Label lblDest = new Label(t.getToOrg() != null ? t.getToOrg().toUpperCase() : "N/A");
        lblDest.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        lblDest.getStyleClass().add("card-title");
        
        flowMap.getChildren().addAll(lblOrigine, arrow, lblDest);
        
        // 4. Quantity
        Label lblQty = new Label(t.getQuantite() + " ml");
        lblQty.setStyle("-fx-font-size: 13px;");
        lblQty.getStyleClass().add("card-title");
        lblQty.setPrefWidth(80);
        
        // 5. Date Envoi
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateStr = (t.getDateEnvoie() != null) ? t.getDateEnvoie().format(formatter) : "--/--/----";
        Label lblDate = new Label(dateStr);
        lblDate.setStyle("-fx-font-size: 13px;");
        lblDate.getStyleClass().add("card-subtitle");
        lblDate.setPrefWidth(100);
        
        // 6. Status
        String statusText = t.getStatus() != null ? t.getStatus().toUpperCase() : "EN COURS";
        Label lblStatus = new Label();
        String styleStatus = "-fx-padding: 6 15; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold; ";
        if (statusText.contains("REÇU") || statusText.contains("RECU")) {
            styleStatus += "-fx-background-color: rgba(40, 167, 69, 0.2); -fx-text-fill: #28a745;";
            lblStatus.setText("✔ REÇU");
        } else if (statusText.contains("COURS") || statusText.contains("ATTENTE")) {
            styleStatus += "-fx-background-color: rgba(255, 193, 7, 0.2); -fx-text-fill: #ffc107;";
            lblStatus.setText("🚚 EN COURS");
        } else {
            styleStatus += "-fx-background-color: rgba(128, 128, 128, 0.2); -fx-text-fill: #888888;";
            lblStatus.setText(statusText);
        }
        lblStatus.setStyle(styleStatus);
        lblStatus.setPrefWidth(120);
        lblStatus.setAlignment(Pos.CENTER);
        
        // Region Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // 7. Action Button
        Button btnView = new Button();
        javafx.scene.shape.SVGPath viewIcon = new javafx.scene.shape.SVGPath();
        viewIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
        viewIcon.setFill(javafx.scene.paint.Color.web("#007bff"));
        viewIcon.setScaleX(0.7); viewIcon.setScaleY(0.7);
        btnView.setGraphic(viewIcon);
        btnView.setStyle("-fx-background-color: rgba(0,123,255,0.2); -fx-border-color: rgba(0,123,255,0.2); -fx-border-radius: 15; -fx-background-radius: 15; -fx-min-width: 38; -fx-min-height: 38; -fx-cursor: hand;");
        btnView.setOnAction(e -> openViewDetails(t));
        
        card.getChildren().addAll(lblId, lblDemande, flowMap, lblQty, lblDate, lblStatus, spacer, btnView);
        return card;
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

    public void loadData() {
        try {
            list.clear();
            list.addAll(service.recuperer());
            
            cardsContainer.getChildren().clear();
            for (Transfert t : list) {
                cardsContainer.getChildren().add(createTransfertCard(t));
            }
            
            if (lblCount != null) {
                lblCount.setText(list.size() + " transfert(s)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
