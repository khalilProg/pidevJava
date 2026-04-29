package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.entities.Transfert;
import java.time.format.DateTimeFormatter;

public class AgentBanqueViewTransfertController {

    @FXML private Label lblTitle;
    @FXML private Label lblCreatedAt;
    @FXML private Label lblStatusTop;
    
    @FXML private Label lblOrigine;
    @FXML private Label lblOrigineId;
    
    @FXML private Label lblFlowQuantite;
    
    @FXML private Label lblDestination;
    @FXML private Label lblDestinationId;
    
    @FXML private Label lblDemandeLiee;
    @FXML private Label lblBloodType;
    @FXML private Label lblDateEnvoi;
    @FXML private Label lblDateReception;

    public void initData(Transfert t) {
        lblTitle.setText("Transfert #" + t.getId());
        
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
        if (t.getCreatedAt() != null) {
            lblCreatedAt.setText("Enregistré le " + t.getCreatedAt().format(dtFormatter));
        }

        // Status pill
        String status = t.getStatus() != null ? t.getStatus().toUpperCase() : "EN COURS";
        if (status.contains("REÇU") || status.contains("RECU")) {
            lblStatusTop.setText("✔ Transfert Reçu");
            lblStatusTop.setStyle("-fx-background-color: rgba(40, 167, 69, 0.1); -fx-border-color: #28a745; -fx-border-radius: 20; -fx-text-fill: #28a745; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 20; -fx-font-size: 14px;");
        } else {
            lblStatusTop.setText("🚚 " + status);
            lblStatusTop.setStyle("-fx-background-color: rgba(255, 193, 7, 0.1); -fx-border-color: #ffc107; -fx-border-radius: 20; -fx-text-fill: #ffc107; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 20; -fx-font-size: 14px;");
        }

        lblOrigine.setText(t.getFromOrg() != null ? t.getFromOrg().toUpperCase() : "N/A");
        lblOrigineId.setText("ID: " + t.getFromOrgId());

        lblFlowQuantite.setText("🩸 " + t.getQuantite());

        lblDestination.setText(t.getToOrg() != null ? t.getToOrg().toUpperCase() : "N/A");
        lblDestinationId.setText("ID: " + t.getToOrgId());

        // Cards
        int dId = t.getDemande() != null ? t.getDemande().getId() : 0;
        lblDemandeLiee.setText("DEM-" + dId);

        // Fetch blood type from attached demande if it exists, otherwise use stock logic
        if (t.getDemande() != null && t.getDemande().getTypeSang() != null) {
            lblBloodType.setText(t.getDemande().getTypeSang());
        } else {
            lblBloodType.setText("N/A"); // Replace with actual stock if possible within entity graph
        }

        DateTimeFormatter dFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblDateEnvoi.setText(t.getDateEnvoie() != null ? t.getDateEnvoie().format(dFormatter) : "--/--/----");
        lblDateReception.setText(t.getDateReception() != null ? t.getDateReception().format(dFormatter) : "--/--/----");
    }

    @FXML
    void goBack(ActionEvent event) {
        AgentBanqueBaseController.getInstance().loadView("/AgentBanqueTransfert.fxml");
    }
}
