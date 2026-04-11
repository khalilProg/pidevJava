package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import tn.esprit.entities.Campagne;

public class CampagneCard {
    @FXML
    private Text campaignName;
    @FXML
    private Text campaignDates;
    @FXML
    private Text bloodTypes;
    @FXML
    private Text campaignDescription;
    @FXML
    private Button participateButton;

    private Campagne campagne;

    public void setData(Campagne c) {
        this.campagne = c;
        campaignName.setText(c.getTitre());
        campaignDates.setText(c.getDateDebut() + " - " + c.getDateFin());
        bloodTypes.setText(c.getTypeSang());
        campaignDescription.setText(c.getDescription());
    }

}
