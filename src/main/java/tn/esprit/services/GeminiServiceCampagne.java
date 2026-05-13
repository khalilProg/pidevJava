package tn.esprit.services;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Service d'appel à l'API Google Gemini pour les prédictions IA.
 */
public class GeminiServiceCampagne {
    private static final String API_KEY = " ";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + API_KEY;

    /**
     * Envoie un prompt à l'API Gemini et retourne la réponse texte.
     * @param prompt Le texte de la requête
     * @return La réponse de l'IA
     */
    public static String generateContent(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        con.setConnectTimeout(15000);
        con.setReadTimeout(30000);

        // Escape the prompt for JSON embedding
        String safePrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        String body = "{"
                + "\"contents\": [{"
                + "\"parts\": [{\"text\": \"" + safePrompt + "\"}]"
                + "}],"
                + "\"generationConfig\": {"
                + "\"temperature\": 0.4,"
                + "\"maxOutputTokens\": 4096"
                + "}"
                + "}";

        try (OutputStream os = con.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = con.getResponseCode();
        java.io.InputStream is = (status >= 200 && status < 300) ? con.getInputStream() : con.getErrorStream();

        Scanner scanner = new Scanner(is, StandardCharsets.UTF_8);
        StringBuilder response = new StringBuilder();
        while (scanner.hasNextLine()) {
            response.append(scanner.nextLine()).append("\n");
        }
        scanner.close();

        if (status < 200 || status >= 300) {
            throw new Exception("Gemini API error " + status + ": " + response);
        }

        // Parse the JSON response manually (avoid extra dependency)
        // Expected: {..., "candidates":[{"content":{"parts":[{"text":"..."}]}}]}
        String raw = response.toString();
        int textStart = raw.indexOf("\"text\":");
        if (textStart == -1) throw new Exception("Réponse inattendue de Gemini : " + raw);

        int quoteStart = raw.indexOf("\"", textStart + 7) + 1;
        int quoteEnd   = quoteStart;
        // Walk char by char to handle escaped quotes
        while (quoteEnd < raw.length()) {
            char ch = raw.charAt(quoteEnd);
            if (ch == '\\') { quoteEnd += 2; continue; }
            if (ch == '"')  { break; }
            quoteEnd++;
        }

        String extracted = raw.substring(quoteStart, quoteEnd);
        // Unescape common sequences
        extracted = extracted
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");

        return extracted;
    }
}
