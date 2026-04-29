package tn.esprit.tools;

/**
 * Configuration for Google OAuth 2.0 Desktop flow.
 *
 * SETUP INSTRUCTIONS:
 * 1. Go to https://console.cloud.google.com/
 * 2. Create a project (or select existing)
 * 3. Enable "Google People API"
 * 4. Go to APIs & Services → Credentials → Create Credentials → OAuth client ID
 * 5. Choose "Desktop app" as application type
 * 6. Copy the Client ID and Client Secret below
 */
public class GoogleOAuthConfig {

    // ─── Replace these with your own credentials ────────────────────────
    public static final String CLIENT_ID = "YOUR_CLIENT_ID.apps.googleusercontent.com";
    public static final String CLIENT_SECRET = "YOUR_CLIENT_SECRET";
    // ────────────────────────────────────────────────────────────────────

    /** Port for the local HTTP server that catches the redirect. */
    public static final int REDIRECT_PORT = 8888;

    /** The loopback redirect URI registered in Google Cloud Console. */
    public static final String REDIRECT_URI = "http://127.0.0.1:" + REDIRECT_PORT;

    /** Google authorization endpoint. */
    public static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";

    /** Google token exchange endpoint. */
    public static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    /** Google userinfo endpoint — returns email, name, picture. */
    public static final String USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo";

    /** Scopes: email + profile info. */
    public static final String SCOPES = "openid email profile";
}
