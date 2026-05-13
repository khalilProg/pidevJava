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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.ChatbotService;
import tn.esprit.entities.User;
import tn.esprit.tools.IconUtils;
import tn.esprit.tools.SessionManager;
import tn.esprit.tools.ThemeManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class BaseFront {
    private static final String ACTIVE_PAGE_KEY = "bloodlink.activePage";
    private static final String NAV_ACTIVE_CLASS = "nav-link-active";
    private static final String MENU_ACTIVE_CLASS = "menu-link-active";

    @FXML protected HBox menuOverlay;
    @FXML protected Button menuToggleBtn;
    @FXML protected Label sessionEmailLabel;
    @FXML protected Label userNameLabel;
    @FXML protected Button userNameBtn;
    @FXML protected Button accueilNavBtn;
    @FXML protected Button historiqueNavBtn;
    @FXML protected Button campagnesNavBtn;

    // ⬅️ YOUR MODULE BUTTONS ADDED HERE
    @FXML protected Button dossierMedNavBtn;
    @FXML protected Button donsNavBtn;

    @FXML protected VBox chatbotWindow;
    @FXML protected VBox chatbotMessages;
    @FXML protected ScrollPane chatbotScroll;
    @FXML protected TextField chatbotInput;
    @FXML protected Button chatbotToggleBtn;

    private final ChatbotService chatbotService = new ChatbotService();
    private final List<ChatbotService.ChatMessage> chatbotHistory = new ArrayList<>();
    private boolean chatbotInitialized;
    private HBox chatbotTypingRow;

    @FXML
    public void initialize() {
        Platform.runLater(this::applySessionUser);
    }

    protected void switchScene(javafx.event.Event event, String fxmlPath) {
        switchSceneFromNode((Node) event.getSource(), fxmlPath);
    }

    protected void switchSceneFromNode(Node source, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            String activePage = activePageForPath(fxmlPath);
            if (!activePage.isBlank()) {
                root.getProperties().put(ACTIVE_PAGE_KEY, activePage);
            }
            Object controller = loader.getController();
            if (controller instanceof BaseFront frontController) {
                frontController.applySessionUser();
                frontController.applyCurrentPageState();
            }

            Stage stage = (Stage) source.getScene().getWindow();
            String title = titleForPath(fxmlPath);
            if (!title.isBlank()) {
                stage.setTitle(title);
            }
            ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    protected void applySessionUser() {
        setupChatbot();
        applyCurrentPageState();

        User user = SessionManager.getCurrentUser();
        if (user == null) {
            return;
        }

        String displayName = buildDisplayName(user);
        if (userNameLabel != null) {
            userNameLabel.setText(displayName);
        }
        Button displayButton = userNameBtn != null ? userNameBtn : findButtonWithStyleClass(resolveRootNode(), "user-badge");
        if (displayButton != null) {
            displayButton.setText("\uD83D\uDC64 " + displayName);
            IconUtils.decorateButton(displayButton);
            displayButton.setOnAction(this::goToProfile);
        }
        Button themeButton = findButtonWithStyleClass(resolveRootNode(), "theme-toggle-btn");
        ThemeManager.getInstance().updateToggleButton(themeButton);
        if (menuToggleBtn != null && !menuOverlayVisible()) {
            menuToggleBtn.setText("MENU");
            IconUtils.decorateButton(menuToggleBtn);
        }
        if (sessionEmailLabel != null) {
            String email = user.getEmail();
            sessionEmailLabel.setText(email == null || email.isBlank() ? "Session: " + displayName : "Session: " + email);
        }

        // Bind and filter dynamic menu events from included FXML
        if (menuOverlay != null) {
            Button menuAccueilBtn = (Button) menuOverlay.lookup("#menuAccueilBtn");
            Button menuCampagnesBtn = (Button) menuOverlay.lookup("#menuCampagnesBtn");
            Button menuEntitesBtn = (Button) menuOverlay.lookup("#menuEntitesBtn");
            Button menuHistoriqueBtn = (Button) menuOverlay.lookup("#menuHistoriqueBtn");
            Button menuCommandesBtn = (Button) menuOverlay.lookup("#menuCommandesBtn");
            Button menuDonsBtn = (Button) menuOverlay.lookup("#menuDonsBtn");
            Button menuDossierMedBtn = (Button) menuOverlay.lookup("#menuDossierMedBtn");
            Button menuProfilBtn = (Button) menuOverlay.lookup("#menuProfilBtn");
            Button menuDemandesBtn = (Button) menuOverlay.lookup("#menuDemandesBtn");
            Button menuTransfertsBtn = (Button) menuOverlay.lookup("#menuTransfertsBtn");
            Button menuFermerBtn = (Button) menuOverlay.lookup("#menuFermerBtn");
            Button menuLogoutBtn = (Button) menuOverlay.lookup("#menuLogoutBtn");
            Region menuBackdrop = (Region) menuOverlay.lookup("#menuBackdrop");
            Label sessionEmailLabelOverlay = (Label) menuOverlay.lookup("#sessionEmailLabel");

            if (menuAccueilBtn != null) menuAccueilBtn.setOnAction(this::goToAccueil);
            if (menuCampagnesBtn != null) menuCampagnesBtn.setOnAction(this::goToCampagnes);
            if (menuEntitesBtn != null) menuEntitesBtn.setOnAction(this::goToEntites);
            if (menuHistoriqueBtn != null) menuHistoriqueBtn.setOnAction(this::goToHistorique);
            if (menuCommandesBtn != null) menuCommandesBtn.setOnAction(this::goToCommandes);
            if (menuDonsBtn != null) menuDonsBtn.setOnAction(this::goToDons);
            if (menuDossierMedBtn != null) menuDossierMedBtn.setOnAction(this::goToDossierMed);
            if (menuProfilBtn != null) menuProfilBtn.setOnAction(this::goToProfile);
            if (menuDemandesBtn != null) menuDemandesBtn.setOnAction(this::goToDemandes);
            if (menuTransfertsBtn != null) menuTransfertsBtn.setOnAction(this::goToTransferts);
            if (menuFermerBtn != null) menuFermerBtn.setOnAction(this::handleMenuToggle);
            if (menuLogoutBtn != null) menuLogoutBtn.setOnAction(this::handleLogout);
            if (menuBackdrop != null) menuBackdrop.setOnMouseClicked(this::handleMenuClose);
            
            if (sessionEmailLabelOverlay != null) {
                String email = user.getEmail();
                sessionEmailLabelOverlay.setText(email == null || email.isBlank() ? "Session: " + displayName : "Session: " + email);
            }

            boolean isCnts = user.getRole() != null && user.getRole().toLowerCase().contains("cnts");
            boolean isBanque = user.getRole() != null && user.getRole().toLowerCase().contains("banque");

            if (isBanque) {
                if (menuAccueilBtn != null) { menuAccueilBtn.setVisible(false); menuAccueilBtn.setManaged(false); }
                if (menuCampagnesBtn != null) { menuCampagnesBtn.setVisible(false); menuCampagnesBtn.setManaged(false); }
                if (menuEntitesBtn != null) { menuEntitesBtn.setVisible(false); menuEntitesBtn.setManaged(false); }
                if (menuHistoriqueBtn != null) { menuHistoriqueBtn.setVisible(false); menuHistoriqueBtn.setManaged(false); }
                if (menuCommandesBtn != null) { menuCommandesBtn.setVisible(false); menuCommandesBtn.setManaged(false); }
                if (menuDonsBtn != null) { menuDonsBtn.setVisible(false); menuDonsBtn.setManaged(false); }
                if (menuDossierMedBtn != null) { menuDossierMedBtn.setVisible(false); menuDossierMedBtn.setManaged(false); }
                if (menuDemandesBtn != null) { menuDemandesBtn.setVisible(true); menuDemandesBtn.setManaged(true); }
                if (menuTransfertsBtn != null) { menuTransfertsBtn.setVisible(true); menuTransfertsBtn.setManaged(true); }
                if (menuProfilBtn != null) { menuProfilBtn.setVisible(true); menuProfilBtn.setManaged(true); }
            } else if (isCnts) {
                if (menuAccueilBtn != null) { menuAccueilBtn.setVisible(false); menuAccueilBtn.setManaged(false); }
                if (menuCampagnesBtn != null) { menuCampagnesBtn.setVisible(true); menuCampagnesBtn.setManaged(true); }
                if (menuEntitesBtn != null) { menuEntitesBtn.setVisible(true); menuEntitesBtn.setManaged(true); }
                if (menuHistoriqueBtn != null) { menuHistoriqueBtn.setVisible(false); menuHistoriqueBtn.setManaged(false); }
                if (menuCommandesBtn != null) { menuCommandesBtn.setVisible(false); menuCommandesBtn.setManaged(false); }
                if (menuDonsBtn != null) { menuDonsBtn.setVisible(false); menuDonsBtn.setManaged(false); }
                if (menuDossierMedBtn != null) { menuDossierMedBtn.setVisible(false); menuDossierMedBtn.setManaged(false); }
                if (menuDemandesBtn != null) { menuDemandesBtn.setVisible(false); menuDemandesBtn.setManaged(false); }
                if (menuTransfertsBtn != null) { menuTransfertsBtn.setVisible(false); menuTransfertsBtn.setManaged(false); }
                if (menuProfilBtn != null) { menuProfilBtn.setVisible(true); menuProfilBtn.setManaged(true); }
            } else {
                if (menuAccueilBtn != null) { menuAccueilBtn.setVisible(true); menuAccueilBtn.setManaged(true); }
                if (menuCampagnesBtn != null) { menuCampagnesBtn.setVisible(true); menuCampagnesBtn.setManaged(true); }
                if (menuEntitesBtn != null) { menuEntitesBtn.setVisible(false); menuEntitesBtn.setManaged(false); }
                if (menuHistoriqueBtn != null) { menuHistoriqueBtn.setVisible(true); menuHistoriqueBtn.setManaged(true); }
                if (menuCommandesBtn != null) { menuCommandesBtn.setVisible(true); menuCommandesBtn.setManaged(true); }
                if (menuDonsBtn != null) { menuDonsBtn.setVisible(true); menuDonsBtn.setManaged(true); }
                if (menuDossierMedBtn != null) { menuDossierMedBtn.setVisible(true); menuDossierMedBtn.setManaged(true); }
                if (menuDemandesBtn != null) { menuDemandesBtn.setVisible(false); menuDemandesBtn.setManaged(false); }
                if (menuTransfertsBtn != null) { menuTransfertsBtn.setVisible(false); menuTransfertsBtn.setManaged(false); }
                if (menuProfilBtn != null) { menuProfilBtn.setVisible(true); menuProfilBtn.setManaged(true); }
            }
        }
    }

    protected void applyCurrentPageState() {
        String activePage = resolveActivePage();
        applyActiveNavigation(activePage);
        applyActiveMenuLinks(resolveRootNode(), activePage);
    }

    private void applyActiveNavigation(String activePage) {
        setActiveStyle(accueilNavBtn, "accueil".equals(activePage), NAV_ACTIVE_CLASS);
        setActiveStyle(historiqueNavBtn, "historique".equals(activePage), NAV_ACTIVE_CLASS);
        setActiveStyle(campagnesNavBtn, "campagnes".equals(activePage), NAV_ACTIVE_CLASS);

        // ⬅️ YOUR MODULES ADDED TO ACTIVE STYLING LOGIC
        setActiveStyle(dossierMedNavBtn, "dossier".equals(activePage), NAV_ACTIVE_CLASS);
        setActiveStyle(donsNavBtn, "dons".equals(activePage), NAV_ACTIVE_CLASS);
    }

    private void applyActiveMenuLinks(Node node, String activePage) {
        if (node == null || activePage == null || activePage.isBlank()) {
            return;
        }
        if (node instanceof Button button && button.getStyleClass().contains("menu-link")) {
            setActiveStyle(button, activePage.equals(pageForMenuButton(button)), MENU_ACTIVE_CLASS);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyActiveMenuLinks(child, activePage);
            }
        }
    }

    private void setActiveStyle(Button button, boolean active, String activeClass) {
        if (button == null) {
            return;
        }
        if (active) {
            if (!button.getStyleClass().contains(activeClass)) {
                button.getStyleClass().add(activeClass);
            }
        } else {
            button.getStyleClass().remove(activeClass);
        }
        IconUtils.decorateButton(button);
    }

    private String resolveActivePage() {
        Node rootNode = resolveRootNode();
        if (rootNode == null) {
            return "";
        }

        Object page = rootNode.getProperties().get(ACTIVE_PAGE_KEY);
        if (page instanceof String pageName && !pageName.isBlank()) {
            return pageName;
        }
        if (rootNode.getScene() != null && rootNode.getScene().getRoot() != null) {
            Object scenePage = rootNode.getScene().getRoot().getProperties().get(ACTIVE_PAGE_KEY);
            if (scenePage instanceof String pageName && !pageName.isBlank()) {
                return pageName;
            }
            if (rootNode.getScene().getWindow() instanceof Stage stage) {
                return activePageFromTitle(stage.getTitle());
            }
        }
        return "";
    }

    private String pageForMenuButton(Button button) {
        String text = safeLower(button.getText());
        if (text.contains("accueil")) {
            return "accueil";
        }
        if (text.contains("campagnes")) {
            return "campagnes";
        }
        if (text.contains("commandes")) {
            return "commandes";
        }
        // ⬅️ YOUR MENU LINK LOGIC (from mobile/dropdown menu)
        if (text.contains("profil médical")) {
            return "dossier";
        }
        if (text.contains("mes dons")) {
            return "dons";
        }
        if (text.contains("profil")) { // This is the team's generic profile
            return "profil";
        }
        if (text.contains("historique")) { // This is the team's history (appointments)
            return "historique";
        }
        return "";
    }

    private String activePageForPath(String fxmlPath) {
        String path = safeLower(fxmlPath);

        // ⬅️ YOUR PATH-TO-PAGE MAPPING
        if (path.contains("frontmedicalprofile")) {
            return "dossier";
        }
        if (path.contains("frontdonationhistory")) {
            return "dons";
        }

        // Team's existing mappings
        if (path.contains("client_home") || path.endsWith("/home.fxml")) {
            return "accueil";
        }
        if (path.contains("listecampagnes") || path.contains("campagnefront") || path.contains("campagnecalendar")
                || path.contains("ajouterquestionnaire") || path.contains("ajouterrendezvous")) {
            return "campagnes";
        }
        if (path.contains("affichercommandes") || path.contains("ajoutcommande")) {
            return "commandes";
        }
        if (path.endsWith("liste.fxml")) {
            return "historique";
        }
        if (path.contains("clientprofile")) {
            return "profil";
        }
        return "";
    }

    private String titleForPath(String fxmlPath) {
        return switch (activePageForPath(fxmlPath)) {
            case "accueil" -> "BloodLink - Accueil";
            case "campagnes" -> "BloodLink - Campagnes";
            case "commandes" -> "BloodLink - Commandes";
            case "historique" -> "BloodLink - Historique";
            case "profil" -> "BloodLink - Profil";
            // ⬅️ YOUR PAGE-TO-TITLE MAPPING
            case "dossier" -> "BloodLink - Profil Médical";
            case "dons" -> "BloodLink - Mes Dons";
            default -> safeLower(fxmlPath).contains("login") ? "BloodLink - Connexion" : "";
        };
    }

    private String activePageFromTitle(String title) {
        String normalized = safeLower(title);

        // ⬅️ YOUR TITLE-TO-PAGE MAPPING (put specific ones first)
        if (normalized.contains("profil médical")) {
            return "dossier";
        }
        if (normalized.contains("mes dons")) {
            return "dons";
        }

        // Team's existing mappings
        if (normalized.contains("accueil")) {
            return "accueil";
        }
        if (normalized.contains("campagne")) {
            return "campagnes";
        }
        if (normalized.contains("commande")) {
            return "commandes";
        }
        if (normalized.contains("historique") || normalized.contains("don")) {
            return "historique";
        }
        if (normalized.contains("profil")) {
            return "profil";
        }
        return "";
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
        if (node == null) { node = sessionEmailLabel; }
        if (node == null) { node = userNameLabel; }
        if (node == null) { node = accueilNavBtn; }
        if (node == null) { node = historiqueNavBtn; }
        if (node == null) { node = campagnesNavBtn; }

        // ⬅️ YOUR BUTTONS ADDED TO NODE RESOLVER
        if (node == null) { node = dossierMedNavBtn; }
        if (node == null) { node = donsNavBtn; }

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

    private String safeLower(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    @FXML
    protected void handleMenuToggle(ActionEvent event) {
        if (menuOverlay != null) {
            boolean isVisible = menuOverlay.isVisible();
            menuOverlay.setVisible(!isVisible);
            menuOverlay.setManaged(!isVisible);
            if (menuToggleBtn != null) {
                menuToggleBtn.setText(isVisible ? "MENU" : "FERMER");
                IconUtils.decorateButton(menuToggleBtn);
            }
        }
    }

    @FXML
    protected void handleMenuClose(javafx.scene.input.MouseEvent event) {
        if (menuOverlay != null) {
            menuOverlay.setVisible(false);
            menuOverlay.setManaged(false);
            if (menuToggleBtn != null) {
                menuToggleBtn.setText("MENU");
                IconUtils.decorateButton(menuToggleBtn);
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
        User user = SessionManager.getCurrentUser();
        boolean isCnts = user != null && user.getRole() != null && user.getRole().toLowerCase().contains("cnts");
        if (isCnts) {
            switchScene(event, "/cnts_agent_home.fxml");
        } else {
            switchScene(event, "/ListeCampagnes.fxml");
        }
    }

    @FXML
    public void goToCommandes(javafx.event.Event event) {
        switchScene(event, "/AfficherCommandes.fxml");
    }

    @FXML
    public void goToDemandes(javafx.event.Event event) {
        // Overridden in child classes (AgentBanqueBaseController)
    }

    @FXML
    public void goToTransferts(javafx.event.Event event) {
        // Overridden in child classes (AgentBanqueBaseController)
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

    @FXML
    public void goToEntites(javafx.event.Event event) {
        switchScene(event, "/EntiteCollecteFront.fxml");
    }

    // ⬅️ YOUR MODULE NAVIGATION METHODS
    @FXML
    public void goToDossierMed(javafx.event.Event event) {
        switchScene(event, "/FrontMedicalProfile.fxml");
    }

    @FXML
    public void goToDons(javafx.event.Event event) {
        switchScene(event, "/FrontDonationHistory.fxml");
    }
}