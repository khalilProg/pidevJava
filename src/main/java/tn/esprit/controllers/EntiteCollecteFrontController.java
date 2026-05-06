package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.EntiteCollecteService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class EntiteCollecteFrontController extends BaseFront implements Initializable {

    @FXML private ScrollPane scrollPaneEntite;
    @FXML private GridPane gridEntites;
    @FXML private TextField txtRechercheFront;
    @FXML private Button btnTrier;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnToggleView;
    @FXML private VBox mapContainer;
    @FXML private WebView mapWebView;
    @FXML private HBox dashboardContainer;

    private EntiteCollecteService service = new EntiteCollecteService();
    private List<EntiteDeCollecte> activeList = new ArrayList<>();
    private boolean isSorted = false;
    private EntiteDeCollecte selectedEntite = null;
    private VBox selectedCard = null;
    private boolean isMapView = false;
    private WebEngine webEngine;
    private boolean mapLoaded = false;

    private static final String STYLE_NORMAL   = "-fx-background-color: #141414; -fx-background-radius: 16; -fx-border-color: #2D2D2D; -fx-border-width: 1; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 12, 0, 0, 5); -fx-cursor: hand; -fx-padding: 18;";
    private static final String STYLE_HOVER    = "-fx-background-color: #1A1A1A; -fx-background-radius: 16; -fx-border-color: rgba(255,62,62,0.3); -fx-border-width: 1; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(255,62,62,0.1), 15, 0, 0, 5); -fx-cursor: hand; -fx-padding: 18;";
    private static final String STYLE_SELECTED = "-fx-background-color: #1A1A1A; -fx-background-radius: 16; -fx-border-color: rgba(255,62,62,0.7); -fx-border-width: 2; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(255,62,62,0.2), 18, 0, 0, 6); -fx-cursor: hand; -fx-padding: 18;";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(); // Appel à BaseFront
        chargerDashboard();
        chargerDonnees(null);

        txtRechercheFront.textProperty().addListener((observable, oldValue, newValue) -> {
            chargerDonnees(newValue);
        });
    }

    private void chargerDashboard() {
        dashboardContainer.getChildren().clear();

        int totalEntites = service.countTotalEntites();
        int totalCampagnes = service.countTotalCampagnesLiees();
        java.util.List<Object[]> top3 = service.getTopEntitesByContributions(3);

        // Card: Total Entités
        VBox cardTotal = buildStatCard("🏥", String.valueOf(totalEntites), "Total Entités", false, "#E63946");
        HBox.setHgrow(cardTotal, Priority.ALWAYS);

        // Card: Campagnes liées
        VBox cardCampagnes = buildStatCard("🩸", String.valueOf(totalCampagnes), "Campagnes Liées", false, "#E63946");
        HBox.setHgrow(cardCampagnes, Priority.ALWAYS);

        dashboardContainer.getChildren().addAll(cardTotal, cardCampagnes);

        // Top 3 entités les plus actives
        String[] medals = {"1", "2", "3"};
        String[] colors = {"#FFD700", "#C0C0C0", "#CD7F32"}; // Gold, Silver, Bronze

        for (int i = 0; i < top3.size(); i++) {
            Object[] row = top3.get(i);
            String nom = (String) row[1];
            String type = (String) row[2];
            int nbCampagnes = (int) row[4];

            VBox card = buildStatCard(
                    "#" + medals[i],
                    String.valueOf(nbCampagnes),
                    nom + " (" + type + ")",
                    true,
                    colors[i]
            );
            HBox.setHgrow(card, Priority.ALWAYS);
            dashboardContainer.getChildren().add(card);
        }
    }

    private VBox buildStatCard(String icon, String value, String label, boolean isTop3, String rankColor) {
        VBox card = new VBox(10);
        card.getStyleClass().add("bloodlink-stat-card");

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblIcon = new Label(icon);
        lblIcon.getStyleClass().add("bloodlink-stat-icon");
        if (isTop3) {
            lblIcon.setStyle("-fx-text-fill: " + rankColor + "; -fx-font-size: 16px; -fx-font-weight: 900;");
        }

        StackPane iconContainer = new StackPane(lblIcon);
        iconContainer.getStyleClass().add("bloodlink-stat-icon-container");
        if (isTop3) {
            iconContainer.setStyle("-fx-background-color: " + rankColor + "1A;"); // 10% opacity
        }

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("bloodlink-stat-value");

        topRow.getChildren().addAll(iconContainer, lblValue);

        Label lblLabel = new Label(label);
        lblLabel.getStyleClass().add("bloodlink-stat-label");

        card.getChildren().addAll(topRow, lblLabel);
        return card;
    }

    private void chargerDonnees(String recherche) {
        try {
            if (recherche == null || recherche.trim().isEmpty()) {
                activeList = service.recuperer();
            } else {
                activeList = service.rechercherParNom(recherche);
            }
            isSorted = false;
            btnTrier.setText("⬇ Trier de A - Z");
            selectedEntite = null;
            selectedCard = null;
            renduCartes();
            if (isMapView) rafraichirCarte();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void handleTrier(ActionEvent event) {
        if (!isSorted) {
            Collections.sort(activeList, Comparator.comparing(EntiteDeCollecte::getNom, String.CASE_INSENSITIVE_ORDER));
            btnTrier.setText("⬆ Reprendre l'ordre");
            isSorted = true;
            renduCartes();
            if (isMapView) rafraichirCarte();
        } else {
            chargerDonnees(txtRechercheFront.getText());
        }
    }

    @FXML
    void handleAjouter(ActionEvent event) {
        switchScene(event, "/AjouterEntiteCollecte.fxml");
    }

    @FXML
    void handleModifier(ActionEvent event) {
        if (selectedEntite == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection", "Cliquez sur une carte pour la sélectionner avant de modifier.");
            return;
        }
        ModifierEntiteCollecteController.setEntiteActive(selectedEntite);
        switchScene(event, "/ModifierEntiteCollecte.fxml");
    }

    @FXML
    void handleSupprimer(ActionEvent event) {
        if (selectedEntite == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection", "Cliquez sur une carte avant de supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'entité « " + selectedEntite.getNom() + " » ?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                service.supprimer(selectedEntite);
                afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Entité supprimée avec succès !");
                chargerDonnees(txtRechercheFront.getText());
            } catch (Exception e) {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    @FXML
    void handleToggleView(ActionEvent event) {
        isMapView = !isMapView;
        if (isMapView) {
            btnToggleView.setText("🔲 Vue Grille");
            scrollPaneEntite.setVisible(false);
            mapContainer.setVisible(true);

            if (!mapLoaded) {
                webEngine = mapWebView.getEngine();
                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        mapLoaded = true;
                        rafraichirCarte();
                    }
                });
                URL url = getClass().getResource("/map_entites.html");
                if (url != null) {
                    webEngine.load(url.toExternalForm());
                } else {
                    System.err.println("Fichier map_entites.html introuvable !");
                }
            } else {
                rafraichirCarte();
            }
        } else {
            btnToggleView.setText("🗺️ Vue Carte");
            scrollPaneEntite.setVisible(true);
            mapContainer.setVisible(false);
        }
    }

    private void rafraichirCarte() {
        if (!mapLoaded || webEngine == null) return;

        webEngine.executeScript("clearMarkers();");
        for (EntiteDeCollecte e : activeList) {
            String nom = e.getNom().replace("'", "\\'");
            String type = e.getType().replace("'", "\\'");
            String adresse = (e.getAdresse() + ", " + e.getVille() + ", Tunisie").replace("'", "\\'");
            String tel = e.getTel().replace("'", "\\'");

            String script = String.format("queueMarqueur('%s', '%s', '%s', '%s');", nom, type, adresse, tel);
            webEngine.executeScript(script);
        }
    }

    private void renduCartes() {
        gridEntites.getChildren().clear();
        int row = 0, column = 0;

        for (EntiteDeCollecte e : activeList) {
            VBox carte = creerCarte(e);
            gridEntites.add(carte, column, row);
            column++;
            if (column == 3) { column = 0; row++; }
        }
    }

    private VBox creerCarte(EntiteDeCollecte e) {
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setStyle(STYLE_NORMAL);
        vbox.setPrefWidth(230);

        // Hover & sélection
        vbox.setOnMouseEntered(ev -> { if (vbox != selectedCard) vbox.setStyle(STYLE_HOVER); });
        vbox.setOnMouseExited(ev  -> { if (vbox != selectedCard) vbox.setStyle(STYLE_NORMAL); });
        vbox.setOnMouseClicked(ev -> {
            if (selectedCard != null) selectedCard.setStyle(STYLE_NORMAL);
            selectedEntite = e;
            selectedCard = vbox;
            vbox.setStyle(STYLE_SELECTED);
        });

        Label lblNom = new Label(e.getNom());
        lblNom.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblNom.setTextFill(Color.WHITE);
        lblNom.setWrapText(true);

        // Badge type
        Label lblType = new Label(e.getType());
        lblType.setStyle("-fx-background-color: rgba(255,62,62,0.15); -fx-background-radius: 6; -fx-padding: 3 8; -fx-text-fill: #FF3E3E; -fx-font-size: 11px; -fx-font-weight: 800;");

        // Séparateur
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: #222222;");

        Label lblContact = new Label("📞  " + e.getTel());
        lblContact.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        lblContact.setTextFill(Color.web("#CCCCCC"));

        Label lblAdresse = new Label("📍  " + e.getAdresse() + ", " + e.getVille());
        lblAdresse.setWrapText(true);
        lblAdresse.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        lblAdresse.setTextFill(Color.web("#888888"));

        vbox.getChildren().addAll(lblNom, lblType, sep, lblContact, lblAdresse);
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
