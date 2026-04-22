package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Banque;
import tn.esprit.entities.User;
import tn.esprit.entities.client;
import tn.esprit.services.BanqueService;
import tn.esprit.services.ClientService;

import java.io.IOException;
import java.util.List;

public class AdminUsersViewController {

    @FXML
    private Label subtitleLabel;
    @FXML
    private Label initialsLabel;
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private VBox roleDetailsContainer;

    private User currentUser;
    private client currentClient;
    private Banque currentBanque;

    public void initData(User user) {
        this.currentUser = user;
        if (user != null) {
            subtitleLabel.setText("Consultez les informations détaillées du compte #" + user.getId() + ".");

            String prenom = user.getPrenom() != null ? user.getPrenom() : "";
            String nom = user.getNom() != null ? user.getNom() : "";
            fullNameLabel.setText(prenom + " " + nom);

            String init = (!prenom.isEmpty() ? prenom.substring(0, 1).toUpperCase() : "?") +
                          (!nom.isEmpty() ? nom.substring(0, 1).toUpperCase() : "?");
            initialsLabel.setText(init);

            emailLabel.setText("✉ " + user.getEmail());

            roleLabel.setText(user.getRole() != null ? user.getRole().toUpperCase() : "UNKNOWN");
            if ("admin".equalsIgnoreCase(user.getRole())) {
                roleLabel.setStyle("-fx-background-color: -primary; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 10px;");
            } else {
                roleLabel.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 8; -fx-text-fill: white; -fx-padding: 2 8; -fx-font-weight: bold; -fx-font-size: 10px;");
            }

            // Load role-specific details
            loadRoleDetails(user);
        }
    }

    private void loadRoleDetails(User user) {
        roleDetailsContainer.getChildren().clear();

        if ("client".equalsIgnoreCase(user.getRole())) {
            loadClientDetails(user);
        } else if ("agent banque".equalsIgnoreCase(user.getRole())) {
            loadBanqueDetails(user);
        }
    }

    private void loadClientDetails(User user) {
        try {
            ClientService clientService = new ClientService();
            List<client> clients = clientService.recuperer();
            for (client c : clients) {
                if (c.getUser() != null && c.getUser().getId() == user.getId()) {
                    currentClient = c;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Section title
        Label sectionTitle = new Label("🩸 INFORMATIONS DONNEUR");
        sectionTitle.setStyle("-fx-text-fill: -primary; -fx-font-weight: 900; -fx-font-size: 14px;");
        VBox.setMargin(sectionTitle, new Insets(10, 0, 0, 0));

        // Info cards
        HBox cardsRow = new HBox(20);

        VBox bloodCard = createInfoCard("GROUPE SANGUIN",
                currentClient != null && currentClient.getTypeSang() != null ? currentClient.getTypeSang() : "N/A");
        VBox donCard = createInfoCard("DERNIER DON",
                currentClient != null && currentClient.getDernierDon() != null ? currentClient.getDernierDon().toString() : "N/A");

        HBox.setHgrow(bloodCard, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(donCard, javafx.scene.layout.Priority.ALWAYS);
        cardsRow.getChildren().addAll(bloodCard, donCard);

        roleDetailsContainer.getChildren().addAll(sectionTitle, cardsRow);
    }

    private void loadBanqueDetails(User user) {
        try {
            BanqueService banqueService = new BanqueService();
            List<Banque> banques = banqueService.recuperer();
            for (Banque b : banques) {
                if (b.getUser() != null && b.getUser().getId() == user.getId()) {
                    currentBanque = b;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Section title
        Label sectionTitle = new Label("DÉTAILS DE LA BANQUE");
        sectionTitle.setStyle("-fx-text-fill: -primary; -fx-font-weight: 900; -fx-font-size: 14px;");
        VBox.setMargin(sectionTitle, new Insets(10, 0, 0, 0));

        // Info cards
        HBox cardsRow = new HBox(20);

        VBox nameCard = createInfoCard("ÉTABLISSEMENT",
                currentBanque != null && currentBanque.getNom() != null ? currentBanque.getNom() : "N/A");
        
        String contactText = currentBanque != null && currentBanque.getTelephone() != null 
                ? "📞 " + currentBanque.getTelephone() : "N/A";
        VBox contactCard = createInfoCard("CONTACT", contactText);

        HBox.setHgrow(nameCard, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(contactCard, javafx.scene.layout.Priority.ALWAYS);
        cardsRow.getChildren().addAll(nameCard, contactCard);

        roleDetailsContainer.getChildren().addAll(sectionTitle, cardsRow);
    }

    private VBox createInfoCard(String title, String value) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-border-color: rgba(255,255,255,0.08); " +
                "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 20;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: -muted; -fx-font-size: 11px; -fx-font-weight: 700;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 800;");

        card.getChildren().addAll(titleLbl, valueLbl);
        return card;
    }

    @FXML
    void handleNavigateUsers(ActionEvent event) {
        navigateTo(event, "/admin_users.fxml");
    }

    @FXML
    void handleNavigateModify(ActionEvent event) {
        try {
            if ("client".equalsIgnoreCase(currentUser.getRole())) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_users_edit_client.fxml"));
                Parent root = loader.load();
                AdminUsersEditClientController controller = loader.getController();
                controller.initData(currentUser, currentClient);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
                stage.show();
            } else if ("agent banque".equalsIgnoreCase(currentUser.getRole())) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_users_edit_banque.fxml"));
                Parent root = loader.load();
                AdminUsersEditBanqueController controller = loader.getController();
                controller.initData(currentUser, currentBanque);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
                stage.show();
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_users_edit.fxml"));
                Parent root = loader.load();
                AdminUsersEditController controller = loader.getController();
                controller.initData(currentUser);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        navigateTo(event, "/login.fxml");
    }

    @FXML
    void handleNavigateDashboard(ActionEvent event) {
        navigateTo(event, "/admin_dashboard.fxml");
    }

    @FXML
    void handleNavigateDemandes(ActionEvent event) {
        navigateTo(event, "/DemandeBackView.fxml");
    }

    @FXML
    void handleNavigateTransferts(ActionEvent event) {
        navigateTo(event, "/TransfertBackView.fxml");
    }

    private void navigateTo(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(tn.esprit.tools.ThemeManager.getInstance().createScene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
