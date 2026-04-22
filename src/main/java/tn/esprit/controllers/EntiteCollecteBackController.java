package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

    private EntiteCollecteService service = new EntiteCollecteService();
    private ObservableList<EntiteDeCollecte> observableList;
    private boolean isSorted = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initialiserTable();
        chargerDonnees();

        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            rechercher(newValue);
        });
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEntiteCollecte.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Entité de Collecte");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            chargerDonnees();
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
            
            ModifierEntiteCollecteController modifierController = loader.getController();
            modifierController.setEntite(selected);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier l'Entité de Collecte");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            chargerDonnees();
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
            } catch (Exception e) {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur lors de la suppression", e.getMessage());
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
