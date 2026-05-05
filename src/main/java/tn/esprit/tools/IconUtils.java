package tn.esprit.tools;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Locale;

public final class IconUtils {
    private static final String LAST_ICON_KEY = "bloodlink.icon.literal";
    private static final String LAST_TEXT_KEY = "bloodlink.icon.text";
    private static final String CLEANER_INSTALLED_KEY = "bloodlink.icon.cleanerInstalled";
    private static final String CLEANING_TEXT_KEY = "bloodlink.icon.cleaningText";
    private static final String[] ICON_TOKENS = {
            "\u2190", "\u27A1", "\u2B05", "\u2713", "\u2705", "\u2714", "\u2715", "\u2716", "\u274C", "\u270E", "\u270F",
            "\u26A0", "\u2699", "\u2139", "\u2600", "\u2630", "\u23FB", "\u2B50", "\u2795", "\u2796",
            "\uD83D\uDCCA", "\uD83D\uDC65", "\uD83D\uDCE6", "\uD83C\uDFE2", "\uD83D\uDCCB",
            "\uD83D\uDD04", "\u2753", "\uD83D\uDCC5", "\uD83D\uDCE2", "\uD83D\uDCE4", "\uD83D\uDCE5", "\uD83C\uDFE5",
            "\uD83D\uDC89", "\uD83D\uDCC1", "\uD83D\uDD0D", "\uD83D\uDD34", "\uD83D\uDC64",
            "\uD83D\uDD11", "\uD83E\uDE78", "\uD83C\uDFDB", "\uD83D\uDCCD", "\uD83D\uDE9A",
            "\uD83D\uDEE1", "\uD83E\uDD16", "\uD83D\uDCC4", "\uD83D\uDCBE", "\uD83D\uDE80",
            "\uD83D\uDC41", "\uD83D\uDED2", "\uD83D\uDDD1", "\uD83D\uDDF8", "\uD83D\uDCCC", "\uD83D\uDCCE"
    };

    private IconUtils() {
    }

    public static void applyIcons(Parent root) {
        if (root == null) {
            return;
        }
        applyIcon(root);
        for (Node child : root.getChildrenUnmodifiable()) {
            if (child instanceof Parent childParent) {
                applyIcons(childParent);
            } else {
                applyIcon(child);
            }
        }
    }

    public static void decorateButton(Button button) {
        applyIcon(button);
    }

    public static void setThemeToggleIcon(Button button, boolean darkMode) {
        if (button == null) {
            return;
        }
        setIcon(button, darkMode ? "fas-sun" : "fas-moon", "", 15);
    }

    private static void applyIcon(Node node) {
        if (node instanceof Button button) {
            IconSpec spec = specForButton(button);
            if (spec != null) {
                setIcon(button, spec.iconLiteral(), spec.text(), spec.size());
            }
            return;
        }

        if (node instanceof Label label) {
            IconSpec spec = specForLabel(label);
            if (spec != null) {
                setIcon(label, spec.text(), spec.iconLiteral(), spec.size());
            }
            return;
        }

        if (node instanceof TextInputControl input) {
            String prompt = input.getPromptText();
            if (prompt != null && startsWithIcon(prompt)) {
                input.setPromptText(cleanText(prompt));
            }
        }
    }

    private static void setIcon(Button button, String iconLiteral, String text, int size) {
        setLabeledIcon(button, iconLiteral, text, size);
        button.setContentDisplay(text == null || text.isBlank() ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.LEFT);
        button.setGraphicTextGap(text == null || text.isBlank() ? 0 : 8);
    }

    private static void setIcon(Label label, String text, String iconLiteral, int size) {
        setLabeledIcon(label, iconLiteral, text, size);
        label.setContentDisplay(text == null || text.isBlank() ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.LEFT);
        label.setGraphicTextGap(text == null || text.isBlank() ? 0 : 6);
    }

    private static void setLabeledIcon(Labeled labeled, String iconLiteral, String text, int size) {
        String safeText = text == null ? "" : text.trim();
        Object lastIcon = labeled.getProperties().get(LAST_ICON_KEY);
        Object lastText = labeled.getProperties().get(LAST_TEXT_KEY);
        if (iconLiteral.equals(lastIcon) && safeText.equals(lastText)) {
            return;
        }

        installTextCleaner(labeled);
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(size);
        if (!icon.getStyleClass().contains("ui-font-icon")) {
            icon.getStyleClass().add("ui-font-icon");
        }
        labeled.setGraphic(icon);
        labeled.setText(safeText);
        labeled.getProperties().put(LAST_ICON_KEY, iconLiteral);
        labeled.getProperties().put(LAST_TEXT_KEY, safeText);
    }

