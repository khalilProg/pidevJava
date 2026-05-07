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
import java.util.ArrayList;
import java.util.List;

public class ModifierCampagneController {

    private Campagne campagneActive;
    private CampagneService service = new CampagneService();
    private EntiteCollecteService entiteService = new EntiteCollecteService();

    @FXML private TextField txtTitre;
    @FXML private TextField txtDescription;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;

    @FXML private CheckBox cbAPlus, cbAMoins, cbBPlus, cbBMoins, cbABPlus, cbABMoins, cbOPlus, cbOMoins;
    @FXML private ListView<EntiteDeCollecte> listEntites;

    @FXML private Button btnEnregistrer;
    @FXML private Button btnAnnuler;

    @FXML
    public void initialize() {
        listEntites.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        try {
            List<EntiteDeCollecte> entites = entiteService.recuperer();
            ObservableList<EntiteDeCollecte> obs = FXCollections.observableArrayList(entites);
            listEntites.setItems(obs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setCampagne(Campagne c) {
        this.campagneActive = c;
        txtTitre.setText(c.getTitre());
        txtDescription.setText(c.getDescription());
        dpDateDebut.setValue(c.getDateDebut());
        dpDateFin.setValue(c.getDateFin());
        
        // Cocher les bonnes cases
        String typeSang = c.getTypeSang();
        if (typeSang != null) {
            if(typeSang.contains("A+")) cbAPlus.setSelected(true);
            if(typeSang.contains("A-")) cbAMoins.setSelected(true);
            if(typeSang.contains("B+")) cbBPlus.setSelected(true);
            if(typeSang.contains("B-")) cbBMoins.setSelected(true);
            if(typeSang.contains("AB+")) cbABPlus.setSelected(true);
            if(typeSang.contains("AB-")) cbABMoins.setSelected(true);
            if(typeSang.contains("O+")) cbOPlus.setSelected(true);
            if(typeSang.contains("O-")) cbOMoins.setSelected(true);
        }

        // Sélectionner les entités
        if (c.getEntiteDeCollectes() != null) {
            for (EntiteDeCollecte e : c.getEntiteDeCollectes()) {
                // Chercher l'index de cette entité dans la liste (basé sur l'id)
                for (int i=0; i<listEntites.getItems().size(); i++) {
                    if (listEntites.getItems().get(i).getId() == e.getId()) {
                        listEntites.getSelectionModel().select(i);
                        break;
                    }
                }
            }
        }
    }

    @FXML
    void handleEnregistrer(ActionEvent event) {
        String titre = txtTitre.getText();
        String description = txtDescription.getText();
        LocalDate debut = dpDateDebut.getValue();
        LocalDate fin = dpDateFin.getValue();

        if (titre.isEmpty() || description.isEmpty() || debut == null || fin == null) {
            afficherErreur("Saisie incomplète", "Veuillez remplir tous les champs !");
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
            afficherErreur("Type manquant", "Sélectionnez au moins un type de sang.");
            return;
        }
        
        List<EntiteDeCollecte> selection = listEntites.getSelectionModel().getSelectedItems();
        if(selection == null || selection.isEmpty()) {
            afficherErreur("Entité manquante", "Sélectionnez au moins une entité de collecte.");
            return;
        }

        campagneActive.setTitre(titre);
        campagneActive.setDescription(description);
        campagneActive.setDateDebut(debut);
        campagneActive.setDateFin(fin);
        campagneActive.setTypeSang(String.join(", ", typesChoisis));
        campagneActive.setEntiteDeCollectes(new ArrayList<>(selection));

        try {
            service.modifier(campagneActive);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Campagne modifiée avec succès !");
            alert.showAndWait();
            fermerFenetre();
        } catch (IllegalArgumentException ex) {
            afficherErreur("Erreur d'unicité", ex.getMessage());
        } catch (Exception ex) {
            afficherErreur("Erreur", "Erreur réseau ou base de données : " + ex.getMessage());
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
