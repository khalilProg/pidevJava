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
import javafx.beans.property.SimpleStringProperty;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.CampagneService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CampagneBackController implements Initializable {

    @FXML private TableView<Campagne> tableCampagne;
    @FXML private TextField txtRecherche;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnTrier;

    private CampagneService service = new CampagneService();
    private ObservableList<Campagne> observableList;
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
        TableColumn<Campagne, Integer> colId = (TableColumn<Campagne, Integer>) tableCampagne.getColumns().get(0);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Campagne, String> colTitre = (TableColumn<Campagne, String>) tableCampagne.getColumns().get(1);
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));

        TableColumn<Campagne, String> colDesc = (TableColumn<Campagne, String>) tableCampagne.getColumns().get(2);
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Campagne, String> colDateDeb = (TableColumn<Campagne, String>) tableCampagne.getColumns().get(3);
        colDateDeb.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));

        TableColumn<Campagne, String> colDateFin = (TableColumn<Campagne, String>) tableCampagne.getColumns().get(4);
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));

        TableColumn<Campagne, String> colSang = (TableColumn<Campagne, String>) tableCampagne.getColumns().get(5);
        colSang.setCellValueFactory(new PropertyValueFactory<>("typeSang"));

        TableColumn<Campagne, String> colEntites = (TableColumn<Campagne, String>) tableCampagne.getColumns().get(6);
        colEntites.setCellValueFactory(cellData -> {
            List<EntiteDeCollecte> entites = cellData.getValue().getEntiteDeCollectes();
            if (entites == null || entites.isEmpty()) {
                return new SimpleStringProperty("Aucune");
            }
            List<String> noms = new ArrayList<>();
            for (EntiteDeCollecte e : entites) noms.add(e.getNom());
            return new SimpleStringProperty(String.join(", ", noms));
        });
    }

    private void chargerDonnees() {
        try {
            List<Campagne> list = service.recuperer();
            observableList = FXCollections.observableArrayList(list);
            tableCampagne.setItems(observableList);
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
        List<Campagne> listRecherche = service.rechercherParTitre(text);
        observableList = FXCollections.observableArrayList(listRecherche);
        tableCampagne.setItems(observableList);
    }
    
    @FXML
    void handleTrier(ActionEvent event) {
        if (observableList != null) {
            if (!isSorted) {
                FXCollections.sort(observableList, (c1, c2) -> c1.getTitre().compareToIgnoreCase(c2.getTitre()));
                btnTrier.setText("⬆ Reprendre l'ordre");
                isSorted = true;
            } else {
                chargerDonnees(); // Recharge l'ordre par défaut (ID/Date de création BD)
                btnTrier.setText("⬇ Trier de A - Z");
                isSorted = false;
            }
        }
    }

    @FXML
    void handleAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCampagne.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Campagne");
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
        Campagne selected = tableCampagne.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une campagne à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCampagne.fxml"));
            Parent root = loader.load();
            
            ModifierCampagneController modifierController = loader.getController();
            modifierController.setCampagne(selected);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier la Campagne");
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
        Campagne selected = tableCampagne.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une campagne à supprimer.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette campagne : " + selected.getTitre() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if(confirm.getResult() == ButtonType.YES) {
            try {
                service.supprimer(selected);
                afficherAlerte(Alert.AlertType.INFORMATION, "Succès", "Campagne supprimée avec succès !");
                chargerDonnees();
            } catch (Exception e) {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur de suppression", e.getMessage());
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
