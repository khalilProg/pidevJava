package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import tn.esprit.entities.*;
import tn.esprit.services.EntiteCollecteService;
import tn.esprit.services.QuestionnaireService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjouterQuestionnaire {

    private Campagne campagne; // el selected campagne
    @FXML private TextField age;
    @FXML private ComboBox<String> sexe;
    @FXML private TextField poids;
    @FXML private TextArea autres;
    @FXML private Button continuer;
    @FXML private Button annuler;
    @FXML private Text ageError;
    @FXML private Text sexeError;
    @FXML private Text poidsError;
    User u = new User(9,"chaffai", "yassine", "yassinechaffai4@gmail.com");
    private Client currentClient = new Client(1, "O+", LocalDate.of(2023, 1, 1), u);
    private Client currentClient1 = new Client(2, "A-", LocalDate.of(2003, 10, 17), u);


    public void setCampagne(Campagne c) {
        this.campagne = c;
        System.out.println("Selected campaign: " + c.getTitre());
    }
    @FXML public void handleContinuer(ActionEvent event) throws IOException, SQLException {
        boolean valid = true;

        // reset lel errors
        ageError.setVisible(false);
        sexeError.setVisible(false);
        poidsError.setVisible(false);
        //age check
        if (age.getText().isEmpty()) {
            ageError.setText("L'âge ne peut pas être vide");
            ageError.setVisible(true);
            valid = false;
        } else {
                int ageValue = Integer.parseInt(age.getText());
                if (ageValue < 18 || ageValue > 70) {
                    ageError.setText("L'âge doit être entre 18 et 70 ans");
                    ageError.setVisible(true);
                    valid = false;
                }
        }

        // sexe check
        if (sexe.getValue() == null || sexe.getValue().isEmpty()) {
            sexeError.setText("Le sexe ne peut pas être vide");
            sexeError.setVisible(true);
            valid = false;
        }

        // poids check
        if (poids.getText().isEmpty()) {
            poidsError.setText("Le poids ne peut pas être vide");
            poidsError.setVisible(true);
            valid = false;
        } else {
                double poidsValue = Double.parseDouble(poids.getText());
                if (poidsValue < 50 ||  poidsValue > 100) {
                    poidsError.setText("Le poids doit être entre 50 et 100 ans");
                    poidsError.setVisible(true);
                    valid = false;
                }
        }

        if (!valid) return; // stop itha validation fails

        else {
            try{
            String nom = currentClient.getUser().getNom();
            String prenom = currentClient.getUser().getPrenom();
            String typeSang = currentClient.getTypeSang();
            int clientId = currentClient.getId();
            int campagneId = campagne.getId();
            Questionnaire q = new Questionnaire(nom, prenom, Integer.parseInt(age.getText()), sexe.getValue(), Double.parseDouble(poids.getText()), autres.getText(), clientId, campagneId, java.time.LocalDateTime.now(), typeSang);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezVous.fxml"));
            Parent root = loader.load();
            AjouterRendezVous rdvController = loader.getController();
            rdvController.setCampagne(this.campagne);
            // Fetch entities from service
            List<EntiteDeCollecte> entities = new EntiteCollecteService().getByCampagneId(campagne.getId());
            rdvController.setEntities(entities);
            rdvController.setQuestionnaire(q);
            continuer.getScene().setRoot(root);

} catch (IOException ex) {
    ex.printStackTrace();
}

        }
    }
    @FXML public void handleAnnuler(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeCampagnes.fxml"));
        Parent root = loader.load();
        annuler.getScene().setRoot(root);
    }
}
