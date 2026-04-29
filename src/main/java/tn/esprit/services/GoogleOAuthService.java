package tn.esprit.services;

import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import tn.esprit.tools.GoogleOAuthConfig;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Handles the full Google OAuth 2.0 Authorization Code flow for desktop apps.
 *
 * Flow:
 * 1. Generate PKCE code verifier + challenge
 * 2. Open system browser with Google consent URL
 * 3. Start a temporary local HTTP server to catch the redirect
 * 4. Exchange the auth code for an access token
 * 5. Fetch user profile (email, name) from Google's userinfo endpoint
 */
public class GoogleOAuthService {

    /** Simple container for the authenticated Google user's profile. */
    public static class GoogleUserInfo {
        public final String email;
        public final String givenName;   // prénom
        public final String familyName;  // nom
        public final String pictureUrl;

        public GoogleUserInfo(String email, String givenName, String familyName, String pictureUrl) {
            this.email = email;
            this.givenName = givenName;
            this.familyName = familyName;
            this.pictureUrl = pictureUrl;
        }
    }

    /**
     * Starts the full OAuth flow and returns the authenticated user's info.
     * This method blocks until the user completes auth in the browser (up to 2 min).
     *
     * @return GoogleUserInfo with the user's email and name
     * @throws Exception if any step fails
     */
    public GoogleUserInfo authenticate() throws Exception {
        // 1. Generate PKCE verifier + challenge
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);

        // 2. Build authorization URL
        String authUrl = GoogleOAuthConfig.AUTH_ENDPOINT
                + "?client_id=" + URLEncoder.encode(GoogleOAuthConfig.CLIENT_ID, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(GoogleOAuthConfig.REDIRECT_URI, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(GoogleOAuthConfig.SCOPES, StandardCharsets.UTF_8)
                + "&code_challenge=" + URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8)
                + "&code_challenge_method=S256"
                + "&access_type=offline"
                + "&prompt=consent";

        // 3. Start local HTTP server to capture redirect
        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", GoogleOAuthConfig.REDIRECT_PORT), 0);

        server.createContext("/", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String code = null;
            String error = null;

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] kv = param.split("=", 2);
                    if (kv.length == 2) {
                        if ("code".equals(kv[0])) code = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                        if ("error".equals(kv[0])) error = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                    }
                }
            }

            // Send a nice HTML response to the browser
            String html;
            if (code != null) {
                html = buildSuccessHtml();
                codeFuture.complete(code);
            } else {
                String errMsg = error != null ? error : "Unknown error";
                html = buildErrorHtml(errMsg);
                codeFuture.completeExceptionally(new RuntimeException("Google auth error: " + errMsg));
            }

            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        server.start();
        System.out.println("🔐 OAuth local server started on port " + GoogleOAuthConfig.REDIRECT_PORT);

        // 4. Open system browser
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(authUrl));
        } else {
            // Fallback: try xdg-open on Linux
            Runtime.getRuntime().exec(new String[]{"xdg-open", authUrl});
        }

        // 5. Wait for the redirect (max 2 minutes)
        String authCode;
        try {
            authCode = codeFuture.get(2, TimeUnit.MINUTES);
        } finally {
            server.stop(1);
            System.out.println("🔐 OAuth local server stopped.");
        }

        // 6. Exchange auth code for access token
        String accessToken = exchangeCodeForToken(authCode, codeVerifier);

        // 7. Fetch user info
        return fetchUserInfo(accessToken);
    }

    /**
     * Exchange the authorization code for an access token via Google's token endpoint.
     */
    private String exchangeCodeForToken(String code, String codeVerifier) throws Exception {
        URL url = new URL(GoogleOAuthConfig.TOKEN_ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(GoogleOAuthConfig.CLIENT_ID, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(GoogleOAuthConfig.CLIENT_SECRET, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(GoogleOAuthConfig.REDIRECT_URI, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code"
                + "&code_verifier=" + URLEncoder.encode(codeVerifier, StandardCharsets.UTF_8);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        String responseBody = readStream(status >= 400 ? conn.getErrorStream() : conn.getInputStream());

        if (status != 200) {
            throw new RuntimeException("Token exchange failed (HTTP " + status + "): " + responseBody);
        }

        JSONObject json = new JSONObject(responseBody);
        return json.getString("access_token");
    }

    /**
     * Fetch the authenticated user's profile from Google's userinfo endpoint.
     */
    private GoogleUserInfo fetchUserInfo(String accessToken) throws Exception {
        URL url = new URL(GoogleOAuthConfig.USERINFO_ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        int status = conn.getResponseCode();
        String responseBody = readStream(status >= 400 ? conn.getErrorStream() : conn.getInputStream());

        if (status != 200) {
            throw new RuntimeException("Userinfo fetch failed (HTTP " + status + "): " + responseBody);
        }

        JSONObject json = new JSONObject(responseBody);
        return new GoogleUserInfo(
                json.optString("email", ""),
                json.optString("given_name", ""),
                json.optString("family_name", ""),
                json.optString("picture", "")
        );
    }

    // ─── PKCE Helpers ───────────────────────────────────────────────────

    private String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCodeChallenge(String verifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    // ─── HTTP Helpers ───────────────────────────────────────────────────

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    // ─── Browser HTML Responses ─────────────────────────────────────────

    private String buildSuccessHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head><title>BloodLink - Connexion réussie</title>
            <style>
                body { font-family: 'Segoe UI', sans-serif; background: #0a0a0a; color: white;
                       display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
                .card { text-align: center; background: #141414; border: 1px solid #222; border-radius: 16px;
                        padding: 40px 60px; box-shadow: 0 8px 32px rgba(0,0,0,0.6); }
                h1 { color: #e74c3c; margin-bottom: 10px; }
                p { color: #aaa; }
            </style></head>
            <body>
                <div class="card">
                    <h1>✔ Connexion réussie</h1>
                    <p>Vous pouvez fermer cet onglet et retourner à BloodLink.</p>
                </div>
            </body>
            </html>
            """;
    }

    private String buildErrorHtml(String errorMsg) {
        return """
            <!DOCTYPE html>
            <html>
            <head><title>BloodLink - Erreur</title>
            <style>
                body { font-family: 'Segoe UI', sans-serif; background: #0a0a0a; color: white;
                       display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
                .card { text-align: center; background: #141414; border: 1px solid #222; border-radius: 16px;
                        padding: 40px 60px; box-shadow: 0 8px 32px rgba(0,0,0,0.6); }
                h1 { color: #e74c3c; margin-bottom: 10px; }
                p { color: #aaa; }
                .error { color: #ff6b6b; font-size: 14px; margin-top: 15px; }
            </style></head>
            <body>
                <div class="card">
                    <h1>✖ Erreur d'authentification</h1>
                    <p>Impossible de se connecter avec Google.</p>
                    <p class="error">%s</p>
                </div>
            </body>
            </html>
            """.formatted(errorMsg);
    }
}
