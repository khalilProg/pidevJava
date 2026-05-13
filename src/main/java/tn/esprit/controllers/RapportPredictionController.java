package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Campagne;
import tn.esprit.services.CampagneService;
import tn.esprit.services.GeminiServiceCampagne;
import tn.esprit.tools.CampagnePDFGenerator;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;

/**
 * Contrôleur du dialog de rapport de prédiction IA des participants.
 */
public class RapportPredictionController {

    @FXML private Label lblTitreCampagne;
    @FXML private Label lblPeriode;
    @FXML private Label lblTypesSang;
    @FXML private Label lblStatut;
    @FXML private TextArea txtRapportIA;
    @FXML private Button btnExportPDF;
    @FXML private Button btnFermer;
    @FXML private VBox loadingBox;
    @FXML private VBox resultBox;

    private Campagne nouvelleCampagne;
    private String rapportTexte = "";

    /**
     * Point d'entrée : initialise le dialog avec la nouvelle campagne.
     */
    public void initData(Campagne campagne) {
        this.nouvelleCampagne = campagne;

        lblTitreCampagne.setText("📋 " + campagne.getTitre());
        lblPeriode.setText("📅 Du " + campagne.getDateDebut() + " au " + campagne.getDateFin());
        lblTypesSang.setText("🩸 Types : " + campagne.getTypeSang());
        lblStatut.setText("⏳ Analyse en cours...");

        loadingBox.setVisible(true);
        resultBox.setVisible(false);
        btnExportPDF.setDisable(true);

        // Appel asynchrone pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                String prompt = buildPrompt();
                String reponse = GeminiServiceCampagne.generateContent(prompt);
                rapportTexte = reponse;

                Platform.runLater(() -> {
                    txtRapportIA.setText(reponse);
                    lblStatut.setText("✅ Rapport généré avec succès !");
                    lblStatut.setStyle("-fx-text-fill: #2ecc71;");
                    loadingBox.setVisible(false);
                    resultBox.setVisible(true);
                    btnExportPDF.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    lblStatut.setText("❌ Erreur lors de l'appel à Gemini AI.");
                    lblStatut.setStyle("-fx-text-fill: #e74c3c;");
                    txtRapportIA.setText("Erreur : " + e.getMessage());
                    loadingBox.setVisible(false);
                    resultBox.setVisible(true);
                });
            }
        }).start();
    }

    /**
     * Construit le prompt contextuel enrichi pour Gemini.
     */
    private String buildPrompt() {
        StringBuilder historique = new StringBuilder();
        int totalParticipants = 0;
        int nbCampagnes = 0;
        int maxParticipants = 0;
        int minParticipants = Integer.MAX_VALUE;
        String bestCampagne = "";

        try {
            CampagneService cs = new CampagneService();
            List<Campagne> campagnes = cs.recuperer();

            for (Campagne c : campagnes) {
                if (c.getId() == nouvelleCampagne.getId()) continue;
                int nb = getNbQuestionnaires(c.getId());
                long duree = c.getDateDebut() != null && c.getDateFin() != null
                        ? java.time.temporal.ChronoUnit.DAYS.between(c.getDateDebut(), c.getDateFin()) : 0;
                String mois = c.getDateDebut() != null
                        ? c.getDateDebut().getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH) : "?";
                String saison = getSaison(c.getDateDebut());
                int nbEntites = (c.getEntiteDeCollectes() != null) ? c.getEntiteDeCollectes().size() : 0;
                double tauxJour = duree > 0 ? (double) nb / duree : 0;

                historique.append("- Campagne \"").append(c.getTitre()).append("\"\n");
                historique.append("  * Periode : ").append(c.getDateDebut()).append(" -> ").append(c.getDateFin())
                        .append(" (").append(duree).append(" jours, ").append(mois).append(", ").append(saison).append(")\n");
                historique.append("  * Types de sang : ").append(c.getTypeSang() != null ? c.getTypeSang() : "N/A").append("\n");
                historique.append("  * Entites de collecte : ").append(nbEntites).append("\n");
                historique.append("  * Participants (questionnaires) : ").append(nb).append("\n");
                historique.append("  * Taux moyen par jour : ").append(String.format("%.2f", tauxJour)).append(" participants/jour\n\n");

                totalParticipants += nb;
                nbCampagnes++;
                if (nb > maxParticipants) { maxParticipants = nb; bestCampagne = c.getTitre(); }
                if (nb < minParticipants) minParticipants = nb;
            }

        } catch (SQLException e) {
            historique.append("[Erreur chargement historique : ").append(e.getMessage()).append("]\n");
        }

        double moyenne = (nbCampagnes > 0) ? (double) totalParticipants / nbCampagnes : 0;
        String saisonNouvelle = getSaison(nouvelleCampagne.getDateDebut());
        String moisNouvelle = nouvelleCampagne.getDateDebut() != null
                ? nouvelleCampagne.getDateDebut().getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH) : "?";
        int nbEntitesNouvelle = (nouvelleCampagne.getEntiteDeCollectes() != null) ? nouvelleCampagne.getEntiteDeCollectes().size() : 0;

        return "ROLE : Tu es un analyste expert en sante publique, specialise dans les campagnes de don de sang en Tunisie. "
                + "Tu as acces a des donnees reelles issues d'un systeme de gestion de campagnes (BloodLink). "
                + "IMPORTANT : Reponds UNIQUEMENT en francais. N'utilise PAS de symboles markdown comme ** ou ##. "
                + "Utilise des separateurs textuels clairs (====, ----, numeros de sections) pour structurer ton rapport.\n\n"

                + "=== CONTEXTE STATISTIQUE GLOBAL ===\n"
                + "Nombre total de campagnes passees : " + nbCampagnes + "\n"
                + "Total participants cumules : " + totalParticipants + "\n"
                + "Moyenne de participants par campagne : " + String.format("%.1f", moyenne) + "\n"
                + "Record de participation : " + maxParticipants + " (campagne : " + bestCampagne + ")\n"
                + "Participation minimale enregistree : " + (minParticipants == Integer.MAX_VALUE ? 0 : minParticipants) + "\n\n"

                + "=== HISTORIQUE DETAILLE DES CAMPAGNES ===\n"
                + historique.toString()

                + "=== NOUVELLE CAMPAGNE A ANALYSER ===\n"
                + "Titre : " + nouvelleCampagne.getTitre() + "\n"
                + "Periode : " + nouvelleCampagne.getDateDebut() + " au " + nouvelleCampagne.getDateFin() + "\n"
                + "Duree : " + getDureeJours() + " jours\n"
                + "Mois de debut : " + moisNouvelle + "\n"
                + "Saison : " + saisonNouvelle + "\n"
                + "Types de sang cibles : " + nouvelleCampagne.getTypeSang() + "\n"
                + "Nombre d'entites de collecte : " + nbEntitesNouvelle + "\n\n"

                + "=== MISSION ===\n"
                + "Genere un rapport analytique complet, professionnel et precis en FRANCAIS avec les sections suivantes :\n\n"
                + "SECTION 1 - SYNTHESE STATISTIQUE\n"
                + "Analyse les donnees historiques : quelles campagnes ont le mieux performe ? Quels patterns ressortent par saison, duree, type de sang et nombre d'entites ?\n\n"
                + "SECTION 2 - PREDICTION DU NOMBRE DE PARTICIPANTS\n"
                + "Donne une fourchette basse et haute precise basee sur les donnees (pas de valeurs generiques). "
                + "Explique le raisonnement mathematique et contextuel derriere ta prediction. "
                + "Compare avec la moyenne historique et les campagnes similaires.\n\n"
                + "SECTION 3 - ANALYSE DES FACTEURS D'INFLUENCE\n"
                + "Analyse detaillee de : l'impact de la saison " + saisonNouvelle + " sur la participation, "
                + "l'impact de la duree de " + getDureeJours() + " jours, "
                + "l'attractivite des types de sang (" + nouvelleCampagne.getTypeSang() + ") dans le contexte tunisien, "
                + "et l'effet du nombre d'entites (" + nbEntitesNouvelle + ") sur la couverture geographique.\n\n"
                + "SECTION 4 - ALERTES ET RISQUES\n"
                + "Identifie les risques potentiels pour cette campagne (periode creuse, concurrence, faible duree, etc.)\n\n"
                + "SECTION 5 - RECOMMANDATIONS OPERATIONNELLES\n"
                + "Formule 5 recommandations concretes et actionnables pour maximiser la participation, "
                + "adaptees au contexte specifique de cette campagne.\n\n"
                + "SECTION 6 - NIVEAU DE CONFIANCE\n"
                + "Evalue le niveau de confiance de ta prediction (0% a 100%) avec justification basee sur la qualite et quantite des donnees historiques disponibles.\n\n"
                + "Sois precis, chiffre tes affirmations quand possible, et reste professionnel.";
    }

    private String getSaison(LocalDate date) {
        if (date == null) return "inconnue";
        int mois = date.getMonthValue();
        if (mois >= 3 && mois <= 5) return "Printemps";
        if (mois >= 6 && mois <= 8) return "Ete";
        if (mois >= 9 && mois <= 11) return "Automne";
        return "Hiver";
    }

    private int getNbQuestionnaires(int campagneId) {
        try {
            Connection cn = tn.esprit.tools.MyDatabase.getInstance().getCnx();
            PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM questionnaire WHERE campagne_id = ?");
            ps.setInt(1, campagneId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private long getDureeJours() {
        if (nouvelleCampagne.getDateDebut() == null || nouvelleCampagne.getDateFin() == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(nouvelleCampagne.getDateDebut(), nouvelleCampagne.getDateFin());
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }

    @FXML
    private void handleExportPDF() {
        Stage stage = (Stage) btnExportPDF.getScene().getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport de prédiction");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fc.setInitialFileName("Prediction_" + nouvelleCampagne.getTitre().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");
        File file = fc.showSaveDialog(stage);

        if (file != null) {
            try {
                genererPDFPrediction(file.getAbsolutePath());
                showAlert(javafx.scene.control.Alert.AlertType.INFORMATION, "Succès",
                        "Le rapport de prédiction a été exporté avec succès !");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Erreur",
                        "Impossible de générer le PDF : " + e.getMessage());
            }
        }
    }

    private void genererPDFPrediction(String path) throws Exception {
        com.itextpdf.text.Document doc = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
        com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(path));
        doc.open();

        // Fonts
        com.itextpdf.text.Font fTitre = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA_BOLD, 20, com.itextpdf.text.BaseColor.BLACK);
        com.itextpdf.text.Font fSub = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA, 12, com.itextpdf.text.BaseColor.GRAY);
        com.itextpdf.text.Font fBody = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA, 11, com.itextpdf.text.BaseColor.BLACK);
        com.itextpdf.text.Font fLabel = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, new com.itextpdf.text.BaseColor(231, 76, 60));

        // Header
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                "Rapport de Prédiction IA — BloodLink", fTitre);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        doc.add(title);

        com.itextpdf.text.Paragraph sub = new com.itextpdf.text.Paragraph(
                "Généré le : " + java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fSub);
        sub.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        sub.setSpacingAfter(20);
        doc.add(sub);

        // Campagne info box
        com.itextpdf.text.pdf.PdfPTable infoTable = new com.itextpdf.text.pdf.PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(20);

        addInfoRow(infoTable, "Campagne", nouvelleCampagne.getTitre(), fLabel, fBody);
        addInfoRow(infoTable, "Période", nouvelleCampagne.getDateDebut() + " → " + nouvelleCampagne.getDateFin(), fLabel, fBody);
        addInfoRow(infoTable, "Types de sang", nouvelleCampagne.getTypeSang(), fLabel, fBody);
        addInfoRow(infoTable, "Durée", getDureeJours() + " jours", fLabel, fBody);
        doc.add(infoTable);

        // Separator
        com.itextpdf.text.Chunk separator = new com.itextpdf.text.Chunk(
                new com.itextpdf.text.pdf.draw.LineSeparator(1f, 100f,
                        new com.itextpdf.text.BaseColor(231, 76, 60),
                        com.itextpdf.text.Element.ALIGN_CENTER, -2));
        doc.add(new com.itextpdf.text.Paragraph(separator));

        // AI Report content
        com.itextpdf.text.Paragraph reportTitle = new com.itextpdf.text.Paragraph("\nAnalyse et Prédiction (Gemini AI)\n", fLabel);
        reportTitle.setSpacingAfter(10);
        doc.add(reportTitle);

        com.itextpdf.text.Paragraph content = new com.itextpdf.text.Paragraph(rapportTexte, fBody);
        content.setLeading(16);
        doc.add(content);

        doc.close();
    }

    private void addInfoRow(com.itextpdf.text.pdf.PdfPTable table, String label, String value,
                            com.itextpdf.text.Font fLabel, com.itextpdf.text.Font fBody) {
        com.itextpdf.text.pdf.PdfPCell c1 = new com.itextpdf.text.pdf.PdfPCell(
                new com.itextpdf.text.Phrase(label, fLabel));
        c1.setPadding(8);
        c1.setBackgroundColor(new com.itextpdf.text.BaseColor(255, 245, 245));
        c1.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);

        com.itextpdf.text.pdf.PdfPCell c2 = new com.itextpdf.text.pdf.PdfPCell(
                new com.itextpdf.text.Phrase(value != null ? value : "", fBody));
        c2.setPadding(8);
        c2.setBorderColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);

        table.addCell(c1);
        table.addCell(c2);
    }

    @FXML
    private void handleFermer() {
        ((Stage) btnFermer.getScene().getWindow()).close();
    }

    private void showAlert(javafx.scene.control.Alert.AlertType type, String titre, String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
