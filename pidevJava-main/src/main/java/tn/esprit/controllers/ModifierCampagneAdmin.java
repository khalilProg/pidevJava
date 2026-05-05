package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import tn.esprit.entities.Campagne;
import tn.esprit.services.CampagneService;

import javafx.scene.layout.FlowPane;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.EntiteCollecteService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ModifierCampagneAdmin {

    @FXML private TextField titreInput;
    @FXML private TextArea descInput;
    @FXML private DatePicker dateDebutInput;
    @FXML private DatePicker dateFinInput;
    @FXML private Label titreError;
    @FXML private Label descError;
    @FXML private Label dateDebutError;
    @FXML private Label dateFinError;
    @FXML private Label typesError;

    @FXML private ToggleButton tglAplus, tglAminus, tglBplus, tglBminus, tglABplus, tglABminus, tglOplus, tglOminus;

    @FXML private FlowPane entitesFlowPane;
    @FXML private Label entitesError;

    private CampagneService cs = new CampagneService();
    private Campagne currentCampagne;

    @FXML
    public void initialize() {
        try {
            List<EntiteDeCollecte> entites = new EntiteCollecteService().recuperer();
            for (EntiteDeCollecte e : entites) {
                ToggleButton tb = new ToggleButton(e.getNom());
                tb.getStyleClass().add("toggle-blood-type");
                tb.setUserData(e);
                entitesFlowPane.getChildren().add(tb);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setCampagneToEdit(Campagne c) {
        this.currentCampagne = c;
        titreInput.setText(c.getTitre());
        descInput.setText(c.getDescription());
        dateDebutInput.setValue(c.getDateDebut());
        dateFinInput.setValue(c.getDateFin());

        String types = c.getTypeSang() != null ? c.getTypeSang() : "";
        if (types.contains("A+")) tglAplus.setSelected(true);
        if (types.contains("A-")) tglAminus.setSelected(true);
        if (types.contains("B+")) tglBplus.setSelected(true);
        if (types.contains("B-")) tglBminus.setSelected(true);
        if (types.contains("AB+")) tglABplus.setSelected(true);
        if (types.contains("AB-")) tglABminus.setSelected(true);
        if (types.contains("O+")) tglOplus.setSelected(true);
        if (types.contains("O-")) tglOminus.setSelected(true);

        if (c.getEntiteDeCollectes() != null) {
            for (Node node : entitesFlowPane.getChildren()) {
                if (node instanceof ToggleButton) {
                    ToggleButton tb = (ToggleButton) node;
                    EntiteDeCollecte eNode = (EntiteDeCollecte) tb.getUserData();
                    for (EntiteDeCollecte existE : c.getEntiteDeCollectes()) {
                        if (existE.getId() == eNode.getId()) {
                            tb.setSelected(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        String titre = titreInput.getText() != null ? titreInput.getText().trim() : "";
        String desc = descInput.getText() != null ? descInput.getText().trim() : "";
        LocalDate debut = dateDebutInput.getValue();
        LocalDate fin = dateFinInput.getValue();

        List<String> selectedTypes = new ArrayList<>();
        if (tglAplus.isSelected()) selectedTypes.add("A+");
        if (tglAminus.isSelected()) selectedTypes.add("A-");
        if (tglBplus.isSelected()) selectedTypes.add("B+");
        if (tglBminus.isSelected()) selectedTypes.add("B-");
        if (tglABplus.isSelected()) selectedTypes.add("AB+");
        if (tglABminus.isSelected()) selectedTypes.add("AB-");
        if (tglOplus.isSelected()) selectedTypes.add("O+");
        if (tglOminus.isSelected()) selectedTypes.add("O-");

        List<EntiteDeCollecte> selectedEntites = new ArrayList<>();
        for (Node node : entitesFlowPane.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton tb = (ToggleButton) node;
                if (tb.isSelected()) {
                    selectedEntites.add((EntiteDeCollecte) tb.getUserData());
                }
            }
        }

        boolean hasError = clearAndValidateCampagne(titre, desc, debut, fin, selectedTypes, selectedEntites);
        if (hasError) return;

        String typeSangCombined = String.join(", ", selectedTypes);

        try {
            // Overlapping dates strict check for selected Entités
            for (Campagne existant : cs.recuperer()) {
                if (existant.getId() == currentCampagne.getId()) continue; // Ignore itself
                if (existant.getDateDebut() == null || existant.getDateFin() == null) continue;
                
                boolean datesOverlap = !(fin.isBefore(existant.getDateDebut()) || debut.isAfter(existant.getDateFin()));
                if (datesOverlap && existant.getEntiteDeCollectes() != null) {
                    for (EntiteDeCollecte newE : selectedEntites) {
                        for (EntiteDeCollecte existE : existant.getEntiteDeCollectes()) {
                            if (newE.getId() == existE.getId()) {
                                showFieldError(entitesError, "L'entité '" + newE.getNom() + "' est déjà occupée par une campagne à ces dates.");
                                return;
                            }
                        }
                    }
                }
            }

            // Update logic
            currentCampagne.setTitre(titre);
            currentCampagne.setDescription(desc);
            currentCampagne.setDateDebut(debut);
            currentCampagne.setDateFin(fin);
            currentCampagne.setTypeSang(typeSangCombined);
            currentCampagne.setEntiteDeCollectes(selectedEntites);

            cs.modifier(currentCampagne);
            navigateTo(event, "/ListeCampagnesAdmin.fxml");
        } catch (IllegalArgumentException ex) {
            showFieldError(titreError, ex.getMessage());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private boolean clearAndValidateCampagne(String titre, String desc, LocalDate debut, LocalDate fin, List<String> types, List<EntiteDeCollecte> entites) {
        resetErrorLabel(titreError, "Donnez un titre accrocheur à votre campagne.");
        resetErrorLabel(descError, "Décrivez les objectifs et les détails de la campagne.");
        resetErrorLabel(dateDebutError, "Quand commence la campagne ?");
        resetErrorLabel(dateFinError, "Quand se termine la campagne ?");
        resetErrorLabel(typesError, "Sélectionnez les groupes sanguins ciblés.");
        resetErrorLabel(entitesError, "Sélectionnez une ou plusieurs entités pour la campagne.");

        boolean hasError = false;

        if (titre.isEmpty()) { showFieldError(titreError, "Le titre est obligatoire."); hasError = true; }
        if (desc.isEmpty()) { showFieldError(descError, "La description est obligatoire."); hasError = true; }
        
        if (debut == null) { 
            showFieldError(dateDebutError, "La date de début est obligatoire."); hasError = true; 
        } else if (debut.isBefore(LocalDate.now()) && !debut.isEqual(currentCampagne.getDateDebut())) {
            showFieldError(dateDebutError, "La date de début ne peut pas être au passé."); hasError = true;
        }

        if (fin == null) { 
            showFieldError(dateFinError, "La date de fin est obligatoire."); hasError = true; 
        } else if (debut != null && fin.isBefore(debut)) {
            showFieldError(dateFinError, "La date de fin doit être postérieure à la date de début."); hasError = true;
        }

        if (types.isEmpty()) { showFieldError(typesError, "Sélectionnez au moins un type de sang."); hasError = true; }
        if (entites.isEmpty()) { showFieldError(entitesError, "Sélectionnez au moins une entité."); hasError = true; }

        return hasError;
    }

    private void showFieldError(Label label, String message) {
        label.setText("⚠ " + message);
        label.setStyle("-fx-text-fill: #ff4d4d; -fx-font-size: 11px;");
    }

    private void resetErrorLabel(Label label, String defaultMessage) {
        label.setText(defaultMessage);
        label.setStyle("-fx-text-fill: -muted; -fx-font-size: 11px;");
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        navigateTo(event, "/ListeCampagnesAdmin.fxml");
    }

    private void navigateTo(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
