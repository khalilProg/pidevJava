package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.client;
import tn.esprit.entities.User;
import tn.esprit.services.CampagneService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ListeCampagnes {
    @FXML private TilePane campaignContainer;
    private CampagneService campagneService = new CampagneService();
    private client currentClient = new client(1, "O+", LocalDate.of(2023, 1, 1)); //1
    private client currentClient1 = new client(2, "A-", LocalDate.of(2003, 10, 17)); //2


    @FXML
    public void initialize() {

        try {
            List<Campagne> campagnes = campagneService.recupererByClient(currentClient);

            for (Campagne c : campagnes) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CampagneCard.fxml"));
                AnchorPane card = loader.load();
                CampagneCard controller = loader.getController();
                controller.setData(c);
                campaignContainer.getChildren().add(card);
            }
        } catch (SQLException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
