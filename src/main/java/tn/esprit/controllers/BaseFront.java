package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.ChatbotService;
import tn.esprit.entities.User;
import tn.esprit.tools.SessionManager;
import tn.esprit.tools.ThemeManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BaseFront {

    @FXML protected HBox menuOverlay;
    @FXML protected Button menuToggleBtn;
    @FXML protected Label sessionEmailLabel;
    @FXML protected Button userNameBtn;
    @FXML protected VBox chatbotWindow;
    @FXML protected VBox chatbotMessages;
    @FXML protected ScrollPane chatbotScroll;
    @FXML protected TextField chatbotInput;
    @FXML protected Button chatbotToggleBtn;

    private final ChatbotService chatbotService = new ChatbotService();
    private final List<ChatbotService.ChatMessage> chatbotHistory = new ArrayList<>();
    private boolean chatbotInitialized;
    private HBox chatbotTypingRow;

    protected void switchScene(javafx.event.Event event, String fxmlPath) {
        switchSceneFromNode((Node) event.getSource(), fxmlPath);
    }

    protected void switchSceneFromNode(Node source, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof BaseFront frontController) {
                frontController.applySessionUser();
            }

            Stage stage = (Stage) source.getScene().getWindow();
            ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    protected void applySessionUser() {
        setupChatbot();

        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }

        String displayName = buildDisplayName(user);
        Button displayButton = userNameBtn != null ? userNameBtn : findButtonWithStyleClass(resolveRootNode(), "user-badge");
        if (displayButton != null) {
            displayButton.setText("\uD83D\uDC64 " + displayName);
            displayButton.setOnAction(this::goToProfile);
        }
        Button themeButton = findButtonWithStyleClass(resolveRootNode(), "theme-toggle-btn");
        ThemeManager.getInstance().updateToggleButton(themeButton);
        if (menuToggleBtn != null && !menuOverlayVisible()) {
            menuToggleBtn.setText("MENU \u2630");
        }
        if (sessionEmailLabel != null) {
            String email = user.getEmail();
            sessionEmailLabel.setText(email == null || email.isBlank() ? "Session: " + displayName : "Session: " + email);
        }
    }

    private boolean menuOverlayVisible() {
        return menuOverlay != null && menuOverlay.isVisible();
    }

    protected void setupChatbot() {
        if (chatbotInitialized || chatbotMessages == null) {
            return;
        }

        chatbotInitialized = true;
        addChatbotMessage(
                "Bonjour! Je suis l'assistant BloodLink. Je peux trouver des banques disponibles, verifier une commande ou vous aider a preparer un don.",
                "model",
                false
        );
    }

    @FXML
    protected void handleChatbotToggle(ActionEvent event) {
        setupChatbot();
        if (chatbotWindow == null) {
            return;
        }
        boolean willShow = !chatbotWindow.isVisible();
        chatbotWindow.setVisible(willShow);
        chatbotWindow.setManaged(willShow);
        if (willShow && chatbotInput != null) {
            Platform.runLater(() -> chatbotInput.requestFocus());
        }
    }

    @FXML
    protected void handleChatbotClose(ActionEvent event) {
        if (chatbotWindow != null) {
            chatbotWindow.setVisible(false);
            chatbotWindow.setManaged(false);
        }
    }

    @FXML
    protected void handleChatbotSubmit(ActionEvent event) {
        setupChatbot();
        if (chatbotInput == null || chatbotMessages == null) {
            return;
        }

        String text = chatbotInput.getText() == null ? "" : chatbotInput.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        chatbotInput.clear();
        List<ChatbotService.ChatMessage> historySnapshot = new ArrayList<>(chatbotHistory);
        addChatbotMessage(text, "user", true);
        showChatbotTyping();
        chatbotInput.setDisable(true);

        CompletableFuture
                .supplyAsync(() -> chatbotService.chat(text, historySnapshot))
                .whenComplete((response, throwable) -> Platform.runLater(() -> {
                    removeChatbotTyping();
                    chatbotInput.setDisable(false);
                    chatbotInput.requestFocus();
                    if (throwable != null) {
                        addChatbotMessage("Je n'arrive pas a repondre pour le moment. Reessayez dans un instant.", "model", true);
                    } else {
                        addChatbotMessage(response, "model", true);
                    }
                }));
    }

    private void addChatbotMessage(String text, String role, boolean remember) {
        if (chatbotMessages == null) {
            return;
        }

        boolean isUser = "user".equalsIgnoreCase(role);
        HBox row = new HBox(8);
        row.getStyleClass().add("chatbot-row");
        row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.TOP_LEFT);

        Label bubble = new Label(text == null ? "" : text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(285);
        bubble.getStyleClass().add(isUser ? "chatbot-user-bubble" : "chatbot-bot-bubble");

        if (isUser) {
            row.getChildren().add(bubble);
        } else {
            Label avatar = new Label("AI");
            avatar.getStyleClass().add("chatbot-avatar");
            row.getChildren().addAll(avatar, bubble);
        }

        chatbotMessages.getChildren().add(row);
        if (remember && text != null && !text.isBlank()) {
            chatbotHistory.add(new ChatbotService.ChatMessage(isUser ? "user" : "model", text));
        }
        scrollChatbotToBottom();
    }

    private void showChatbotTyping() {
        if (chatbotMessages == null) {
            return;
        }

        removeChatbotTyping();
        chatbotTypingRow = new HBox(8);
        chatbotTypingRow.setAlignment(Pos.TOP_LEFT);
        chatbotTypingRow.getStyleClass().add("chatbot-row");

        Label avatar = new Label("AI");
        avatar.getStyleClass().add("chatbot-avatar");
        Label bubble = new Label("...");
        bubble.getStyleClass().add("chatbot-bot-bubble");
        bubble.getStyleClass().add("chatbot-typing");
        chatbotTypingRow.getChildren().addAll(avatar, bubble);
        chatbotMessages.getChildren().add(chatbotTypingRow);
        scrollChatbotToBottom();
    }

    private void removeChatbotTyping() {
        if (chatbotTypingRow != null && chatbotMessages != null) {
            chatbotMessages.getChildren().remove(chatbotTypingRow);
        }
        chatbotTypingRow = null;
    }

    private void scrollChatbotToBottom() {
        if (chatbotScroll != null) {
            Platform.runLater(() -> chatbotScroll.setVvalue(1.0));
        }
    }

    private String buildDisplayName(User user) {
        String firstName = user.getPrenom() == null ? "" : user.getPrenom().trim();
        String lastName = user.getNom() == null ? "" : user.getNom().trim();
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isEmpty()) {
            return fullName;
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }
        return "Utilisateur";
    }

    private Node resolveRootNode() {
        Node node = userNameBtn != null ? userNameBtn : menuToggleBtn;
        if (node == null) {
            node = sessionEmailLabel;
        }
        while (node != null && node.getParent() != null) {
            node = node.getParent();
        }
        return node;
    }

    private Button findButtonWithStyleClass(Node node, String styleClass) {
        if (node instanceof Button button && button.getStyleClass().contains(styleClass)) {
            return button;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Button match = findButtonWithStyleClass(child, styleClass);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    @FXML
    protected void handleMenuToggle(ActionEvent event) {
        if (menuOverlay != null) {
            boolean isVisible = menuOverlay.isVisible();
            menuOverlay.setVisible(!isVisible);
            menuOverlay.setManaged(!isVisible);
            if (menuToggleBtn != null) {
                menuToggleBtn.setText(isVisible ? "MENU \u2630" : "FERMER \u2715");
            }
        }
    }

    @FXML
    protected void handleMenuClose(javafx.scene.input.MouseEvent event) {
        if (menuOverlay != null) {
            menuOverlay.setVisible(false);
            menuOverlay.setManaged(false);
            if (menuToggleBtn != null) {
                menuToggleBtn.setText("MENU \u2630");
            }
        }
    }

    @FXML
    protected void handleLogout(ActionEvent event) {
        SessionManager.clear();
        switchScene(event, "/login.fxml");
    }

    @FXML
    protected void handleThemeToggle(ActionEvent event) {
        ThemeManager themeManager = ThemeManager.getInstance();
        Node source = (Node) event.getSource();
        themeManager.toggleTheme(source.getScene());
        if (source instanceof Button button) {
            themeManager.updateToggleButton(button);
        }
    }

    @FXML
    public void goToAccueil(javafx.event.Event event) {
        switchScene(event, "/client_home.fxml");
    }

    @FXML
    public void goToCampagnes(javafx.event.Event event) {
        switchScene(event, "/ListeCampagnes.fxml");
    }

    @FXML
    public void goToCommandes(javafx.event.Event event) {
        switchScene(event, "/AfficherCommandes.fxml");
    }

    @FXML
    public void goToProfile(javafx.event.Event event) {
        switchScene(event, "/ClientProfile.fxml");
    }

    @FXML
    public void goToCalendar(javafx.event.Event event) {
        switchScene(event, "/CampagneCalendar.fxml");
    }

    @FXML
    public void goToHistorique(javafx.event.Event event) {
        switchScene(event, "/Liste.fxml");
    }
}