    private static void installTextCleaner(Labeled labeled) {
        if (Boolean.TRUE.equals(labeled.getProperties().get(CLEANER_INSTALLED_KEY))) {
            return;
        }

        labeled.getProperties().put(CLEANER_INSTALLED_KEY, Boolean.TRUE);
        labeled.textProperty().addListener((observable, oldText, newText) -> {
            if (newText == null || newText.isBlank()) {
                return;
            }
            if (Boolean.TRUE.equals(labeled.getProperties().get(CLEANING_TEXT_KEY))) {
                return;
            }

            String cleaned = cleanText(newText);
            if (!cleaned.equals(newText)) {
                try {
                    labeled.getProperties().put(CLEANING_TEXT_KEY, Boolean.TRUE);
                    labeled.setText(cleaned);
                    labeled.getProperties().put(LAST_TEXT_KEY, cleaned);
                } finally {
                    labeled.getProperties().put(CLEANING_TEXT_KEY, Boolean.FALSE);
                }
            }
        });
    }

    private static IconSpec specForButton(Button button) {
        String id = safeLower(button.getId());
        String text = button.getText() == null ? "" : button.getText();
        String normalized = safeLower(text);
        String cleaned = cleanText(text);

        return switch (id) {
            case "btndashboard" -> new IconSpec("fas-chart-bar", "Dashboard", 14);
            case "btnusers" -> new IconSpec("fas-users", "Utilisateurs", 14);
            case "btncommandes" -> new IconSpec("fas-box", "Commandes", 14);
            case "btnstocks" -> new IconSpec("fas-warehouse", "Stocks", 14);
            case "btndemandes" -> new IconSpec("fas-clipboard-list", "Demandes", 14);
            case "btntransferts" -> new IconSpec("fas-sync-alt", "Transferts", 14);
            case "btnquestionnaires" -> new IconSpec("fas-question-circle", "Questionnaires", 14);
            case "btnrendezvous" -> new IconSpec("fas-calendar-alt", "Rendez-vous", 14);
            case "btncampagnes" -> new IconSpec("fas-bullhorn", "Campagnes", 14);
            case "btncollectes" -> new IconSpec("fas-hospital", "Collectes", 14);
            case "btnthemetoggle" -> new IconSpec("fas-sun", "", 15);
            default -> specForButtonText(button, normalized, cleaned);
        };
    }

    private static IconSpec specForButtonText(Button button, String normalized, String cleaned) {
        if (button.getStyleClass().contains("sidebar-disconnect-btn") || normalized.contains("deconnexion") || normalized.contains("déconnexion")) {
            return new IconSpec("fas-power-off", cleaned.isBlank() ? "Deconnexion" : cleaned, 14);
        }
        if (button.getStyleClass().contains("user-badge") || normalized.contains("utilisateur") || normalized.contains("inscrire")) {
            return new IconSpec("fas-user", cleaned, 14);
        }
        if (normalized.contains("menu")) {
            return new IconSpec("fas-bars", cleaned.replace("\u2630", "").trim(), 14);
        }
        if (normalized.contains("fermer")) {
            return new IconSpec("fas-times", cleaned, 14);
        }
        if (button.getStyleClass().contains("action-btn-delete") || normalized.contains("supprimer") || normalized.equals("delete")) {
            return new IconSpec("fas-trash", cleaned.equalsIgnoreCase("delete") ? "" : cleaned, 13);
        }
        if (startsWithAny(button.getText(), "\u2715", "\u2716", "\u274C")) {
            return new IconSpec("fas-times", cleaned, 13);
        }
        if (button.getStyleClass().contains("action-btn-edit") || normalized.contains("modifier") || normalized.equals("edit") || normalized.equals("update")) {
            return new IconSpec("fas-edit", cleaned.equalsIgnoreCase("edit") || cleaned.equalsIgnoreCase("update") ? "" : cleaned, 13);
        }
        if (startsWithAny(button.getText(), "\u2713", "\u2705", "\u2714", "\uD83D\uDDF8")) {
            return new IconSpec("fas-check", cleaned, 13);
        }
        if (normalized.contains("voir") || normalized.equals("view")) {
            return new IconSpec("fas-eye", cleaned.equalsIgnoreCase("view") ? "" : cleaned, 13);
        }
        if (normalized.contains("pdf")) {
            return new IconSpec("fas-file-pdf", cleaned, 14);
        }
        if (startsWithAny(button.getText(), "\uD83D\uDCE5")) {
            return new IconSpec("fas-download", cleaned, 14);
        }
        if (normalized.contains("retour")) {
            return new IconSpec("fas-arrow-left", cleaned, 13);
        }
        if (normalized.contains("enregistrer") || normalized.contains("confirmer") || normalized.contains("creer") || normalized.contains("créer") || normalized.contains("mettre a jour") || normalized.contains("mettre à jour")) {
            return new IconSpec("fas-save", cleaned, 14);
        }
        if (normalized.contains("ajouter") || normalized.contains("nouvel")) {
            return new IconSpec("fas-plus", cleaned, 14);
        }
        if (normalized.contains("prediction") || normalized.contains("prédiction")) {
            return new IconSpec("fas-robot", cleaned, 14);
        }
        if (normalized.contains("envoyer")) {
            return new IconSpec("fas-paper-plane", cleaned, 14);
        }
        if (normalized.contains("calendrier")) {
            return new IconSpec("fas-calendar-alt", cleaned, 14);
        }
        if (normalized.contains("dons")) {
            return new IconSpec("fas-tint", cleaned, 14);
        }
        if (normalized.contains("dossiers")) {
            return new IconSpec("fas-folder", cleaned, 14);
        }
        if (startsWithIcon(normalized)) {
            return new IconSpec("fas-check", cleaned, 14);
        }
        return null;
    }

