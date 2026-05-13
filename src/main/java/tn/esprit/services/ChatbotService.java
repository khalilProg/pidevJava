package tn.esprit.services;

import org.json.JSONArray;
import org.json.JSONObject;
import tn.esprit.entities.Banque;
import tn.esprit.entities.Client;
import tn.esprit.entities.Commande;
import tn.esprit.entities.Stock;
import tn.esprit.entities.User;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatbotService {

    private static final Pattern BLOOD_TYPE_PATTERN = Pattern.compile("(?<![A-Z])(AB|A|B|O)\\s*([+-])(?![A-Z+-])", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+\\b");
    private static final Properties LOCAL_CONFIG = loadLocalConfig();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .build();
    private final StockService stockService = new StockService();
    private final BanqueService banqueService = new BanqueService();
    private final CommandeService commandeService = new CommandeService();
    private final ClientService clientService = new ClientService();

    public String chat(String userMessage, List<ChatMessage> history) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Envoyez-moi une question sur les banques disponibles, le statut d'une commande ou la preparation au don.";
        }

        String apiKey = getGeminiApiKey();
        if (apiKey.isBlank() || "your_google_gemini_api_key_here".equals(apiKey)) {
            return answerLocally(userMessage);
        }

        try {
            return askGemini(apiKey, userMessage, history == null ? List.of() : history);
        } catch (Exception e) {
            System.err.println("Chatbot Gemini error: " + e.getMessage());
            return answerLocally(userMessage);
        }
    }

    private String askGemini(String apiKey, String userMessage, List<ChatMessage> history)
            throws IOException, InterruptedException {
        JSONArray contents = buildContents(userMessage, history);
        JSONObject payload = new JSONObject()
                .put("contents", contents)
                .put("tools", buildTools())
                .put("generationConfig", new JSONObject().put("temperature", 0.2));

        JSONObject result = postGemini(apiKey, payload);
        JSONObject functionCall = extractFunctionCall(result);

        if (functionCall != null) {
            String name = functionCall.optString("name");
            JSONObject args = functionCall.optJSONObject("args");
            JSONObject toolResult = handleToolCall(name, args == null ? new JSONObject() : args);

            JSONObject modelContent = result.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content");
            contents.put(modelContent);
            contents.put(new JSONObject()
                    .put("role", "user")
                    .put("parts", new JSONArray().put(new JSONObject()
                            .put("functionResponse", new JSONObject()
                                    .put("name", name)
                                    .put("response", new JSONObject().put("result", toolResult))))));

            JSONObject secondPayload = new JSONObject()
                    .put("contents", contents)
                    .put("tools", buildTools())
                    .put("generationConfig", new JSONObject().put("temperature", 0.2));
            return extractText(postGemini(apiKey, secondPayload));
        }

        return extractText(result);
    }

    private JSONArray buildContents(String userMessage, List<ChatMessage> history) {
        JSONArray contents = new JSONArray();
        contents.put(content("user", "You are a helpful blood donation assistant for BloodLink. Answer concisely in the same language as the user. Use tools to fetch real data about banks and commandes."));
        contents.put(content("model", "Understood. I will help with BloodLink questions using the available tools."));

        for (ChatMessage message : history) {
            if (message == null || message.text() == null || message.text().isBlank()) {
                continue;
            }
            contents.put(content("user".equalsIgnoreCase(message.role()) ? "user" : "model", message.text()));
        }

        contents.put(content("user", userMessage));
        return contents;
    }

    private JSONObject content(String role, String text) {
        return new JSONObject()
                .put("role", role)
                .put("parts", new JSONArray().put(new JSONObject().put("text", text)));
    }

    private JSONArray buildTools() {
        JSONArray declarations = new JSONArray();
        declarations.put(functionDeclaration(
                "get_available_banks",
                "Get a list of blood banks that have a specific blood type available.",
                new JSONObject()
                        .put("blood_type", new JSONObject()
                                .put("type", "STRING")
                                .put("description", "The blood type, e.g. A+, O-, B+")),
                new JSONArray().put("blood_type")));
        declarations.put(functionDeclaration(
                "get_commande_status",
                "Get the status of a specific commande by its ID or reference.",
                new JSONObject()
                        .put("commande_id", new JSONObject()
                                .put("type", "STRING")
                                .put("description", "The ID or reference of the commande.")),
                new JSONArray().put("commande_id")));
        declarations.put(functionDeclaration(
                "get_donation_preparation",
                "Get FAQ instructions on how to prepare for a blood donation.",
                new JSONObject(),
                new JSONArray()));

        return new JSONArray().put(new JSONObject().put("function_declarations", declarations));
    }

    private JSONObject functionDeclaration(String name, String description, JSONObject properties, JSONArray required) {
        return new JSONObject()
                .put("name", name)
                .put("description", description)
                .put("parameters", new JSONObject()
                        .put("type", "OBJECT")
                        .put("properties", properties)
                        .put("required", required));
    }

    private JSONObject postGemini(String apiKey, JSONObject payload) throws IOException, InterruptedException {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(25))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Gemini returned HTTP " + response.statusCode() + ": " + response.body());
        }
        return new JSONObject(response.body());
    }

    private JSONObject extractFunctionCall(JSONObject result) {
        JSONArray parts = result.optJSONArray("candidates") == null
                ? null
                : result.optJSONArray("candidates")
                        .optJSONObject(0)
                        .optJSONObject("content")
                        .optJSONArray("parts");
        if (parts == null) {
            return null;
        }
        for (int i = 0; i < parts.length(); i++) {
            JSONObject call = parts.optJSONObject(i).optJSONObject("functionCall");
            if (call != null) {
                return call;
            }
        }
        return null;
    }

    private String extractText(JSONObject result) {
        JSONArray candidates = result.optJSONArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return "Je ne peux pas repondre pour le moment.";
        }
        JSONArray parts = candidates.optJSONObject(0).optJSONObject("content").optJSONArray("parts");
        if (parts == null || parts.isEmpty()) {
            return "Je ne peux pas repondre pour le moment.";
        }
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < parts.length(); i++) {
            String partText = parts.optJSONObject(i).optString("text");
            if (!partText.isBlank()) {
                if (!text.isEmpty()) {
                    text.append("\n");
                }
                text.append(partText);
            }
        }
        return text.isEmpty() ? "Je ne peux pas repondre pour le moment." : text.toString();
    }

    private JSONObject handleToolCall(String name, JSONObject args) {
        return switch (name) {
            case "get_available_banks" -> getAvailableBanks(args.optString("blood_type"));
            case "get_commande_status" -> getCommandeStatus(args.optString("commande_id"));
            case "get_donation_preparation" -> getDonationPreparation();
            default -> new JSONObject().put("error", "Unknown tool call");
        };
    }

    private String answerLocally(String userMessage) {
        String normalized = normalize(userMessage);
        String bloodType = extractBloodType(userMessage);

        if (bloodType != null && containsAny(normalized, "bank", "banque", "disponible", "available", "stock", "trouve")) {
            return banksToText(getAvailableBanks(bloodType));
        }

        if (containsAny(normalized, "commande", "order", "status", "statut", "reference", "suivi")) {
            String id = extractFirstNumber(userMessage);
            if (id != null) {
                return commandeStatusToText(getCommandeStatus(id));
            }
            return "Donnez-moi le numero ou la reference de la commande, par exemple: statut commande 23.";
        }

        if (containsAny(normalized, "donation", "don", "donner", "preparer", "preparation", "avant")) {
            return donationPreparationToText();
        }

        if (bloodType != null) {
            return banksToText(getAvailableBanks(bloodType));
        }

        return "Bonjour! Je peux vous aider a trouver une banque avec un type sanguin disponible, verifier le statut d'une commande, ou vous expliquer comment vous preparer pour un don.";
    }

    private JSONObject getAvailableBanks(String bloodType) {
        String normalizedBloodType = normalizeBloodType(bloodType);
        JSONArray banks = new JSONArray();
        if (normalizedBloodType.isBlank()) {
            return new JSONObject().put("message", "Type sanguin manquant.");
        }

        try {
            for (Stock stock : stockService.recuperer()) {
                if (stock.getQuantite() <= 0
                        || stock.getTypeSang() == null
                        || !normalizedBloodType.equalsIgnoreCase(stock.getTypeSang().trim())
                        || stock.getTypeOrg() == null
                        || !"banque".equalsIgnoreCase(stock.getTypeOrg().trim())) {
                    continue;
                }

                Banque banque = banqueService.getById(stock.getTypeOrgid());
                if (banque != null) {
                    banks.put("Banque: " + banque.getNom()
                            + " (Telephone: " + banque.getTelephone() + ") a "
                            + stock.getQuantite() + " unite(s) de " + normalizedBloodType);
                }
            }
        } catch (SQLException e) {
            return new JSONObject().put("message", "Erreur lors de la lecture des stocks: " + e.getMessage());
        }

        if (banks.isEmpty()) {
            return new JSONObject().put("message", "Aucune banque n'a actuellement du " + normalizedBloodType + " disponible.");
        }
        return new JSONObject().put("banks", banks);
    }

    private JSONObject getCommandeStatus(String commandeId) {
        try {
            Integer clientId = resolveCurrentClientId();
            Commande commande = commandeService.findByIdOrReference(commandeId, clientId);
            if (commande == null) {
                return new JSONObject().put("status", "Commande introuvable.");
            }
            return new JSONObject()
                    .put("reference", commande.getReference())
                    .put("status", safe(commande.getStatus()))
                    .put("priorite", safe(commande.getPriorite()))
                    .put("quantite", commande.getQuantite())
                    .put("type_sang", safe(commande.getTypeSang()));
        } catch (SQLException e) {
            return new JSONObject().put("status", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    private JSONObject getDonationPreparation() {
        return new JSONObject().put("instructions", new JSONArray()
                .put("Buvez beaucoup d'eau avant le don, au moins 500 ml.")
                .put("Mangez un repas equilibre et evitez les aliments trop gras.")
                .put("Apportez une piece d'identite valide.")
                .put("Dormez bien la veille.")
                .put("Evitez l'effort physique intense avant et apres le don."));
    }

    private String banksToText(JSONObject result) {
        JSONArray banks = result.optJSONArray("banks");
        if (banks == null || banks.isEmpty()) {
            return result.optString("message", "Aucune banque trouvee.");
        }
        StringBuilder text = new StringBuilder("Voici les banques disponibles:\n");
        for (int i = 0; i < banks.length(); i++) {
            text.append("- ").append(banks.optString(i)).append("\n");
        }
        return text.toString().trim();
    }

    private String commandeStatusToText(JSONObject result) {
        if (result.has("reference")) {
            return "Commande #" + result.optInt("reference")
                    + "\nStatut: " + result.optString("status")
                    + "\nPriorite: " + result.optString("priorite")
                    + "\nType sang: " + result.optString("type_sang")
                    + "\nQuantite: " + result.optInt("quantite");
        }
        return result.optString("status", "Commande introuvable.");
    }

    private String donationPreparationToText() {
        JSONArray instructions = getDonationPreparation().optJSONArray("instructions");
        StringBuilder text = new StringBuilder("Avant un don de sang:\n");
        for (int i = 0; i < instructions.length(); i++) {
            text.append("- ").append(instructions.optString(i)).append("\n");
        }
        return text.toString().trim();
    }

    private Integer resolveCurrentClientId() throws SQLException {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return null;
        }
        Client client = clientService.getByUserId(user.getId());
        return client == null ? null : client.getId();
    }

    private String getGeminiApiKey() {
        String propertyValue = System.getProperty("GEMINI_API_KEY");
        if (propertyValue == null || propertyValue.isBlank()) {
            propertyValue = System.getProperty("gemini.api.key");
        }
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }

        String envValue = System.getenv("GEMINI_API_KEY");
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        String localValue = LOCAL_CONFIG.getProperty("GEMINI_API_KEY");
        if (localValue == null || localValue.isBlank()) {
            localValue = LOCAL_CONFIG.getProperty("gemini.api.key");
        }
        return localValue == null ? "" : localValue.trim();
    }

    private static Properties loadLocalConfig() {
        Properties properties = new Properties();
        List<Path> paths = new ArrayList<>();
        paths.add(Path.of("chatbot.properties"));
        paths.add(Path.of("email.properties"));

        for (Path path : paths) {
            if (Files.exists(path)) {
                try (InputStream input = Files.newInputStream(path)) {
                    properties.load(input);
                } catch (IOException e) {
                    System.err.println("Could not read " + path + ": " + e.getMessage());
                }
            }
        }

        try (InputStream input = ChatbotService.class.getResourceAsStream("/chatbot.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Could not read classpath chatbot.properties: " + e.getMessage());
        }
        return properties;
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private boolean containsAny(String value, String... tokens) {
        for (String token : tokens) {
            if (value.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private String extractBloodType(String value) {
        Matcher matcher = BLOOD_TYPE_PATTERN.matcher(value == null ? "" : value.toUpperCase(Locale.ROOT));
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).toUpperCase(Locale.ROOT) + matcher.group(2);
    }

    private String normalizeBloodType(String value) {
        String extracted = extractBloodType(value);
        return extracted == null ? "" : extracted;
    }

    private String extractFirstNumber(String value) {
        Matcher matcher = NUMBER_PATTERN.matcher(value == null ? "" : value);
        return matcher.find() ? matcher.group() : null;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public record ChatMessage(String role, String text) {
    }
}
