package tn.esprit.services;

import tn.esprit.controllers.ConfigLoader;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AiReportService {

    private static final String API_KEY = ConfigLoader.get("GEMINI_API_KEY");
    private static final String GEMINI_MODEL = "gemini-2.5-flash";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/" + GEMINI_MODEL + ":generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final QuestionnaireService qsService = new QuestionnaireService();

    public void generatePdfReport(List<RendezVous> rdvs, String filePath) throws Exception {
        if (rdvs == null || rdvs.isEmpty()) throw new IllegalArgumentException("Liste vide !");
        if (filePath == null || filePath.isBlank()) throw new IllegalArgumentException("Chemin PDF invalide !");

        // Build raw data
        StringBuilder data = new StringBuilder();
        int index = 1;
        for (RendezVous rdv : rdvs) {
            Questionnaire q = qsService.getQuestionnaireById(rdv.getQuestionnaire_id());
            String nom = (q != null) ? q.getNom() : "Inconnu";
            String prenom = (q != null) ? q.getPrenom() : "";
            String age = (q != null) ? String.valueOf(q.getAge()) : "N/A";
            String sexe = (q != null) ? q.getSexe() : "N/A";
            String poids = (q != null) ? String.valueOf(q.getPoids()) : "N/A";
            String groupe = (q != null) ? q.getGroupeSanguin() : "N/A";
            String autres = (q != null) ? q.getAutres() : "Aucune";

            data.append("--- RDV #").append(index++).append(" ---\n")
                    .append("Nom : ").append(nom).append("\n")
                    .append("Prénom : ").append(prenom).append("\n")
                    .append("Âge : ").append(age).append("\n")
                    .append("Sexe : ").append(sexe).append("\n")
                    .append("Poids : ").append(poids).append("\n")
                    .append("Groupe sanguin : ").append(groupe).append("\n")
                    .append("Autres infos : ").append(autres).append("\n")
                    .append("Date : ").append(rdv.getDateDon()).append("\n")
                    .append("Statut : ").append(rdv.getStatus()).append("\n\n");
        }

        // Build prompt for AI
        String prompt = """
                Tu es un expert en analyse médicale pour une plateforme de don de sang.
                Génère un rapport professionnel et structuré en français basé sur les données de rendez-vous fournies.
                
                STRUCTURE DU RAPPORT :
                1. RÉSUMÉ ANALYTIQUE : Un aperçu global de la situation (nombre de donneurs, groupes sanguins représentés).
                2. ANALYSE DES DONNEURS : Points saillants sur les profils (âge moyen, poids, contre-indications potentielles).
                3. RECOMMANDATIONS : Conseils pour l'organisation des prochaines collectes.
                4. CONCLUSION : Un mot de fin professionnel.
                
                IMPORTANT : Ne retourne QUE le texte du rapport, sans introduction méta (ex: "Voici le rapport...").
                
                DONNÉES :
                """ + data;

        // Calculate KPIs for the summary box
        int totalRdvs = rdvs.size();
        double totalAge = 0;
        java.util.Set<String> groups = new java.util.HashSet<>();
        int countWithAge = 0;

        for (RendezVous rdv : rdvs) {
            Questionnaire q = qsService.getQuestionnaireById(rdv.getQuestionnaire_id());
            if (q != null) {
                if (q.getAge() > 0) {
                    totalAge += q.getAge();
                    countWithAge++;
                }
                if (q.getGroupeSanguin() != null && !q.getGroupeSanguin().isBlank()) {
                    groups.add(q.getGroupeSanguin());
                }
            }
        }
        double avgAge = countWithAge > 0 ? totalAge / countWithAge : 0;
        String groupList = groups.isEmpty() ? "N/A" : String.join(" / ", groups);

        // Call Gemini API
        String aiText = callGemini(prompt);

        // Write PDF locally with KPIs
        writePdf(filePath, aiText, totalRdvs, avgAge, groupList);
    }

    private String callGemini(String text) throws Exception {
        JSONObject body = new JSONObject();
        
        // Structure for Gemini generateContent: { "contents": [{ "parts": [{ "text": "..." }] }] }
        JSONObject part = new JSONObject().put("text", text);
        JSONArray parts = new JSONArray().put(part);
        JSONObject content = new JSONObject().put("parts", parts);
        JSONArray contents = new JSONArray().put(content);
        
        body.put("contents", contents);

        RequestBody requestBody = RequestBody.create(body.toString(), JSON);

        Request request = new Request.Builder()
                .url(GEMINI_URL + "?key=" + API_KEY)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String respStr = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful())
                throw new Exception("Gemini Error (HTTP " + response.code() + "): " + respStr);

            JSONObject json = new JSONObject(respStr);
            JSONArray candidates = json.getJSONArray("candidates");
            return candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim();
        }
    }
    private void writePdf(String filePath, String content, int count, double avgAge, String groups) throws Exception {
        File file = new File(filePath);
        if (file.getParentFile() != null) file.getParentFile().mkdirs();

        com.lowagie.text.Document doc = new com.lowagie.text.Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // Colors & Fonts
        java.awt.Color bloodRed = new java.awt.Color(200, 0, 0);
        java.awt.Color lightGray = new java.awt.Color(150, 150, 150);
        java.awt.Color boxBg = new java.awt.Color(255, 245, 245);
        
        Font brandFont = new Font(Font.HELVETICA, 28, Font.BOLD, bloodRed);
        Font headerSmallFont = new Font(Font.HELVETICA, 9, Font.BOLD, java.awt.Color.DARK_GRAY);
        Font dateFont = new Font(Font.HELVETICA, 8, Font.NORMAL, lightGray);
        Font kpiValueFont = new Font(Font.HELVETICA, 18, Font.BOLD, bloodRed);
        Font kpiLabelFont = new Font(Font.HELVETICA, 8, Font.BOLD, lightGray);
        Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL, java.awt.Color.BLACK);
        Font sectionHeaderFont = new Font(Font.HELVETICA, 10, Font.BOLD, java.awt.Color.DARK_GRAY);
        Font footerFont = new Font(Font.HELVETICA, 8, Font.NORMAL, new java.awt.Color(200, 200, 200));

        // 1. TOP HEADER (BLOODLINK | NOTE STRATÉGIQUE)
        com.lowagie.text.pdf.PdfPTable headerTable = new com.lowagie.text.pdf.PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 1});

        com.lowagie.text.pdf.PdfPCell leftCell = new com.lowagie.text.pdf.PdfPCell(new Paragraph("BLOODLINK", brandFont));
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        headerTable.addCell(leftCell);

        Paragraph rightContent = new Paragraph();
        rightContent.add(new Chunk("NOTE STRATÉGIQUE AI\n", headerSmallFont));
        rightContent.add(new Chunk("Généré le: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), dateFont));
        com.lowagie.text.pdf.PdfPCell rightCell = new com.lowagie.text.pdf.PdfPCell(rightContent);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        headerTable.addCell(rightCell);
        
        doc.add(headerTable);

        // Red line
        com.lowagie.text.pdf.PdfPTable lineTable = new com.lowagie.text.pdf.PdfPTable(1);
        lineTable.setWidthPercentage(100);
        com.lowagie.text.pdf.PdfPCell lineCell = new com.lowagie.text.pdf.PdfPCell();
        lineCell.setBorder(Rectangle.BOTTOM);
        lineCell.setBorderWidthBottom(2f);
        lineCell.setBorderColorBottom(bloodRed);
        lineCell.setFixedHeight(10f);
        lineTable.addCell(lineCell);
        doc.add(lineTable);
        doc.add(new Paragraph("\n"));

        // 2. KPI SUMMARY BOX
        com.lowagie.text.pdf.PdfPTable kpiTable = new com.lowagie.text.pdf.PdfPTable(3);
        kpiTable.setWidthPercentage(100);
        kpiTable.setSpacingBefore(10f);
        kpiTable.setSpacingAfter(20f);

        addKpiCell(kpiTable, String.valueOf(count), "RENDEZ-VOUS", kpiValueFont, kpiLabelFont, boxBg, bloodRed);
        addKpiCell(kpiTable, String.format("%.1f", avgAge), "ÂGE MOYEN", kpiValueFont, kpiLabelFont, boxBg, bloodRed);
        addKpiCell(kpiTable, groups, "GROUPES", kpiValueFont, kpiLabelFont, boxBg, bloodRed);
        
        doc.add(kpiTable);

        // 3. MAIN CONTENT
        doc.add(new Paragraph("En tant qu'expert BloodLink, voici mon analyse détaillée des données fournies :\n\n", bodyFont));

        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                doc.add(new Paragraph("\n"));
                continue;
            }

            Paragraph p = new Paragraph(line.replace("**", "").replace("#", "").trim(), bodyFont);
            
            // Highlight section headers
            if (line.toUpperCase().equals(line) && line.length() > 5 || line.contains(":") && line.length() < 50) {
                p.setFont(sectionHeaderFont);
                p.setSpacingBefore(8f);
            }
            
            p.setAlignment(Element.ALIGN_JUSTIFIED);
            doc.add(p);
        }

        // 4. FOOTER
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("────────────────────────────────────────────────────────────────────────", footerFont));
        Paragraph footer = new Paragraph("BloodLink AI Expert System — Document Confidentiel — Direction des Opérations", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
    }

    private void addKpiCell(com.lowagie.text.pdf.PdfPTable table, String value, String label, Font vFont, Font lFont, java.awt.Color bg, java.awt.Color border) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new java.awt.Color(255, 200, 200));
        cell.setPadding(15f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_CENTER);
        p.add(new Chunk(value + "\n", vFont));
        p.add(new Chunk(label, lFont));
        cell.addElement(p);
        
        table.addCell(cell);
    }
}