    private static IconSpec specForLabel(Label label) {
        String id = safeLower(label.getId());
        String text = label.getText() == null ? "" : label.getText();
        String normalized = safeLower(text);
        String cleaned = cleanText(text);

        if (id.contains("emaillabel")) {
            return new IconSpec("fas-envelope", cleaned, 13);
        }
        if (id.contains("datelabel") || id.contains("dateslabel") || id.contains("campaigndates")) {
            return new IconSpec("fas-calendar-alt", cleaned, 13);
        }
        if (id.contains("villelabel")) {
            return new IconSpec("fas-map-marker-alt", cleaned, 13);
        }
        if (id.contains("iconlabel")) {
            return new IconSpec("fas-hospital", "", 32);
        }
        if ("users".equals(normalized)) {
            return new IconSpec("fas-users", "", 20);
        }
        if ("cmd".equals(normalized)) {
            return new IconSpec("fas-box", "", 20);
        }
        if ("stock".equals(normalized)) {
            return new IconSpec("fas-warehouse", "", 20);
        }
        if ("rdv".equals(normalized)) {
            return new IconSpec("fas-calendar-alt", "", 24);
        }
        if ("q".equals(normalized)) {
            return new IconSpec("fas-question-circle", "", 24);
        }
        if ("c".equals(normalized)) {
            return new IconSpec("fas-hospital", "", 24);
        }
        if ("camp".equals(normalized)) {
            return new IconSpec("fas-bullhorn", "", 24);
        }
        if ("=".equals(normalized)) {
            return new IconSpec("fas-bolt", "", 13);
        }
        if (isBrandLabel(label) || startsWithAny(text, "\uD83E\uDE78", "\uD83D\uDCA7")) {
            return new IconSpec("fas-tint", cleaned, 20);
        }
        if (startsWithAny(text, "\uD83D\uDD0D")) {
            return new IconSpec("fas-search", cleaned, 14);
        }
        if (startsWithAny(text, "\uD83D\uDD11")) {
            return new IconSpec("fas-key", cleaned, 28);
        }
        if (startsWithAny(text, "\uD83D\uDC64")) {
            return new IconSpec("fas-user", cleaned, 28);
        }
        if (startsWithAny(text, "\uD83D\uDCE6", "\uD83D\uDED2")) {
            return new IconSpec("fas-box", cleaned, 22);
        }
        if (startsWithAny(text, "\uD83D\uDEE1")) {
            return new IconSpec("fas-shield-alt", cleaned, 22);
        }
        if (startsWithAny(text, "\uD83C\uDFE5")) {
            return new IconSpec("fas-hospital", cleaned, 22);
        }
        if (startsWithAny(text, "\uD83C\uDFDB")) {
            return new IconSpec("fas-university", cleaned, 16);
        }
        if (startsWithAny(text, "\uD83D\uDCCD")) {
            return new IconSpec("fas-map-marker-alt", cleaned, 13);
        }
        if (startsWithAny(text, "\uD83D\uDE9A")) {
            return new IconSpec("fas-truck", cleaned, 13);
        }
        if (startsWithAny(text, "\u2714", "\u2713")) {
            return new IconSpec("fas-check", cleaned, 13);
        }
        if (startsWithAny(text, "\u2705", "\uD83D\uDDF8")) {
            return new IconSpec("fas-check", cleaned, 13);
        }
        if (startsWithAny(text, "\u2715", "\u2716", "\u274C")) {
            return new IconSpec("fas-times", cleaned, 13);
        }
        if (startsWithAny(text, "\u2B50")) {
            return new IconSpec("fas-star", cleaned, 13);
        }
        if (startsWithAny(text, "\u26A0")) {
            return new IconSpec("fas-exclamation-triangle", cleaned, 13);
        }
        if (startsWithIcon(text) && cleaned.isBlank()) {
            return new IconSpec("fas-circle", "", 13);
        }
        return null;
    }

    private static boolean startsWithIcon(String value) {
        return startsWithAny(value, ICON_TOKENS);
    }

    private static boolean isBrandLabel(Label label) {
        String normalized = safeLower(label.getText());
        if (!"bloodlink".equals(normalized)) {
            return false;
        }
        return label.getStyleClass().contains("brand-label")
                || label.getStyleClass().contains("nav-logo-text")
                || label.getStyleClass().contains("sidebar-brand-text")
                || label.getStyleClass().contains("logo-text");
    }

    private static boolean startsWithAny(String value, String... prefixes) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        for (String prefix : prefixes) {
            if (trimmed.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static String cleanText(String text) {
        if (text == null) {
            return "";
        }

        String cleaned = text;
        for (String token : ICON_TOKENS) {
            cleaned = cleaned.replace(token, "");
        }
        return cleaned.replace("\uFE0F", "")
                .replace("\u200D", "")
                .replaceAll("^\\+\\s*", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record IconSpec(String iconLiteral, String text, int size) {
    }
}
