package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.CampagneService;

import java.io.IOException;
import java.time.LocalDate;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class CampagneFrontController extends BaseFront implements Initializable {

    @FXML private ScrollPane scrollPaneCampagnes;
    @FXML private GridPane gridCampagnes;
    @FXML private TextField txtRechercheFront;
    @FXML private DatePicker datePickerFilter;
    @FXML private Button btnTrier;
    @FXML private Button btnTrierDate;
    @FXML private VBox vboxEmptyState;

    private CampagneService service = new CampagneService();
    private List<Campagne> fullList = new ArrayList<>();
    private List<Campagne> activeList = new ArrayList<>();
    private boolean isSortedAZ = false;
    private boolean isSortedDate = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerToutesLesDonnees();

        txtRechercheFront.textProperty().addListener((obs, old, newValue) -> appliquerFiltres());
        datePickerFilter.valueProperty().addListener((obs, old, newValue) -> appliquerFiltres());
    }

    private void chargerToutesLesDonnees() {
        try {
            fullList = service.recuperer();
            appliquerFiltres();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void appliquerFiltres() {
        String recherche = txtRechercheFront.getText().toLowerCase().trim();
        LocalDate dateFiltre = datePickerFilter.getValue();

        activeList = new ArrayList<>();
        for (Campagne c : fullList) {
            boolean matchesSearch = recherche.isEmpty() || c.getTitre().toLowerCase().contains(recherche) || c.getDescription().toLowerCase().contains(recherche);
            boolean matchesDate = dateFiltre == null || (c.getDateDebut().equals(dateFiltre) || (c.getDateDebut().isBefore(dateFiltre) && c.getDateFin().isAfter(dateFiltre)) || c.getDateFin().equals(dateFiltre));

            if (matchesSearch && matchesDate) {
                activeList.add(c);
            }
        }
        
        if (isSortedAZ) {
            activeList.sort(Comparator.comparing(Campagne::getTitre, String.CASE_INSENSITIVE_ORDER));
        } else if (isSortedDate) {
            activeList.sort(Comparator.comparing(Campagne::getDateDebut).reversed());
        }
        
        renduCartes();
    }

    @FXML
    void handleTrier(ActionEvent event) {
        isSortedAZ = !isSortedAZ;
        isSortedDate = false;
        btnTrier.setText(isSortedAZ ? "A-Z ↑" : "Sort A-Z");
        btnTrierDate.setText("Date ↓");
        appliquerFiltres();
    }

    @FXML
    void handleTrierDate(ActionEvent event) {
        isSortedDate = !isSortedDate;
        isSortedAZ = false;
        btnTrierDate.setText(isSortedDate ? "Date ↑" : "Date ↓");
        btnTrier.setText("Sort A-Z");
        appliquerFiltres();
    }

    @FXML
    void handleReset(ActionEvent event) {
        txtRechercheFront.clear();
        datePickerFilter.setValue(null);
        isSortedAZ = false;
        isSortedDate = false;
        btnTrier.setText("Sort A-Z");
        btnTrierDate.setText("Date ↓");
        appliquerFiltres();
    }
    private void renduCartes() {
        gridCampagnes.getChildren().clear();
        
        if (activeList.isEmpty()) {
            vboxEmptyState.setVisible(true);
            scrollPaneCampagnes.setVisible(false);
            return;
        }

        vboxEmptyState.setVisible(false);
        scrollPaneCampagnes.setVisible(true);

        int row = 0;
        int column = 0;

        for (Campagne c : activeList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CampagneCard.fxml"));
                Parent card = loader.load();
                CampagneCard controller = loader.getController();
                controller.setData(c);

                gridCampagnes.add(card, column, row);
                
                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
