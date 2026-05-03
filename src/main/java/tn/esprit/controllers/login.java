package tn.esprit.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

import org.mindrot.jbcrypt.BCrypt;

import tn.esprit.entities.User;
import tn.esprit.services.FaceIdService;
import tn.esprit.services.UserService;
import tn.esprit.tools.SessionManager;

public class login {

    @FXML
    private TextField emailF;

    @FXML
    private CheckBox rememberMeCheckbox;

    @FXML
    private PasswordField passF;

    @FXML
    private Label errorLabel;

    @FXML
    private Label googleLoadingLabel;

    @FXML
    public void initialize() {
        Preferences prefs = Preferences.userNodeForPackage(login.class);
        String savedEmail = prefs.get("saved_email", "");
        if (!savedEmail.isEmpty()) {
            emailF.setText(savedEmail);
            rememberMeCheckbox.setSelected(true);
        }
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailF.getText();
        String password = passF.getText();
        errorLabel.setVisible(false);

        if (email.isEmpty() || password.isEmpty()) {
            displayError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            UserService userService = new UserService();
            List<User> users = userService.recuperer();
            boolean found = false;

            for (User u : users) {
                if (u.getEmail().equals(email)) {
                    String hash = u.getPassword();
                    boolean passMatch = false;

                    // Support Symfony $2y$ BCrypt format by converting the prefix for jBcrypt
                    if (hash != null && hash.startsWith("$2y$")) {
                        hash = "$2a$" + hash.substring(4);
                        passMatch = BCrypt.checkpw(password, hash);
                    } else if (hash != null && hash.startsWith("$2a$")) {
                        passMatch = BCrypt.checkpw(password, hash);
                    } else if (password.equals(hash)) {
                        passMatch = true; // Support pure plain-text registrations from the desktop app
                    }

                    if (passMatch) {
                        found = true;
                        SessionManager.setCurrentUser(u);
                        
                        Preferences prefs = Preferences.userNodeForPackage(login.class);
                        if (rememberMeCheckbox.isSelected()) {
                            prefs.put("saved_email", email);
                        } else {
                            prefs.remove("saved_email");
                        }

                        // Check if the user is an admin
                        if ("admin".equalsIgnoreCase(u.getRole())) {
                            navigateToDashboard(event);
                        } else if (u.getRole() != null && u.getRole().toLowerCase().contains("cnts")) {
                            navigateToCntsAgentHome(event, u);
                        } else if (u.getRole() != null && u.getRole().toLowerCase().contains("banque")) {
                            navigateToAgentBanque(event, u);
                        } else {
                            navigateToClientHome(event, u);
                        }
                        break;
                    }
                }
            }

            if (!found) {
                displayError("Email ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayError("Erreur réseau: " + e.getMessage());
        }
    }

    private void navigateToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin_dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BloodLink - Dashboard Administration");
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToAgentBanque(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgentBanqueBase.fxml"));
            Parent root = loader.load();
            
            // Pass user to controller
            AgentBanqueBaseController controller = loader.getController();
            controller.initData(user);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BloodLink - Espace Banque");
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToCntsAgentHome(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cnts_agent_home.fxml"));
            Parent root = loader.load();
            CntsAgentHomeController controller = loader.getController();
            controller.initData(user);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BloodLink - Espace CNTS");
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToClientHome(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_home.fxml"));
            Parent root = loader.load();
            ClientHomeController controller = loader.getController();
            controller.initData(user);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BloodLink - Accueil");
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    void handleGoToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/register.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleMenuToggle(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/custom_menu.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BloodLink - Menu");
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleForgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/forgot_password.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleGoogleSignIn(ActionEvent event) {
        errorLabel.setVisible(false);
        googleLoadingLabel.setVisible(true);
        googleLoadingLabel.setManaged(true);
        
        // Run OAuth flow in a background thread to prevent freezing the UI
        new Thread(() -> {
            try {
                tn.esprit.services.GoogleOAuthService oauthService = new tn.esprit.services.GoogleOAuthService();
                tn.esprit.services.GoogleOAuthService.GoogleUserInfo userInfo = oauthService.authenticate();
                
                // Switch back to JavaFX Application Thread for UI/DB updates
                javafx.application.Platform.runLater(() -> {
                    try {
                        UserService userService = new UserService();
                        User u = userService.findByEmail(userInfo.email);
                        
                        if (u == null) {
                            // User doesn't exist, create a new one with a random placeholder password
                            String randomPassword = java.util.UUID.randomUUID().toString();
                            u = new User(userInfo.email, userInfo.familyName, userInfo.givenName, randomPassword, "client", "");
                            userService.ajouter(u);
                            System.out.println("Google Auth: Created new user " + userInfo.email);
                        }
                        
                        // Proceed to login
                        SessionManager.setCurrentUser(u);
                        
                        if ("admin".equalsIgnoreCase(u.getRole())) {
                            navigateToDashboard(event);
                        } else if (u.getRole() != null && u.getRole().toLowerCase().contains("cnts")) {
                            navigateToCntsAgentHome(event, u);
                        } else if (u.getRole() != null && u.getRole().toLowerCase().contains("banque")) {
                            navigateToAgentBanque(event, u);
                        } else {
                            navigateToClientHome(event, u);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        displayError("Erreur base de données: " + e.getMessage());
                    } finally {
                        googleLoadingLabel.setVisible(false);
                        googleLoadingLabel.setManaged(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    googleLoadingLabel.setVisible(false);
                    googleLoadingLabel.setManaged(false);
                    displayError("Erreur Google SignIn: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    void handleFaceIdLogin(ActionEvent event) {
        errorLabel.setVisible(false);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = stage.getScene();

        // ── Build the Apple Face ID overlay ──────────────────────────────

        // Dark background
        Rectangle overlay = new Rectangle(scene.getWidth(), scene.getHeight());
        overlay.setFill(Color.rgb(0, 0, 0, 0.94));
        overlay.widthProperty().bind(scene.widthProperty());
        overlay.heightProperty().bind(scene.heightProperty());

        // Face scanning frame (Apple-style rounded square)
        double frameSize = 200;
        Rectangle frame = new Rectangle(frameSize, frameSize);
        frame.setFill(Color.TRANSPARENT);
        frame.setStroke(Color.WHITE);
        frame.setStrokeWidth(3);
        frame.setArcWidth(50);
        frame.setArcHeight(50);
        frame.setStrokeLineCap(StrokeLineCap.ROUND);
        frame.setStrokeLineJoin(StrokeLineJoin.ROUND);
        frame.setOpacity(0.8);

        // Glow effect on the frame
        DropShadow frameGlow = new DropShadow();
        frameGlow.setColor(Color.rgb(100, 160, 255, 0.5));
        frameGlow.setRadius(25);
        frameGlow.setSpread(0.1);
        frame.setEffect(frameGlow);

        // Scanning line (moves up and down inside the frame)
        Line scanLine = new Line(-frameSize / 2 + 20, 0, frameSize / 2 - 20, 0);
        scanLine.setStroke(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.3, Color.rgb(100, 160, 255, 0.8)),
                new Stop(0.5, Color.rgb(120, 180, 255, 1.0)),
                new Stop(0.7, Color.rgb(100, 160, 255, 0.8)),
                new Stop(1, Color.TRANSPARENT)));
        scanLine.setStrokeWidth(2.5);
        scanLine.setStrokeLineCap(StrokeLineCap.ROUND);

        // Face icon above frame
        Label faceIcon = new Label("👤");
        faceIcon.setStyle("-fx-font-size: 48px;");

        // Status text
        Label statusLabel = new Label("Recherche de visage...");
        statusLabel.getStyleClass().add("faceid-status-text");

        Label hintLabel = new Label("Placez votre visage dans le cadre");
        hintLabel.getStyleClass().add("faceid-hint-text");

        // Cancel button
        Button cancelBtn = new Button("Annuler");
        cancelBtn.getStyleClass().add("faceid-cancel-btn");

        // Frame + scan line stack
        StackPane frameStack = new StackPane(frame, scanLine);
        frameStack.setMaxSize(frameSize, frameSize);
        frameStack.setPrefSize(frameSize, frameSize);

        // Layout
        VBox content = new VBox(20, faceIcon, frameStack, statusLabel, hintLabel, cancelBtn);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        StackPane overlayPane = new StackPane(overlay, content);
        overlayPane.setAlignment(Pos.CENTER);
        overlayPane.setStyle("-fx-background-color: transparent;");

        // Add overlay to the scene's root
        AnchorPane root = (AnchorPane) scene.getRoot();
        AnchorPane.setTopAnchor(overlayPane, 0.0);
        AnchorPane.setBottomAnchor(overlayPane, 0.0);
        AnchorPane.setLeftAnchor(overlayPane, 0.0);
        AnchorPane.setRightAnchor(overlayPane, 0.0);
        root.getChildren().add(overlayPane);

        // ── Animations ──────────────────────────────────────────────────

        // 1. Fade-in the overlay
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), overlayPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        overlayPane.setOpacity(0);
        fadeIn.play();

        // 2. Scale-pulse the frame
        ScaleTransition framePulse = new ScaleTransition(Duration.millis(1200), frame);
        framePulse.setFromX(0.95);
        framePulse.setFromY(0.95);
        framePulse.setToX(1.05);
        framePulse.setToY(1.05);
        framePulse.setCycleCount(Animation.INDEFINITE);
        framePulse.setAutoReverse(true);
        framePulse.setInterpolator(Interpolator.EASE_BOTH);
        framePulse.play();

        // 3. Scan line animation (moves up and down)
        TranslateTransition scanAnim = new TranslateTransition(Duration.millis(1500), scanLine);
        scanAnim.setFromY(-frameSize / 2 + 15);
        scanAnim.setToY(frameSize / 2 - 15);
        scanAnim.setCycleCount(Animation.INDEFINITE);
        scanAnim.setAutoReverse(true);
        scanAnim.setInterpolator(Interpolator.EASE_BOTH);
        scanAnim.play();

        // 4. Frame glow color rotation
        Timeline glowAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(frameGlow.colorProperty(), Color.rgb(100, 160, 255, 0.5))),
                new KeyFrame(Duration.millis(1500),
                        new KeyValue(frameGlow.colorProperty(), Color.rgb(160, 100, 255, 0.5))),
                new KeyFrame(Duration.millis(3000),
                        new KeyValue(frameGlow.colorProperty(), Color.rgb(100, 160, 255, 0.5)))
        );
        glowAnim.setCycleCount(Animation.INDEFINITE);
        glowAnim.play();

        // ── Cancel handler ──────────────────────────────────────────────
        final boolean[] cancelled = {false};
        cancelBtn.setOnAction(e -> {
            cancelled[0] = true;
            framePulse.stop();
            scanAnim.stop();
            glowAnim.stop();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlayPane);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> root.getChildren().remove(overlayPane));
            fadeOut.play();
        });

        // ── Run Face ID verification in background ──────────────────────
        new Thread(() -> {
            FaceIdService faceIdService = new FaceIdService();

            // Update status on FX thread
            Platform.runLater(() -> {
                statusLabel.setText("Analyse en cours...");
                hintLabel.setText("Ne bougez pas");
            });

            FaceIdService.FaceIdResult result = faceIdService.verify();

            if (cancelled[0]) return;

            Platform.runLater(() -> {
                // Stop scanning animations
                framePulse.stop();
                scanAnim.stop();
                glowAnim.stop();
                scanLine.setVisible(false);

                if (result.match && result.email != null) {
                    // ── SUCCESS ──────────────────────────────────────────
                    frame.setStroke(Color.rgb(76, 217, 100));
                    frameGlow.setColor(Color.rgb(76, 217, 100, 0.6));
                    statusLabel.setText("✓ Visage reconnu");
                    statusLabel.setStyle("-fx-text-fill: #4cd964; -fx-font-size: 20px; -fx-font-weight: bold;");
                    hintLabel.setText(String.format("Confiance: %.0f%%", result.confidence * 100));
                    cancelBtn.setVisible(false);

                    // Scale-up success animation on the frame
                    ScaleTransition successPulse = new ScaleTransition(Duration.millis(300), frame);
                    successPulse.setToX(1.15);
                    successPulse.setToY(1.15);
                    successPulse.setAutoReverse(true);
                    successPulse.setCycleCount(2);
                    successPulse.play();

                    // Navigate after a short delay
                    PauseTransition delay = new PauseTransition(Duration.millis(1500));
                    delay.setOnFinished(ev -> {
                        root.getChildren().remove(overlayPane);
                        try {
                            UserService userService = new UserService();
                            User u = userService.findByEmail(result.email);
                            if (u != null) {
                                SessionManager.setCurrentUser(u);
                                if ("admin".equalsIgnoreCase(u.getRole())) {
                                    navigateToDashboard(event);
                                } else if (u.getRole() != null && u.getRole().toLowerCase().contains("cnts")) {
                                    navigateToCntsAgentHome(event, u);
                                } else if (u.getRole() != null && u.getRole().toLowerCase().contains("banque")) {
                                    navigateToAgentBanque(event, u);
                                } else {
                                    navigateToClientHome(event, u);
                                }
                            } else {
                                displayError("Utilisateur non trouvé dans la base de données.");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            displayError("Erreur base de données: " + ex.getMessage());
                        }
                    });
                    delay.play();
                } else {
                    // ── FAILURE ──────────────────────────────────────────
                    frame.setStroke(Color.rgb(231, 76, 60));
                    frameGlow.setColor(Color.rgb(231, 76, 60, 0.6));
                    String errorMsg = result.error != null ? result.error : "Visage non reconnu";
                    statusLabel.setText("✗ " + errorMsg);
                    statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 18px; -fx-font-weight: bold;");
                    hintLabel.setText("Veuillez réessayer ou utiliser un autre mode de connexion");

                    // Shake animation on failure
                    TranslateTransition shake = new TranslateTransition(Duration.millis(80), frame);
                    shake.setFromX(-10);
                    shake.setToX(10);
                    shake.setCycleCount(6);
                    shake.setAutoReverse(true);
                    shake.setOnFinished(ev -> frame.setTranslateX(0));
                    shake.play();

                    // Auto-dismiss after 3 seconds
                    PauseTransition autoDismiss = new PauseTransition(Duration.millis(3000));
                    autoDismiss.setOnFinished(ev -> {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlayPane);
                        fadeOut.setToValue(0);
                        fadeOut.setOnFinished(ev2 -> root.getChildren().remove(overlayPane));
                        fadeOut.play();
                    });
                    autoDismiss.play();
                }
            });
        }).start();
    }
}
