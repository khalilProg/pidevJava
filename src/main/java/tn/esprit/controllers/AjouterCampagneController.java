package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.CampagneService;
import tn.esprit.services.EntiteCollecteService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AjouterCampagneController {

    @FXML private TextField txtTitre;
    @FXML private TextField txtDescription;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;

    @FXML private CheckBox cbAPlus, cbAMoins, cbBPlus, cbBMoins, cbABPlus, cbABMoins, cbOPlus, cbOMoins;
    @FXML private ListView<EntiteDeCollecte> listEntites;

    @FXML private Button btnEnregistrer;
    @FXML private Button btnAnnuler;

    private CampagneService service = new CampagneService();
    private EntiteCollecteService entiteService = new EntiteCollecteService();

    @FXML
    public void initialize() {
        // Préparer la Selection Multiple pour la ListView
        listEntites.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        try {
            List<EntiteDeCollecte> entites = entiteService.recuperer();
            ObservableList<EntiteDeCollecte> obs = FXCollections.observableArrayList(entites);
            listEntites.setItems(obs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleEnregistrer(ActionEvent event) {
        String titre = txtTitre.getText();
        String description = txtDescription.getText();
        LocalDate debut = dpDateDebut.getValue();
        LocalDate fin = dpDateFin.getValue();

        if (titre.isEmpty() || description.isEmpty() || debut == null || fin == null) {
            afficherErreur("Saisie incomplète", "Veuillez remplir tous les champs textuels et dates !");
            return;
        }

        if (description.length() <= 5) {
            afficherErreur("Description trop courte", "La description doit contenir plus de 5 caractères.");
            return;
        }

        if (debut.isBefore(LocalDate.now())) {
            afficherErreur("Date invalide", "La date de début doit être supérieure ou égale à aujourd'hui.");
            return;
        }

        if (fin.isBefore(debut) || fin.isEqual(debut)) {
            afficherErreur("Date invalide", "La date de fin doit être strictement supérieure à la date de début.");
            return;
        }

        // Récupération des types de sang
        List<String> typesChoisis = new ArrayList<>();
        if(cbAPlus.isSelected()) typesChoisis.add("A+");
        if(cbAMoins.isSelected()) typesChoisis.add("A-");
        if(cbBPlus.isSelected()) typesChoisis.add("B+");
        if(cbBMoins.isSelected()) typesChoisis.add("B-");
        if(cbABPlus.isSelected()) typesChoisis.add("AB+");
        if(cbABMoins.isSelected()) typesChoisis.add("AB-");
        if(cbOPlus.isSelected()) typesChoisis.add("O+");
        if(cbOMoins.isSelected()) typesChoisis.add("O-");

        if(typesChoisis.isEmpty()) {
            afficherErreur("Type de sang manquant", "Veuillez sélectionner au moins un type de sang.");
            return;
        }
        String typeSangFinal = String.join(", ", typesChoisis);

        // Récupération des entités sélectionnées
        List<EntiteDeCollecte> selection = listEntites.getSelectionModel().getSelectedItems();
        if(selection == null || selection.isEmpty()) {
            afficherErreur("Entité manquante", "Veuillez sélectionner au moins une entité de collecte.");
            return;
        }

        Campagne c = new Campagne();
        c.setTitre(titre);
        c.setDescription(description);
        c.setDateDebut(debut);
        c.setDateFin(fin);
        c.setTypeSang(typeSangFinal);
        c.setCreatedAt(LocalDateTime.now());
        c.setEntiteDeCollectes(new ArrayList<>(selection));

        try {
            service.ajouter(c);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Campagne ajoutée avec succès !");
            alert.showAndWait();
            fermerFenetre();
        } catch (IllegalArgumentException ex) {
            afficherErreur("Erreur d'unicité", ex.getMessage());
        } catch (SQLException ex) {
            afficherErreur("Erreur Serveur", "Erreur lors de l'ajout en BD : " + ex.getMessage());
        }
    }

    @FXML
    void handleAnnuler(ActionEvent event) {
        fermerFenetre();
    }
    
    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
