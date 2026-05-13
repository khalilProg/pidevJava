package tn.esprit.services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import tn.esprit.tools.GoogleOAuthConfig;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Google Calendar integration using the same OAuth credentials as the login flow.
 * Uses a browser-based OAuth 2.0 flow with a local HTTP callback server.
 * Caches refresh tokens to disk so the user only authenticates once.
 */
public class GoogleCalendarOAuthService {

    private static final String APPLICATION_NAME = "BloodLink";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final int CALENDAR_REDIRECT_PORT = 8889; // Different port from login OAuth
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";
    private static final File TOKEN_FILE = new File("google_calendar_tokens.json");

    private Calendar calendarService;

    // Cached tokens
    private static String cachedAccessToken = null;
    private static String cachedRefreshToken = null;

    static {
        // Load cached tokens from disk on class load
        loadTokensFromDisk();
    }

    public Calendar getCalendarService() throws Exception {
        if (calendarService == null) {
            String accessToken = getValidAccessToken();
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod();
            com.google.api.client.auth.oauth2.Credential credential =
                    new com.google.api.client.auth.oauth2.Credential(
                            com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod())
                            .setAccessToken(accessToken);

            calendarService = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
        return calendarService;
    }

    /**
     * Gets a valid access token — either from cache, by refreshing, or by starting
     * a new browser-based OAuth flow.
     */
    private String getValidAccessToken() throws Exception {
        // 1. Try using the cached access token
        if (cachedAccessToken != null) {
            return cachedAccessToken;
        }

        // 2. Try refreshing with a cached refresh token
        if (cachedRefreshToken != null) {
            String refreshed = refreshAccessToken(cachedRefreshToken);
            if (refreshed != null) {
                cachedAccessToken = refreshed;
                return cachedAccessToken;
            }
        }

        // 3. Full browser OAuth flow
        return performBrowserOAuthFlow();
    }

    /**
     * Performs the full browser-based OAuth 2.0 authorization code flow
     * for Calendar scope, similar to GoogleOAuthService.
     */
    private String performBrowserOAuthFlow() throws Exception {
        String redirectUri = "http://127.0.0.1:" + CALENDAR_REDIRECT_PORT;

        String authUrl = GoogleOAuthConfig.AUTH_ENDPOINT
                + "?client_id=" + URLEncoder.encode(GoogleOAuthConfig.CLIENT_ID, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(CALENDAR_SCOPE, StandardCharsets.UTF_8)
                + "&access_type=offline"
                + "&prompt=consent";

        // Start local callback server
        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", CALENDAR_REDIRECT_PORT), 0);

        server.createContext("/", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String code = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("code=")) {
                        code = URLDecoder.decode(param.substring(5), StandardCharsets.UTF_8);
                    }
                }
            }

            String html = code != null
                    ? "<html><body style='font-family:sans-serif;text-align:center;padding-top:60px;background:#0a0a0a;color:white;'>"
                    + "<h1 style='color:#4cd964;'>✓ Calendrier Google autorisé</h1>"
                    + "<p>Vous pouvez fermer cette fenêtre et retourner à BloodLink.</p></body></html>"
                    : "<html><body style='font-family:sans-serif;text-align:center;padding-top:60px;background:#0a0a0a;color:white;'>"
                    + "<h1 style='color:#e74c3c;'>✗ Autorisation échouée</h1></body></html>";

            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();

            if (code != null) {
                codeFuture.complete(code);
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("📅 Calendar OAuth server started on port " + CALENDAR_REDIRECT_PORT);

        // Open browser (Linux-safe)
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("linux") || os.contains("unix")) {
                Runtime.getRuntime().exec(new String[]{"xdg-open", authUrl});
            } else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", authUrl});
            } else if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", authUrl});
            }
        } catch (Exception e) {
            System.out.println("Veuillez ouvrir cette URL manuellement : " + authUrl);
        }

        // Wait for the auth code (max 2 minutes)
        String authCode;
        try {
            authCode = codeFuture.get(2, TimeUnit.MINUTES);
        } finally {
            server.stop(0);
            System.out.println("📅 Calendar OAuth server stopped.");
        }

        // Exchange the auth code for tokens
        return exchangeCodeForTokens(authCode, redirectUri);
    }

    /**
     * Exchanges the authorization code for access + refresh tokens.
     */
    private String exchangeCodeForTokens(String code, String redirectUri) throws Exception {
        String body = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(GoogleOAuthConfig.CLIENT_ID, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(GoogleOAuthConfig.CLIENT_SECRET, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";

        JSONObject json = postToTokenEndpoint(body);
        cachedAccessToken = json.getString("access_token");

        if (json.has("refresh_token")) {
            cachedRefreshToken = json.getString("refresh_token");
            saveTokensToDisk();
        }

        return cachedAccessToken;
    }

    /**
     * Refreshes the access token using a stored refresh token.
     */
    private String refreshAccessToken(String refreshToken) {
        try {
            String body = "refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                    + "&client_id=" + URLEncoder.encode(GoogleOAuthConfig.CLIENT_ID, StandardCharsets.UTF_8)
                    + "&client_secret=" + URLEncoder.encode(GoogleOAuthConfig.CLIENT_SECRET, StandardCharsets.UTF_8)
                    + "&grant_type=refresh_token";

            JSONObject json = postToTokenEndpoint(body);
            return json.getString("access_token");
        } catch (Exception e) {
            System.err.println("Token refresh failed: " + e.getMessage());
            cachedRefreshToken = null;
            return null;
        }
    }

    private JSONObject postToTokenEndpoint(String body) throws Exception {
        URL url = new URL(GoogleOAuthConfig.TOKEN_ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

        int status = conn.getResponseCode();
        InputStream stream = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String response = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

        if (status != 200) {
            throw new Exception("Token exchange failed (HTTP " + status + "): " + response);
        }

        return new JSONObject(response);
    }

    // ── Token persistence ──────────────────────────────────────────────

    private static void saveTokensToDisk() {
        try {
            JSONObject json = new JSONObject();
            if (cachedRefreshToken != null) {
                json.put("refresh_token", cachedRefreshToken);
            }
            try (FileWriter fw = new FileWriter(TOKEN_FILE)) {
                fw.write(json.toString());
            }
        } catch (Exception e) {
            System.err.println("Could not save calendar tokens: " + e.getMessage());
        }
    }

    private static void loadTokensFromDisk() {
        try {
            if (TOKEN_FILE.exists()) {
                String content = new String(java.nio.file.Files.readAllBytes(TOKEN_FILE.toPath()), StandardCharsets.UTF_8);
                JSONObject json = new JSONObject(content);
                cachedRefreshToken = json.optString("refresh_token", null);
            }
        } catch (Exception e) {
            System.err.println("Could not load calendar tokens: " + e.getMessage());
        }
    }

    // ── Public API ─────────────────────────────────────────────────────

    /**
     * Adds a rendez-vous to Google Calendar and returns the event link.
     */
    public String addRendezVous(String patientName, String campagne,
                                LocalDateTime dateTime, String entite) throws Exception {
        // Reset service so token is refreshed each time
        calendarService = null;

        Event event = new Event()
                .setSummary("🩸 Don de sang — " + campagne)
                .setLocation(entite)
                .setDescription("Rendez-vous BloodLink pour " + patientName
                        + "\nCampagne : " + campagne
                        + "\nEntité de collecte : " + entite);

        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(
                        dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                .setTimeZone("Africa/Tunis");

        EventDateTime end = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(
                        dateTime.plusMinutes(45).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                .setTimeZone("Africa/Tunis");

        event.setStart(start);
        event.setEnd(end);

        Event created = getCalendarService().events()
                .insert("primary", event)
                .execute();

        return created.getHtmlLink();
    }
}
