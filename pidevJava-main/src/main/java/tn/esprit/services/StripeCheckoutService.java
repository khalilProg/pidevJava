package tn.esprit.services;

import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONObject;
import tn.esprit.entities.Commande;
import tn.esprit.entities.User;
import tn.esprit.tools.ThemeManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;

public class StripeCheckoutService {

    public enum CheckoutResult {
        SUCCESS,
        CANCELLED,
        CLOSED
    }

    private static final Properties LOCAL_CONFIG = loadLocalConfig();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .build();

    public boolean isConfigured() {
        return !getCheckoutEndpoint().isBlank() || !getSecretKey().isBlank();
    }

    public String createCheckoutSessionUrl(Commande commande, User user, String clientName, String banqueName)
            throws IOException, InterruptedException {
        String endpoint = getCheckoutEndpoint();
        if (!endpoint.isBlank()) {
            return createCheckoutSessionUrlThroughBackend(endpoint, commande, user, clientName, banqueName);
        }

        return createCheckoutSessionUrlDirectly(commande, user, clientName, banqueName);
    }

    private String createCheckoutSessionUrlThroughBackend(String endpoint, Commande commande, User user,
                                                          String clientName, String banqueName)
            throws IOException, InterruptedException {
        JSONObject payload = new JSONObject()
                .put("commandeId", commande.getId())
                .put("reference", commande.getReference())
                .put("clientId", commande.getClientId())
                .put("clientEmail", user == null ? "" : safe(user.getEmail()))
                .put("clientName", safe(clientName))
                .put("banqueId", commande.getBanqueId())
                .put("banqueName", safe(banqueName))
                .put("bloodType", safe(commande.getTypeSang()))
                .put("quantity", commande.getQuantite())
                .put("priority", safe(commande.getPriorite()));

        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(25))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Stripe checkout endpoint returned HTTP " + response.statusCode() + ": " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        String checkoutUrl = json.optString("url", "");
        if (checkoutUrl.isBlank()) {
            checkoutUrl = json.optString("checkoutUrl", "");
        }
        if (checkoutUrl.isBlank()) {
            checkoutUrl = json.optString("sessionUrl", "");
        }
        if (checkoutUrl.isBlank()) {
            throw new IOException("Stripe checkout endpoint did not return a checkout URL.");
        }
        return checkoutUrl;
    }

    private String createCheckoutSessionUrlDirectly(Commande commande, User user, String clientName, String banqueName)
            throws IOException, InterruptedException {
        String secretKey = getSecretKey();
        if (secretKey.isBlank()) {
            throw new IllegalStateException("Stripe secret key is not configured.");
        }

        String productName = "BloodLink commande #" + commande.getReference();
        String description = commande.getTypeSang() + " - " + commande.getQuantite() + " ml - " + safe(banqueName);
        int amount = getConfiguredAmount();

        String body = form(
                "mode", "payment",
                "payment_method_types[0]", "card",
                "customer_email", user == null ? "" : safe(user.getEmail()),
                "client_reference_id", String.valueOf(commande.getId()),
                "metadata[commandeId]", String.valueOf(commande.getId()),
                "metadata[reference]", String.valueOf(commande.getReference()),
                "metadata[clientName]", safe(clientName),
                "line_items[0][quantity]", "1",
                "line_items[0][price_data][currency]", getCurrency(),
                "line_items[0][price_data][unit_amount]", String.valueOf(amount),
                "line_items[0][price_data][product_data][name]", productName,
                "line_items[0][price_data][product_data][description]", description,
                "success_url", getSuccessUrl(),
                "cancel_url", getCancelUrl()
        );

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.stripe.com/v1/checkout/sessions"))
                .timeout(Duration.ofSeconds(25))
                .header("Authorization", "Bearer " + secretKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Stripe returned HTTP " + response.statusCode() + ": " + response.body());
        }

        JSONObject json = new JSONObject(response.body());
        String checkoutUrl = json.optString("url", "");
        if (checkoutUrl.isBlank()) {
            throw new IOException("Stripe did not return a checkout URL.");
        }
        return checkoutUrl;
    }

    public CheckoutResult openCheckoutUrlInApp(String checkoutUrl, Window owner) throws Exception {
        if (checkoutUrl == null || checkoutUrl.isBlank()) {
            throw new IllegalArgumentException("Checkout URL is empty.");
        }

        URI.create(checkoutUrl);
        CheckoutResult[] result = {CheckoutResult.CLOSED};

        Stage stage = new Stage();
        stage.setTitle("BloodLink - Paiement Stripe");
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }

        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);
        engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        engine.setCreatePopupHandler(features -> engine);

        Label title = new Label("Paiement Stripe");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 900;");

        Label status = new Label("Chargement du paiement securise...");
        status.setStyle("-fx-font-size: 12px;");

        Button reloadButton = new Button("Recharger");
        reloadButton.setOnAction(event -> engine.reload());

        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(event -> {
            result[0] = CheckoutResult.CANCELLED;
            stage.close();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, title, spacer, reloadButton, cancelButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 10, 16));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.progressProperty().bind(engine.getLoadWorker().progressProperty());
        progressBar.visibleProperty().bind(engine.getLoadWorker().runningProperty());
        progressBar.managedProperty().bind(progressBar.visibleProperty());

        VBox top = new VBox(header, progressBar);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(webView);
        root.setBottom(status);
        BorderPane.setMargin(status, new Insets(8, 16, 12, 16));

        applyPaymentWindowStyle(root, header, title, status, reloadButton, cancelButton);

        engine.locationProperty().addListener((observable, oldLocation, newLocation) -> {
            if (matchesConfiguredUrl(newLocation, getSuccessUrl())) {
                result[0] = CheckoutResult.SUCCESS;
                stage.close();
            } else if (matchesConfiguredUrl(newLocation, getCancelUrl())) {
                result[0] = CheckoutResult.CANCELLED;
                stage.close();
            }
        });

        engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.RUNNING) {
                status.setText("Chargement du paiement securise...");
            } else if (newState == Worker.State.SUCCEEDED) {
                status.setText("Paiement ouvert dans BloodLink.");
            } else if (newState == Worker.State.FAILED) {
                Throwable exception = engine.getLoadWorker().getException();
                status.setText("Erreur de chargement Stripe: "
                        + (exception == null ? "verifiez votre connexion." : exception.getMessage()));
            }
        });

        Scene scene = new Scene(root, 980, 720);
        stage.setScene(scene);
        engine.load(checkoutUrl);
        stage.showAndWait();
        return result[0];
    }

    private String getCheckoutEndpoint() {
        String value = System.getProperty("BLOODLINK_STRIPE_CHECKOUT_ENDPOINT");
        if (value == null || value.isBlank()) {
            value = System.getProperty("bloodlink.stripe.checkout.endpoint");
        }
        if (value == null || value.isBlank()) {
            value = System.getenv("BLOODLINK_STRIPE_CHECKOUT_ENDPOINT");
        }
        if (value == null || value.isBlank()) {
            value = LOCAL_CONFIG.getProperty("BLOODLINK_STRIPE_CHECKOUT_ENDPOINT");
        }
        if (value == null || value.isBlank()) {
            value = LOCAL_CONFIG.getProperty("bloodlink.stripe.checkout.endpoint");
        }
        return value == null ? "" : value.trim();
    }

    private String getSecretKey() {
        String value = getConfig("STRIPE_SECRET_KEY", "stripe.secret.key", "");
        if ("sk_test_your_key_here".equals(value) || "sk_live_your_key_here".equals(value)) {
            return "";
        }
        return value;
    }

    private String getCurrency() {
        String value = getConfig("STRIPE_CURRENCY", "stripe.currency", "usd");
        return value.isBlank() ? "usd" : value.toLowerCase();
    }

    private int getConfiguredAmount() {
        String value = getConfig("STRIPE_UNIT_AMOUNT", "stripe.unit.amount", "1000");
        try {
            int amount = Integer.parseInt(value.trim());
            return Math.max(amount, 50);
        } catch (NumberFormatException e) {
            return 1000;
        }
    }

    private String getSuccessUrl() {
        return getConfig("STRIPE_SUCCESS_URL", "stripe.success.url", "https://example.com/success");
    }

    private String getCancelUrl() {
        return getConfig("STRIPE_CANCEL_URL", "stripe.cancel.url", "https://example.com/cancel");
    }

    private boolean matchesConfiguredUrl(String currentUrl, String configuredUrl) {
        if (currentUrl == null || currentUrl.isBlank() || configuredUrl == null || configuredUrl.isBlank()) {
            return false;
        }
        String current = currentUrl.trim();
        String configured = configuredUrl.trim();
        if (current.equals(configured) || current.startsWith(configured + "?") || current.startsWith(configured + "#")) {
            return true;
        }
        try {
            URI currentUri = URI.create(current);
            URI configuredUri = URI.create(configured);
            return same(currentUri.getScheme(), configuredUri.getScheme())
                    && same(currentUri.getHost(), configuredUri.getHost())
                    && currentUri.getPort() == configuredUri.getPort()
                    && same(normalizePath(currentUri.getPath()), normalizePath(configuredUri.getPath()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean same(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equalsIgnoreCase(right == null ? "" : right);
    }

    private String normalizePath(String path) {
        return path == null || path.isBlank() ? "/" : path;
    }

    private void applyPaymentWindowStyle(BorderPane root, HBox header, Label title, Label status,
                                         Button reloadButton, Button cancelButton) {
        boolean dark = ThemeManager.getInstance().isDarkMode();
        if (dark) {
            root.setStyle("-fx-background-color: #0a0a0a;");
            header.setStyle("-fx-background-color: #141414; -fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 0 0 1 0;");
            title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: 900;");
            status.setStyle("-fx-text-fill: #aeb7c3; -fx-font-size: 12px; -fx-background-color: #0a0a0a;");
            stylePaymentButton(reloadButton, "#242424", "#ffffff", "rgba(255,255,255,0.14)");
            stylePaymentButton(cancelButton, "#e74c3c", "#ffffff", "#e74c3c");
        } else {
            root.setStyle("-fx-background-color: #f4f6f8;");
            header.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e9f0; -fx-border-width: 0 0 1 0;");
            title.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: 900;");
            status.setStyle("-fx-text-fill: #667085; -fx-font-size: 12px; -fx-background-color: #f4f6f8;");
            stylePaymentButton(reloadButton, "#ffffff", "#111827", "#d9e1ea");
            stylePaymentButton(cancelButton, "#e74c3c", "#ffffff", "#e74c3c");
        }
    }

    private void stylePaymentButton(Button button, String background, String text, String border) {
        button.setStyle("-fx-background-color: " + background + ";"
                + "-fx-text-fill: " + text + ";"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: 1;"
                + "-fx-background-radius: 8;"
                + "-fx-border-radius: 8;"
                + "-fx-padding: 8 14;"
                + "-fx-font-weight: 800;"
                + "-fx-cursor: hand;");
    }

    private String getConfig(String envName, String propertyName, String defaultValue) {
        String value = System.getProperty(envName);
        if (value == null || value.isBlank()) {
            value = System.getProperty(propertyName);
        }
        if (value == null || value.isBlank()) {
            value = System.getenv(envName);
        }
        if (value == null || value.isBlank()) {
            value = LOCAL_CONFIG.getProperty(envName);
        }
        if (value == null || value.isBlank()) {
            value = LOCAL_CONFIG.getProperty(propertyName);
        }
        return value == null ? defaultValue : value.trim();
    }

    private String form(String... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i += 2) {
            String value = values[i + 1];
            if (value == null || value.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(encode(values[i])).append('=').append(encode(value));
        }
        return builder.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static Properties loadLocalConfig() {
        Properties properties = new Properties();
        Path localPath = Path.of("stripe.properties");
        if (Files.exists(localPath)) {
            try (InputStream input = Files.newInputStream(localPath)) {
                properties.load(input);
                return properties;
            } catch (IOException e) {
                System.err.println("Could not read stripe.properties: " + e.getMessage());
            }
        }

        try (InputStream input = StripeCheckoutService.class.getResourceAsStream("/stripe.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Could not read classpath stripe.properties: " + e.getMessage());
        }
        return properties;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
