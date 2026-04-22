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
import tn.esprit.services.CampagneService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ListeCampagnes extends BaseFront {
    @FXML private FlowPane campaignContainer;
    @FXML private TextField searchField;
    @FXML private Label campaignCountLabel;
    @FXML private VBox emptyState;

    private final CampagneService campagneService = new CampagneService();
    private final Client currentClient = new Client(1, "O+", LocalDate.of(2023, 1, 1));
    private List<Campagne> campagnes = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            campagnes = campagneService.recupererByClient(currentClient);
            renderCampagnes(campagnes);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            renderCampagnes(new ArrayList<>());
        }

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter(newValue));
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
