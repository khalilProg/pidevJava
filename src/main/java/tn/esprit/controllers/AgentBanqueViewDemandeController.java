package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import tn.esprit.entities.Banque;
import tn.esprit.entities.Demande;
import tn.esprit.services.BanqueService;
import tn.esprit.services.DemandeService;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AgentBanqueViewDemandeController {

    @FXML private Label lblTitle;
    @FXML private Label lblCreatedAt;
    @FXML private Label lblStatusTop;
    @FXML private Label lblType;
    @FXML private Label lblQuantite;
    @FXML private Label lblUrgence;
    @FXML private Label lblBanque;

    private Demande currentDemande;
    private DemandeService service = new DemandeService();
    private BanqueService banqueService = new BanqueService();

    public void initData(Demande d) {
        this.currentDemande = d;
        lblTitle.setText("Demande #" + d.getId());
        
        if (d.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'à' HH:mm");
            lblCreatedAt.setText("Créée le " + d.getCreatedAt().format(formatter));
        }

        lblStatusTop.setText("● " + d.getStatus());
        lblType.setText(d.getTypeSang());
        lblQuantite.setText(String.valueOf(d.getQuantite()));
        lblUrgence.setText(d.getUrgence());
        
        // Fetch bank name
        try {
            List<Banque> banques = banqueService.recuperer();
            String bankName = "Banque #" + d.getBanque();
            for (Banque b : banques) {
                if (b.getId() == d.getBanque()) {
                    bankName = b.getNom();
                    break;
                }
            }
            lblBanque.setText(bankName);
        } catch (SQLException e) {
            lblBanque.setText("ID: " + d.getBanque());
        }
    }

    @FXML
    void handleEdit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgentBanqueEditDemande.fxml"));
            Parent root = loader.load();
            
            AgentBanqueEditDemandeController controller = loader.getController();
            controller.initData(currentDemande);
            
            AgentBanqueBaseController.getInstance().loadView(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        try {
            service.supprimer(currentDemande);
            goBack(event);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        AgentBanqueBaseController.getInstance().loadView("/AgentBanqueDemande.fxml");
    }
}
