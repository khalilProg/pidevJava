package tn.esprit.tools;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;

/**
 * Singleton utility for managing Light/Dark theme switching across the application.
 * The current theme preference persists in-memory across view navigations.
 */
public class ThemeManager {

    public enum Theme { DARK, LIGHT }

    private static final ThemeManager INSTANCE = new ThemeManager();

    private Theme currentTheme = Theme.DARK; // default

    private static final String DARK_CSS = "/commande.css";
    private static final String LIGHT_CSS = "/commande-light.css";

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    // ─── Core Methods ──────────────────────────────────────────────

    /**
     * Apply the current theme to the given scene (swap stylesheets).
     */
    public void applyTheme(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        String css = (currentTheme == Theme.DARK) ? DARK_CSS : LIGHT_CSS;
        scene.getStylesheets().add(getClass().getResource(css).toExternalForm());
    }

    /**
     * Toggle between DARK ↔ LIGHT theme on the given scene.
     */
    public void toggleTheme(Scene scene) {
        currentTheme = (currentTheme == Theme.DARK) ? Theme.LIGHT : Theme.DARK;
        applyTheme(scene);
    }

    /**
     * Update a toggle button's text to reflect the current theme.
     * Shows ☀ for switching to light, 🌙 for switching to dark.
     */
    public void updateToggleButton(Button btn) {
        if (btn == null) return;
        btn.setText(currentTheme == Theme.DARK ? "☀  Light" : "🌙  Dark");
    }

    // ─── Theme Queries ─────────────────────────────────────────────

    public boolean isDarkMode() {
        return currentTheme == Theme.DARK;
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    // ─── Inline Style Helpers ──────────────────────────────────────
    // For table cell factories and other elements that need inline styles

    /** Primary text color */
    public String getTextColor() {
        return isDarkMode() ? "#cccccc" : "#1a1a1a";
    }

    /** Secondary / muted text color */
    public String getSubTextColor() {
        return isDarkMode() ? "#888888" : "#6c757d";
    }

    /** Primary background color */
    public String getBgColor() {
        return isDarkMode() ? "#121212" : "#f4f6f8";
    }

    /** Card / elevated surface color */
    public String getSurfaceColor() {
        return isDarkMode() ? "#1e1e1e" : "#ffffff";
    }

    /** Tag / chip background */
    public String getChipBgColor() {
        return isDarkMode() ? "#2a2a2a" : "#e9ecef";
    }

    /** Tag / chip text color */
    public String getChipTextColor() {
        return isDarkMode() ? "#cccccc" : "#4a4a4a";
    }

    /** Bold text on table cells */
    public String getTableBoldStyle() {
        return "-fx-text-fill: " + getTextColor() + "; -fx-font-weight: bold;";
    }

    /** Reference chip style */
    public String getReferenceChipStyle() {
        return "-fx-background-color: " + getChipBgColor() + "; -fx-text-fill: " + getChipTextColor()
                + "; -fx-font-weight: bold; -fx-padding: 4 10 4 10; -fx-background-radius: 12;";
    }

    /** Organisation label style */
    public String getOrgLabelStyle() {
        return "-fx-text-fill: " + getTextColor() + "; -fx-font-weight: bold;";
    }

    /** Date label style */
    public String getDateLabelStyle() {
        return "-fx-text-fill: " + getSubTextColor() + ";";
    }

    /** Count label style */
    public String getCountLabelStyle() {
        return "-fx-text-fill: " + getSubTextColor() + "; -fx-font-size: 11px; -fx-font-weight: bold;";
    }

    /** Front-office count label style (white in dark, dark in light) */
    public String getFrontCountStyle() {
        return "-fx-text-fill: " + getTextColor() + "; -fx-font-size: 12px; -fx-font-weight: bold;";
    }

    /**
     * Apply theme-aware styles to a dialog pane.
     */
    public void styleDialog(DialogPane dialogPane) {
        if (dialogPane == null) return;
        if (isDarkMode()) {
            dialogPane.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");
            dialogPane.lookupAll(".label").forEach(node -> node.setStyle("-fx-text-fill: white;"));
        } else {
            dialogPane.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1a1a1a;");
            dialogPane.lookupAll(".label").forEach(node -> node.setStyle("-fx-text-fill: #1a1a1a;"));
        }
    }
}
