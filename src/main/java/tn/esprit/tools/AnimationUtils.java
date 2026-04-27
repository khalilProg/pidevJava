package tn.esprit.tools;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class AnimationUtils {

    private static final String ENTRANCE_APPLIED_KEY = "bloodlink.entranceAnimationApplied";
    private static final String HOVER_APPLIED_KEY = "bloodlink.hoverAnimationApplied";

    public static void animateNode(Node node, double delayMillis) {
        if (node == null) return;
        if (Boolean.TRUE.equals(node.getProperties().get(ENTRANCE_APPLIED_KEY))) return;
        node.getProperties().put(ENTRANCE_APPLIED_KEY, Boolean.TRUE);

        // Set initial state
        node.setOpacity(0);
        node.setTranslateY(20);

        // Fade animation
        FadeTransition ft = new FadeTransition(Duration.millis(600), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delayMillis));
        ft.setInterpolator(Interpolator.EASE_OUT);

        // Slide animation
        TranslateTransition tt = new TranslateTransition(Duration.millis(600), node);
        tt.setFromY(20);
        tt.setToY(0);
        tt.setDelay(Duration.millis(delayMillis));
        tt.setInterpolator(Interpolator.EASE_OUT);

        ft.play();
        tt.play();
    }

    public static void applyHoverAnimation(Node node) {
        if (node == null) return;
        if (node instanceof Button button) {
            IconUtils.decorateButton(button);
        }
        if (Boolean.TRUE.equals(node.getProperties().get(HOVER_APPLIED_KEY))) return;
        node.getProperties().put(HOVER_APPLIED_KEY, Boolean.TRUE);

        ScaleTransition stIn = new ScaleTransition(Duration.millis(200), node);
        stIn.setToX(1.03);
        stIn.setToY(1.03);
        stIn.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition stOut = new ScaleTransition(Duration.millis(200), node);
        stOut.setToX(1.0);
        stOut.setToY(1.0);
        stOut.setInterpolator(Interpolator.EASE_OUT);

        node.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            stOut.stop();
            stIn.playFromStart();
        });

        node.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            stIn.stop();
            stOut.playFromStart();
        });
    }

    public static void applyReferenceAnimations(Parent root) {
        if (root == null) return;
        int[] delay = {80};
        applyReferenceAnimations(root, delay);
    }

    private static void applyReferenceAnimations(Node node, int[] delay) {
        if (node == null) return;

        if (!isInSidebar(node)) {
            if (node instanceof Button) {
                applyHoverAnimation(node);
            }

            if (shouldAnimate(node)) {
                animateNode(node, Math.min(delay[0], 520));
                delay[0] += 60;
            }
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyReferenceAnimations(child, delay);
            }
        }
    }

    private static boolean shouldAnimate(Node node) {
        return node instanceof TextInputControl
                || node instanceof ComboBoxBase
                || node instanceof DatePicker
                || node instanceof Spinner
                || node instanceof TableView
                || hasAnyStyle(node,
                    "dark-field",
                    "text-field-custom",
                    "transparent-input",
                    "dark-combo",
                    "submit-button",
                    "cancel-button",
                    "retour-button",
                    "button-primary",
                    "btn-primary",
                    "btn-primary-modern",
                    "nouveau-btn",
                    "filter-btn",
                    "card-btn-outline",
                    "toggle-blood-type",
                    "glass-card",
                    "form-card",
                    "dark-table",
                    "data-table"
                );
    }

    private static boolean isInSidebar(Node node) {
        Node current = node;
        while (current != null) {
            if (hasAnyStyle(current,
                    "sidebar",
                    "admin-sidebar",
                    "sidebar-root",
                    "sidebar-nav",
                    "sidebar-nav-item",
                    "sidebar-nav-item-active",
                    "nav-item",
                    "nav-button")) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private static boolean hasAnyStyle(Node node, String... styleClasses) {
        for (String styleClass : styleClasses) {
            if (node.getStyleClass().contains(styleClass)) {
                return true;
            }
        }
        return false;
    }
}
