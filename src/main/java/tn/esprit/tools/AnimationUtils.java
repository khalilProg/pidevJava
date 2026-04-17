package tn.esprit.tools;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtils {

    public static void animateNode(Node node, double delayMillis) {
        if (node == null) return;

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

        ScaleTransition stIn = new ScaleTransition(Duration.millis(200), node);
        stIn.setToX(1.03);
        stIn.setToY(1.03);
        stIn.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition stOut = new ScaleTransition(Duration.millis(200), node);
        stOut.setToX(1.0);
        stOut.setToY(1.0);
        stOut.setInterpolator(Interpolator.EASE_OUT);

        node.setOnMouseEntered(e -> {
            stOut.stop();
            stIn.playFromStart();
        });

        node.setOnMouseExited(e -> {
            stIn.stop();
            stOut.playFromStart();
        });
    }
}
