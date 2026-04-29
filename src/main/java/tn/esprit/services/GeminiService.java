package tn.esprit.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiService {
   
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + API_KEY;

    public String predireBesoins(String stocks, String demandes) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // 1. On prépare le texte du prompt
        String promptText = "Tu es un expert en gestion de banque de sang. " +
            "Voici l'état actuel des STOCKS : " + stocks + ". " +
            "Voici les dernières DEMANDES reçues : " + demandes + ". " +
            "Analyse les risques de pénurie et prédis les besoins futurs.";

        // 2. On construit l'objet JSON proprement avec GSON
        JsonObject root = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();

        textPart.addProperty("text", promptText);
        parts.add(textPart);
        content.add("parts", parts);
        contents.add(content);
        root.add("contents", contents);

        // 3. Envoi de la requête avec root.toString()
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(root.toString()))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 4. Parsing sécurisé
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

        if (jsonResponse.has("candidates")) {
            return jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject()
                .getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject()
                .get("text").getAsString();
        } else {
            // C'est ici que tu verras l'erreur réelle de Google (ex: INVALID_ARGUMENT)
            System.err.println("Réponse brute de l'API : " + response.body());
            return "Erreur : L'API n'a pas renvoyé de résultats. Vérifie la console.";
        }
    }
}
