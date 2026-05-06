package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
import java.util.List;
import java.util.ResourceBundle;

public class EntiteCollecteBackController implements Initializable {

    @FXML private TableView<EntiteDeCollecte> tableEntite;
    @FXML private TextField txtRecherche;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnTrier;
    @FXML private HBox dashboardContainer;

    private EntiteCollecteService service = new EntiteCollecteService();
    private ObservableList<EntiteDeCollecte> observableList;
    private boolean isSorted = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initialiserTable();
        chargerDashboard();
        chargerDonnees();

        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            rechercher(newValue);
        });
    }

    private void chargerDashboard() {
        dashboardContainer.getChildren().clear();

        int totalEntites = service.countTotalEntites();
        int totalCampagnes = service.countTotalCampagnesLiees();
        List<Object[]> top3 = service.getTopEntitesByContributions(3);

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

    private void initialiserTable() {
        TableColumn<EntiteDeCollecte, Integer> colId = (TableColumn<EntiteDeCollecte, Integer>) tableEntite.getColumns().get(0);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<EntiteDeCollecte, String> colNom = (TableColumn<EntiteDeCollecte, String>) tableEntite.getColumns().get(1);
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<EntiteDeCollecte, String> colTel = (TableColumn<EntiteDeCollecte, String>) tableEntite.getColumns().get(2);
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));

        TableColumn<EntiteDeCollecte, String> colType = (TableColumn<EntiteDeCollecte, String>) tableEntite.getColumns().get(3);
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<EntiteDeCollecte, String> colAdresse = (TableColumn<EntiteDeCollecte, String>) tableEntite.getColumns().get(4);
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
    }

    private void chargerDonnees() {
        try {
            List<EntiteDeCollecte> list = service.recuperer();
            observableList = FXCollections.observableArrayList(list);
            tableEntite.setItems(observableList);
            isSorted = false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void rechercher(String text) {
        if (text == null || text.trim().isEmpty()) {
            chargerDonnees();
            return;
        }
        List<EntiteDeCollecte> listRecherche = service.rechercherParNom(text);
        observableList = FXCollections.observableArrayList(listRecherche);
        tableEntite.setItems(observableList);
    }

    @FXML
    void handleTrier(ActionEvent event) {
        if (observableList != null) {
            if (!isSorted) {
                FXCollections.sort(observableList, (e1, e2) -> e1.getNom().compareToIgnoreCase(e2.getNom()));
                btnTrier.setText("⬆ Reprendre l'ordre");
                isSorted = true;
            } else {
                chargerDonnees();
                btnTrier.setText("⬇ Trier de A - Z");
                isSorted = false;
            }
        }
    }

    @FXML
    void handleAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEntiteAdmin.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Entité de Collecte");
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerDonnees();
            chargerDashboard();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleModifier(ActionEvent event) {
        EntiteDeCollecte selected = tableEntite.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une entité à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEntiteCollecte.fxml"));
            Parent root = loader.load();

            ModifierEntiteCollecteController.setEntiteActive(selected);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'Entité de Collecte");
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            chargerDonnees();
            chargerDashboard();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSupprimer(ActionEvent event) {
        EntiteDeCollecte selected = tableEntite.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une entité à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer l'entité : " + selected.getNom() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if(confirm.getResult() == ButtonType.YES) {
            try {
                service.supprimer(selected);
                afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Entité supprimée avec succès !");
                chargerDonnees();
                chargerDashboard();
            } catch (Exception e) {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur lors de la suppression", e.getMessage());
            }
        }
    }

    @FXML
    void handleRetour(ActionEvent event) {
        // Retour à la page d'administration principale si besoin
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
