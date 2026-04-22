package tn.esprit.services;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import tn.esprit.controllers.ConfigLoader;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AiReportService {

    private static final String API_KEY  = ConfigLoader.get("GEMINI_API_KEY");
    private static final String MODEL    = "gemini-2.5-flash";   // stable + disponible
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String GEMINI_URL = BASE_URL + MODEL + ":generateContent?key=" + API_KEY;

    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    // Timeout plus long car le prompt est volumineux
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final QuestionnaireService    questionnaireService    = new QuestionnaireService();
    private final EntiteCollecteService   entiteCollecteService   = new EntiteCollecteService();

    // ─────────────────────────────────────────────
    //  Point d'entrée principal
    // ─────────────────────────────────────────────
    public void generatePdfReport(List<RendezVous> rdvs, String filePath) throws Exception {

        if (rdvs == null || rdvs.isEmpty())
            throw new IllegalArgumentException("La liste des rendez-vous est vide.");
        if (filePath == null || filePath.isBlank())
            throw new IllegalArgumentException("Chemin PDF invalide.");

        // 1. Construire les données brutes
        String rawData = buildRawData(rdvs);

        // 2. Appel Gemini avec retry
        String aiText = callGeminiWithRetry(buildPrompt(rawData));

        // 3. Fallback si Gemini échoue
        if (aiText == null || aiText.isBlank()) {
            aiText = "Rapport généré sans IA (aucune réponse reçue).\n\n" + rawData;
        }

        // 4. Écriture PDF
        writePdf(filePath, aiText);
    }

    // ─────────────────────────────────────────────
    //  Construction des données brutes
    // ─────────────────────────────────────────────
    private String buildRawData(List<RendezVous> rdvs) {
        StringBuilder sb = new StringBuilder();
        int index = 1;

        for (RendezVous rdv : rdvs) {
            try {
                Questionnaire q = questionnaireService.getQuestionnaireById(rdv.getQuestionnaire_id());
                String entite   = entiteCollecteService.getEntiteById(rdv.getEntite_id()).getNom();

                sb.append("--- Rendez-vous #").append(index++).append(" ---\n");
                sb.append("Nom          : ").append(safe(q.getNom())).append('\n');
                sb.append("Prénom       : ").append(safe(q.getPrenom())).append('\n');
                sb.append("Âge          : ").append(safe(q.getAge())).append('\n');
                sb.append("Sexe         : ").append(safe(q.getSexe())).append('\n');
                sb.append("Poids        : ").append(safe(q.getPoids())).append(" kg\n");
                sb.append("Groupe sanguin: ").append(safe(q.getGroupeSanguin())).append('\n');
                sb.append("Autres infos : ").append(safe(q.getAutres())).append('\n');
                sb.append("Date         : ").append(safe(rdv.getDateDon())).append('\n');
                sb.append("Statut       : ").append(safe(rdv.getStatus())).append('\n');
                sb.append("Entité       : ").append(safe(entite)).append('\n');
                sb.append('\n');

            } catch (Exception e) {
                sb.append("--- Rendez-vous #").append(index++).append(" (erreur lecture) ---\n\n");
            }
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────
    //  Prompt Gemini
    // ─────────────────────────────────────────────
    private String buildPrompt(String rawData) {
        return """
                Tu es un assistant médical professionnel pour une plateforme de don de sang.
                Génère un rapport professionnel en français basé sur les données suivantes.

                Le rapport doit contenir :
                1. Un titre : "Rapport des Rendez-Vous — Don de Sang"
                2. Un résumé global (nombre total, répartition confirmés/annulés, groupes sanguins présents)
                3. Une liste structurée de chaque rendez-vous
                4. Une analyse courte (tendances, observations)
                5. Une conclusion formelle

                Règles :
                - Ton formel et professionnel
                - Répondre uniquement en français
                - Ne pas inventer de données absentes
                - Texte brut uniquement, pas de markdown

                Données :
                """ + rawData;
    }

    // ─────────────────────────────────────────────
    //  Appel Gemini avec retry automatique
    // ─────────────────────────────────────────────
    private String callGeminiWithRetry(String prompt) throws IOException {
        if (API_KEY == null || API_KEY.isBlank())
            throw new IOException("Clé API Gemini manquante dans config.properties.");

        int     maxRetries  = 3;
        int     waitSeconds = 5;
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("Gemini — tentative " + attempt + "/" + maxRetries);
                return callGemini(prompt);

            } catch (IOException e) {
                lastException = e;
                String msg = e.getMessage() != null ? e.getMessage() : "";

                boolean retryable = msg.contains("503")
                        || msg.contains("429")
                        || msg.contains("UNAVAILABLE")
                        || msg.contains("overloaded");

                if (!retryable || attempt == maxRetries) break;

                System.out.println("Gemini surchargé — attente " + waitSeconds + "s...");
                try { Thread.sleep(waitSeconds * 1000L); } catch (InterruptedException ignored) {}
                waitSeconds *= 2; // backoff exponentiel : 5s → 10s → 20s
            }
        }
        throw lastException != null ? lastException
                : new IOException("Gemini indisponible après " + maxRetries + " tentatives.");
    }

    // ─────────────────────────────────────────────
    //  Appel HTTP Gemini (une seule tentative)
    // ─────────────────────────────────────────────
    private String callGemini(String prompt) throws IOException {

        // Construire le JSON de la requête
        JSONObject part    = new JSONObject().put("text", prompt);
        JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
        JSONObject body    = new JSONObject()
                .put("contents", new JSONArray().put(content))
                .put("generationConfig", new JSONObject()
                        .put("temperature", 0.3)
                        .put("maxOutputTokens", 2048));

        RequestBody requestBody = RequestBody.create(body.toString(), JSON_TYPE);

        Request request = new Request.Builder()
                .url(GEMINI_URL)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            String responseStr = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new IOException("Gemini HTTP " + response.code() + " : " + responseStr);
            }

            return extractText(responseStr);
        }
    }

    // ─────────────────────────────────────────────
    //  Extraction du texte de la réponse JSON
    // ─────────────────────────────────────────────
    private String extractText(String responseBody) throws IOException {
        try {
            JSONObject json       = new JSONObject(responseBody);
            JSONArray  candidates = json.getJSONArray("candidates");
            JSONObject candidate  = candidates.getJSONObject(0);

            // Vérifier finishReason
            String finishReason = candidate.optString("finishReason", "");
            if ("SAFETY".equals(finishReason))
                throw new IOException("Gemini a bloqué la réponse pour des raisons de sécurité.");

            JSONArray parts = candidate
                    .getJSONObject("content")
                    .getJSONArray("parts");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                sb.append(parts.getJSONObject(i).optString("text", ""));
            }

            String result = sb.toString().trim();
            if (result.isEmpty())
                throw new IOException("Gemini a retourné un texte vide.");

            return result;

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Erreur parsing réponse Gemini : " + responseBody, e);
        }
    }

    // ─────────────────────────────────────────────
    //  Écriture PDF avec iText/OpenPDF
    // ─────────────────────────────────────────────
    private void writePdf(String filePath, String content) throws Exception {
        File file   = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        Document doc = new Document(PageSize.A4, 50, 50, 60, 60);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            PdfWriter.getInstance(doc, fos);
            doc.open();

            // Titre
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Rapport IA — Don de Sang", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            doc.add(title);

            // Séparateur
            doc.add(new Paragraph("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            doc.add(new Paragraph(" "));

            // Contenu généré par Gemini
            Font bodyFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
            for (String line : content.split("\n")) {
                Paragraph p = new Paragraph(line.isEmpty() ? " " : line, bodyFont);
                p.setSpacingAfter(2f);
                doc.add(p);
            }

        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    private String safe(Object v) {
        return v == null ? "Non renseigné" : v.toString().trim();
    }
}