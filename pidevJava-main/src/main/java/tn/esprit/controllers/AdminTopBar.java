package tn.esprit.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import tn.esprit.entities.Commande;
import tn.esprit.services.CommandeService;
import tn.esprit.tools.ThemeManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class AdminTopBar {
    private static final String INSTALLED_KEY = "bloodlink.admin.topbar.installed";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final int RECENT_LIMIT = 20;
    private static final int VISIBLE_NOTIFICATION_ROWS = 3;

    private AdminTopBar() {
    }

    public static void install(Node anchor) {
        if (anchor == null) {
            return;
        }

        Platform.runLater(() -> installNow(anchor));
        anchor.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> installNow(anchor));
            }
        });
    }

    public static void bindExisting(StackPane bellContainer, Circle notificationDot, Label dateLabel) {
        if (dateLabel != null) {
            dateLabel.setText(LocalDate.now().format(DATE_FORMAT));
        }
        if (bellContainer == null || notificationDot == null) {
            return;
        }

        configureNotificationDot(notificationDot);
        StackPane.setAlignment(notificationDot, Pos.TOP_RIGHT);
        Tooltip.install(bellContainer, new Tooltip("Nouvelles commandes"));
        bellContainer.setCursor(javafx.scene.Cursor.HAND);
        bellContainer.setOnMouseClicked(event -> showNotificationPopup(bellContainer));
        updateNotificationDot(notificationDot);
        startAutoRefresh(bellContainer, notificationDot);
    }

    private static void installNow(Node anchor) {
        Scene scene = anchor.getScene();
        if (scene == null || !(scene.getRoot() instanceof BorderPane root)) {
            return;
        }
        if (Boolean.TRUE.equals(root.getProperties().get(INSTALLED_KEY))) {
            return;
        }

        Node center = root.getCenter();
        if (center == null) {
            return;
        }

        Circle notificationDot = new Circle();
        configureNotificationDot(notificationDot);

        Button bellButton = new Button();
        bellButton.getStyleClass().add("admin-notification-button");
        bellButton.setTooltip(new Tooltip("Nouvelles commandes"));
        FontIcon bellIcon = new FontIcon("fas-bell");
        bellIcon.setIconSize(22);
        bellIcon.getStyleClass().add("admin-notification-icon");
        bellButton.setGraphic(bellIcon);

        StackPane bellStack = new StackPane(bellButton, notificationDot);
        bellStack.getStyleClass().add("admin-notification-stack");
        StackPane.setAlignment(notificationDot, Pos.TOP_RIGHT);
        bellButton.setOnAction(event -> showNotificationPopup(bellButton));

        FontIcon calendarIcon = new FontIcon("fas-calendar-alt");
        calendarIcon.setIconSize(16);
        calendarIcon.getStyleClass().add("admin-date-icon");
        Label dateLabel = new Label(LocalDate.now().format(DATE_FORMAT));
        dateLabel.getStyleClass().add("admin-date-label");
        HBox dateBox = new HBox(8, calendarIcon, dateLabel);
        dateBox.setAlignment(Pos.CENTER);
        dateBox.getStyleClass().add("admin-date-box");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topBar = new HBox(24, spacer, bellStack, dateBox);
        topBar.getStyleClass().add("admin-notification-bar");
        topBar.setAlignment(Pos.CENTER_RIGHT);

        VBox wrapper = new VBox(topBar, center);
        wrapper.getStyleClass().add("admin-center-with-topbar");
        VBox.setVgrow(center, Priority.ALWAYS);
        root.setCenter(wrapper);
        root.getProperties().put(INSTALLED_KEY, Boolean.TRUE);

        updateNotificationDot(notificationDot);
        startAutoRefresh(topBar, notificationDot);
    }

    private static void startAutoRefresh(Node owner, Circle notificationDot) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), event -> updateNotificationDot(notificationDot)),
                new KeyFrame(Duration.seconds(20), event -> updateNotificationDot(notificationDot))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        owner.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                timeline.stop();
            }
        });
    }

    private static void updateNotificationDot(Circle notificationDot) {
        int pendingCount = getPendingCount();
        boolean hasNotifications = pendingCount > 0;
        notificationDot.setVisible(hasNotifications);
        notificationDot.setManaged(hasNotifications);
        notificationDot.getProperties().put("pendingCount", pendingCount);
    }

    private static void showNotificationPopup(Node owner) {
        List<Commande> recentCommandes = getRecentPendingCommandes();
        int pendingCount = getPendingCount();
        boolean lightPopup = isLightPopup(owner);

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox popupCard = new VBox(12);
        popupCard.getStyleClass().add("admin-notification-popup");
        applyPopupTheme(owner, popupCard);
        if (lightPopup) {
            applyLightPopupCardStyle(popupCard);
        }
        popupCard.setPadding(new Insets(16));

        Label title = new Label("Notifications");
        title.getStyleClass().add("admin-notification-popup-title");
        Label subtitle = new Label(pendingCount == 0
                ? "Aucune nouvelle commande"
                : pendingCount + " commande(s) en attente");
        subtitle.getStyleClass().add("admin-notification-popup-subtitle");
        if (lightPopup) {
            title.setStyle("-fx-text-fill: #111827; -fx-font-size: 16px; -fx-font-weight: 900;");
            subtitle.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        }
        popupCard.getChildren().addAll(title, subtitle);

        if (recentCommandes.isEmpty()) {
            Label empty = new Label("Les nouvelles commandes apparaissent ici.");
            empty.getStyleClass().add("admin-notification-empty");
            if (lightPopup) {
                empty.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
            }
            popupCard.getChildren().add(empty);
        } else {
            VBox notificationList = new VBox(8);
            notificationList.getStyleClass().add("admin-notification-list");
            for (Commande commande : recentCommandes) {
                notificationList.getChildren().add(createNotificationRow(commande, popup, owner, lightPopup));
            }

            ScrollPane notificationScroll = new ScrollPane(notificationList);
            notificationScroll.getStyleClass().add("admin-notification-scroll");
            if (lightPopup) {
                notificationScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
                notificationList.setStyle("-fx-background-color: transparent;");
            }
            notificationScroll.setFitToWidth(true);
            notificationScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            notificationScroll.setVbarPolicy(recentCommandes.size() > VISIBLE_NOTIFICATION_ROWS
                    ? ScrollPane.ScrollBarPolicy.AS_NEEDED
                    : ScrollPane.ScrollBarPolicy.NEVER);
            int visibleRows = Math.min(VISIBLE_NOTIFICATION_ROWS, recentCommandes.size());
            notificationScroll.setPrefViewportHeight((visibleRows * 58) + Math.max(0, visibleRows - 1) * 8);
            notificationScroll.setMaxHeight((VISIBLE_NOTIFICATION_ROWS * 58) + ((VISIBLE_NOTIFICATION_ROWS - 1) * 8));
            popupCard.getChildren().add(notificationScroll);
        }

        Button openCommandes = new Button("Voir commandes");
        openCommandes.getStyleClass().add("admin-notification-open-btn");
        if (lightPopup) {
            openCommandes.setStyle("-fx-background-color: #e63939; -fx-background-radius: 8px; -fx-text-fill: #ffffff; "
                    + "-fx-font-size: 12px; -fx-font-weight: 900; -fx-padding: 10 12; -fx-cursor: hand;");
        }
        openCommandes.setMaxWidth(Double.MAX_VALUE);
        openCommandes.setOnAction(event -> {
            popup.hide();
            navigateToCommandes(owner);
        });
        popupCard.getChildren().add(openCommandes);

        popup.getContent().add(popupCard);
        Bounds bounds = owner.localToScreen(owner.getBoundsInLocal());
        double x = Math.max(12, bounds.getMaxX() - 340);
        double y = bounds.getMaxY() + 10;
        popup.show(owner, x, y);
        applyPopupTheme(owner, popup.getScene().getRoot());
        if (lightPopup) {
            applyLightPopupCardStyle(popupCard);
        }
    }

    private static HBox createNotificationRow(Commande commande, Popup popup, Node owner, boolean lightPopup) {
        StackPane icon = createNotificationMarker(lightPopup);

        Label title = new Label("Commande #" + commande.getReference());
        title.getStyleClass().add("admin-notification-row-title");
        Label meta = new Label(safe(commande.getTypeSang()) + " | "
                + commande.getQuantite() + " ml | " + safe(commande.getPriorite()));
        meta.getStyleClass().add("admin-notification-row-meta");
        if (lightPopup) {
            title.setStyle("-fx-text-fill: #111827; -fx-font-size: 13px; -fx-font-weight: 800;");
            meta.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        }

        VBox texts = new VBox(3, title, meta);
        HBox row = new HBox(10, icon, texts);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("admin-notification-row");
        if (lightPopup) {
            row.setStyle("-fx-background-color: #f5f7fb; -fx-background-radius: 8px; -fx-padding: 10 12; -fx-cursor: hand;");
            row.setOnMouseEntered(event -> row.setStyle("-fx-background-color: #fff1f1; -fx-background-radius: 8px; -fx-padding: 10 12; -fx-cursor: hand;"));
            row.setOnMouseExited(event -> row.setStyle("-fx-background-color: #f5f7fb; -fx-background-radius: 8px; -fx-padding: 10 12; -fx-cursor: hand;"));
        }
        row.setOnMouseClicked(event -> {
            popup.hide();
            navigateToCommandes(owner);
        });
        return row;
    }

    private static StackPane createNotificationMarker(boolean lightPopup) {
        Circle outer = new Circle(8);
        outer.setStyle("-fx-fill: " + (lightPopup ? "#fff1f1" : "rgba(230, 57, 57, 0.14)") + ";");

        Circle inner = new Circle(4);
        inner.setStyle("-fx-fill: #e63939;");

        StackPane marker = new StackPane(outer, inner);
        marker.setMinSize(18, 18);
        marker.setPrefSize(18, 18);
        marker.setMaxSize(18, 18);
        marker.getStyleClass().add("admin-notification-row-marker");
        return marker;
    }

    private static int getPendingCount() {
        try {
            return new CommandeService().countPendingCommandes();
        } catch (SQLException e) {
            System.err.println("Could not count pending commandes: " + e.getMessage());
            return 0;
        }
    }

    private static void applyLightPopupCardStyle(VBox popupCard) {
        popupCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; "
                + "-fx-border-color: rgba(15, 23, 42, 0.10); -fx-border-radius: 8px; "
                + "-fx-border-width: 1px; -fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.20), 24, 0, 0, 12); "
                + "-fx-min-width: 320px; -fx-pref-width: 320px;");
    }

    private static boolean isLightPopup(Node owner) {
        if (isPageVisuallyLight(owner)) {
            return true;
        }
        return "/commande-light.css".equals(resolveThemeCss(owner));
    }

    private static boolean isPageVisuallyLight(Node owner) {
        Scene scene = owner == null ? null : owner.getScene();
        if (scene == null || scene.getRoot() == null) {
            return false;
        }

        scene.getRoot().applyCss();
        Boolean sidebarLight = findLightnessByStyleClass(scene.getRoot(), "sidebar");
        if (sidebarLight != null) {
            return sidebarLight;
        }

        String[] surfaceStyleClasses = {
                "admin-center-with-topbar",
                "content-area",
                "main-content",
                "main-container"
        };
        for (String styleClass : surfaceStyleClasses) {
            Boolean light = findLightnessByStyleClass(scene.getRoot(), styleClass);
            if (light != null) {
                return light;
            }
        }

        return false;
    }

    private static Boolean findLightnessByStyleClass(Node node, String styleClass) {
        if (node == null) {
            return null;
        }

        if (node instanceof Region region && node.getStyleClass().contains(styleClass)) {
            Double brightness = backgroundBrightness(region);
            if (brightness != null) {
                return brightness > 0.72;
            }
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Boolean light = findLightnessByStyleClass(child, styleClass);
                if (light != null) {
                    return light;
                }
            }
        }

        return null;
    }

    private static Double backgroundBrightness(Region region) {
        if (region.getBackground() == null || region.getBackground().getFills().isEmpty()) {
            return null;
        }

        Paint fill = region.getBackground().getFills().get(0).getFill();
        if (fill instanceof Color color) {
            return color.getBrightness();
        }
        return null;
    }

    private static void configureNotificationDot(Circle notificationDot) {
        notificationDot.setRadius(5.5);
        notificationDot.setTranslateX(-4);
        notificationDot.setTranslateY(7);
        if (!notificationDot.getStyleClass().contains("admin-notification-dot")) {
            notificationDot.getStyleClass().add("admin-notification-dot");
        }
    }

    private static void applyPopupTheme(Node owner, Parent popupRoot) {
        if (popupRoot == null) {
            return;
        }
        String cssUrl = AdminTopBar.class.getResource(resolveThemeCss(owner)).toExternalForm();
        popupRoot.getStylesheets().setAll(cssUrl);
    }

    private static String resolveThemeCss(Node owner) {
        Scene scene = owner == null ? null : owner.getScene();
        if (scene != null) {
            for (String stylesheet : scene.getStylesheets()) {
                if (stylesheet != null && stylesheet.contains("commande-light.css")) {
                    return "/commande-light.css";
                }
            }
            if (hasStylesheet(owner, "commande-light.css") || hasStylesheet(scene.getRoot(), "commande-light.css")) {
                return "/commande-light.css";
            }
            for (String stylesheet : scene.getStylesheets()) {
                if (stylesheet != null && stylesheet.contains("commande.css")) {
                    return "/commande.css";
                }
            }
            if (hasStylesheet(owner, "commande.css") || hasStylesheet(scene.getRoot(), "commande.css")) {
                return "/commande.css";
            }
        }
        return ThemeManager.getInstance().isDarkMode() ? "/commande.css" : "/commande-light.css";
    }

    private static boolean hasStylesheet(Node node, String fileName) {
        Node current = node;
        while (current != null) {
            if (current instanceof Parent parent) {
                for (String stylesheet : parent.getStylesheets()) {
                    if (stylesheet != null && stylesheet.contains(fileName)) {
                        return true;
                    }
                }
            }
            current = current.getParent();
        }
        return false;
    }

    private static List<Commande> getRecentPendingCommandes() {
        try {
            return new CommandeService().recupererRecentPendingCommandes(RECENT_LIMIT);
        } catch (SQLException e) {
            System.err.println("Could not load pending commandes: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private static void navigateToCommandes(Node source) {
        try {
            AdminSidebarController.setCurrentPath("/AdminAfficherCommandes.fxml");
            FXMLLoader loader = new FXMLLoader(AdminTopBar.class.getResource("/AdminAfficherCommandes.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) source.getScene().getWindow();
            ThemeManager.getInstance().setScene(stage, root);
        } catch (IOException e) {
            System.err.println("Failed to navigate to AdminAfficherCommandes.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}
