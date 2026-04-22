package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class CampagneFrontController implements Initializable {

    @FXML private ScrollPane scrollPaneCampagnes;
    @FXML private GridPane gridCampagnes;
    @FXML private TextField txtRechercheFront;
    @FXML private Button btnTrier;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    private CampagneService service = new CampagneService();
    private List<Campagne> activeList = new ArrayList<>();
    private boolean isSorted = false;
    private Campagne selectedCampagne = null;
    private VBox selectedCard = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerDonnees(null);

        txtRechercheFront.textProperty().addListener((observable, oldValue, newValue) -> {
            chargerDonnees(newValue);
        });
    }

    private void chargerDonnees(String recherche) {
        try {
            if (recherche == null || recherche.trim().isEmpty()) {
                activeList = service.recuperer();
            } else {
                activeList = service.rechercherParTitre(recherche);
            }
            isSorted = false;
            btnTrier.setText("⬇ Trier de A - Z");
            selectedCampagne = null;
            selectedCard = null;
            renduCartes();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleTrier(ActionEvent event) {
        if (!isSorted) {
            Collections.sort(activeList, Comparator.comparing(Campagne::getTitre, String.CASE_INSENSITIVE_ORDER));
            btnTrier.setText("⬆ Reprendre l'ordre");
            isSorted = true;
        } else {
            chargerDonnees(txtRechercheFront.getText());
            return;
        }
        renduCartes();
    }

    @FXML
    void handleAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCampagne.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Campagne");
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerDonnees(txtRechercheFront.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleModifier(ActionEvent event) {
        if (selectedCampagne == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection", "Cliquez sur une carte de campagne pour la sélectionner avant de modifier.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCampagne.fxml"));
            Parent root = loader.load();
            ModifierCampagneController ctrl = loader.getController();
            ctrl.setCampagne(selectedCampagne);
            Stage stage = new Stage();
            stage.setTitle("Modifier la Campagne");
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            chargerDonnees(txtRechercheFront.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSupprimer(ActionEvent event) {
        if (selectedCampagne == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection", "Cliquez sur une carte de campagne avant de supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la campagne « " + selectedCampagne.getTitre() + " » ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                service.supprimer(selectedCampagne);
                afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Campagne supprimée !");
                chargerDonnees(txtRechercheFront.getText());
            } catch (Exception e) {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    private void renduCartes() {
        gridCampagnes.getChildren().clear();
        int row = 0;
        int column = 0;

        for (Campagne c : activeList) {
            VBox carte = creerCarte(c);
            gridCampagnes.add(carte, column, row);
            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    private static final String CARD_NORMAL = "-fx-background-color: #141414; -fx-background-radius: 16; -fx-border-color: #2D2D2D; -fx-border-width: 1; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 12, 0, 0, 5); -fx-cursor: hand; -fx-padding: 18;";
    private static final String CARD_SELECTED = "-fx-background-color: #1A1A1A; -fx-background-radius: 16; -fx-border-color: rgba(255,62,62,0.7); -fx-border-width: 2; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(255,62,62,0.2), 18, 0, 0, 6); -fx-cursor: hand; -fx-padding: 18;";

    private VBox creerCarte(Campagne c) {
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setStyle(CARD_NORMAL);
        vbox.setPrefWidth(230);

        // Sélection au clic
        vbox.setOnMouseClicked(e -> {
            if (selectedCard != null) selectedCard.setStyle(CARD_NORMAL);
            selectedCampagne = c;
            selectedCard = vbox;
            vbox.setStyle(CARD_SELECTED);
        });

        // Hover effect
        vbox.setOnMouseEntered(e -> { if (vbox != selectedCard) vbox.setStyle(CARD_NORMAL.replace("#141414", "#1A1A1A").replace("#2D2D2D", "rgba(255,62,62,0.3)")); });
        vbox.setOnMouseExited(e -> { if (vbox != selectedCard) vbox.setStyle(CARD_NORMAL); });

        Label lblTitre = new Label(c.getTitre());
        lblTitre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTitre.setTextFill(Color.WHITE);
        lblTitre.setWrapText(true);

        Label lblDesc = new Label(c.getDescription());
        lblDesc.setWrapText(true);
        lblDesc.setFont(Font.font("Segoe UI", 13));
        lblDesc.setTextFill(Color.web("#A0A0A0"));

        Label lblDates = new Label("📅 " + c.getDateDebut() + "  →  " + c.getDateFin());
        lblDates.setFont(Font.font("Segoe UI", 12));
        lblDates.setTextFill(Color.web("#888888"));

        Label lblSang = new Label("🩸 " + (c.getTypeSang() != null && !c.getTypeSang().isEmpty() ? c.getTypeSang() : "Tous"));
        lblSang.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblSang.setTextFill(Color.web("#FF3E3E"));

        String entitesTexte = "Aucune entité affectée.";
        if (c.getEntiteDeCollectes() != null && !c.getEntiteDeCollectes().isEmpty()) {
            List<String> noms = new ArrayList<>();
            for (EntiteDeCollecte entite : c.getEntiteDeCollectes()) noms.add(entite.getNom());
            entitesTexte = String.join(", ", noms);
        }
        Label lblEntites = new Label("📍 " + entitesTexte);
        lblEntites.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 11));
        lblEntites.setTextFill(Color.web("#666666"));
        lblEntites.setWrapText(true);

        // Séparateur visuel
        javafx.scene.layout.Region sep = new javafx.scene.layout.Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: #222222;");

        vbox.getChildren().addAll(lblTitre, sep, lblDesc, lblDates, lblSang, lblEntites);
        return vbox;
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
