package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.Client;
import tn.esprit.entities.User;
import tn.esprit.services.CampagneService;
import tn.esprit.services.ClientService;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListeCampagnes extends BaseFront {
    @FXML private FlowPane campaignContainer;
    @FXML private TextField searchField;
    @FXML private Label campaignCountLabel;
    @FXML private VBox emptyState;

    private final CampagneService campagneService = new CampagneService();
    private final ClientService clientService = new ClientService();
    private List<Campagne> campagnes = new ArrayList<>();

    @FXML
    public void initialize() {
        applySessionUser();

        try {
            Client currentClient = resolveCurrentClient();
            campagnes = currentClient == null ? new ArrayList<>() : campagneService.recupererByClient(currentClient);
            renderCampagnes(campagnes);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            renderCampagnes(new ArrayList<>());
        }

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter(newValue));
    }

    private Client resolveCurrentClient() throws SQLException {
        User user = SessionManager.getCurrentUser();
        return user == null ? null : clientService.getByUserId(user.getId());
    }

    private void applyFilter(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase();
        if (normalized.isEmpty()) {
            renderCampagnes(campagnes);
            return;
        }

        List<Campagne> filtered = campagnes.stream()
                .filter(c -> c.getTitre().toLowerCase().contains(normalized)
                        || c.getDescription().toLowerCase().contains(normalized)
                        || c.getTypeSang().toLowerCase().contains(normalized))
                .toList();
        renderCampagnes(filtered);
    }

    private void renderCampagnes(List<Campagne> data) {
        campaignContainer.getChildren().clear();
        campaignCountLabel.setText(data.size() + " campagnes disponibles");
        boolean isEmpty = data.isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);

        if (isEmpty) {
            return;
        }

        for (Campagne campagne : data) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientCampagneCard.fxml"));
                AnchorPane card = loader.load();
                CampagneCard controller = loader.getController();
                controller.setData(campagne);
                campaignContainer.getChildren().add(card);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
