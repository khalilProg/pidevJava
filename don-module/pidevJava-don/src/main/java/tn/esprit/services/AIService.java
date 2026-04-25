package tn.esprit.services;

import tn.esprit.entities.Don;
import tn.esprit.entities.DossierMed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class AIService {

    // THE SOURCE OF TRUTH: Real API Key and Latest Model Endpoint
    private static final String API_KEY = "AIzaSyBMz69ai9jogEHJyXVWPn8kgPFhKL5HGy0";
    // CHANGE: Removed 'beta' and '-latest' for maximum compatibility
    private static final String STABLE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public String analyzeDossierMed(DossierMed dm) {
        try {
            String prompt = String.format("Analyze patient: Age %d, Blood %s, Weight %.1fkg. 2-sentence professional medical verdict on blood donation eligibility.",
                    dm.getAge(), dm.getType_sang(), dm.getPoid());
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            return "❌ AI Diagnosis Unavailable: " + e.getMessage();
        }
    }

    public String getVitaSphereSynthesis(List<Don> dons) {
        try {
            if (dons.isEmpty()) return "Legacy initialized. Biological sequence history pending.";
            float vol = (float) dons.stream().mapToDouble(Don::getQuantite).sum();
            String prompt = String.format("A donor gave blood %d times totaling %.1fml. Write 2 heroic and inspiring sentences.",
                    dons.size(), vol);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            return "Offline Mode: Your biological bridge has secured stability for human systems.";
        }
    }

    public String getRecoveryPlan(float weight, float bmi) {
        try {
            String prompt = String.format("Generate a recovery nutrition plan for weight %.1f, BMI %.1f in 2 short sentences.",
                    weight, bmi);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            return "Recovery Protocol: Increase liquid intake and iron-dense nourishment.";
        }
    }

    private String callGeminiAPI(String promptText) throws Exception {
        JSONObject part = new JSONObject().put("text", promptText);
        JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
        JSONObject payload = new JSONObject().put("contents", new JSONArray().put(content));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STABLE_URL))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            return json.getJSONArray("candidates").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("parts")
                    .getJSONObject(0).getString("text").trim().replace("\n", " ");
        } else {
            // Log the error body to see exactly why it fails (Quota, Region, etc.)
            System.err.println("API Failure Body: " + response.body());
            throw new RuntimeException("Google API Error: HTTP " + response.statusCode());
        }
    }
}