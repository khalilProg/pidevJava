package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.scene.control.TextField;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.tools.ThemeManager;

public class AdminUsersController implements Initializable {

    @FXML
    private VBox usersContainer;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button sortBtn;

    private List<User> allUsers = new ArrayList<>();
    private boolean sortAscending = true;
    private final ThemeManager themeManager = ThemeManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUsersFromDb();
        
        // Add listener for real-time search filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplay();
        });
    }

    private void loadUsersFromDb() {
        try {
            UserService userService = new UserService();
            allUsers = userService.recuperer();
            filterAndDisplay();
        } catch (Exception e) {
            System.err.println("Failed to fetch users: " + e.getMessage());
        }
    }

    private void filterAndDisplay() {
        usersContainer.getChildren().clear();
        String query = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";

        List<User> filteredList = allUsers.stream()
                .filter(u -> matchesSearch(u, query))
                .collect(Collectors.toList());

        // Sort the filtered list
        if (sortAscending) {
            filteredList.sort(Comparator.comparingInt(User::getId));
        } else {
            filteredList.sort((u1, u2) -> Integer.compare(u2.getId(), u1.getId()));
        }

        // Render rows
        for (User u : filteredList) {
            usersContainer.getChildren().add(createRow(u));
        }
    }

    private boolean matchesSearch(User u, String query) {
        if (query.isEmpty()) return true;
        
        String nom = u.getNom() != null ? u.getNom().toLowerCase() : "";
        String prenom = u.getPrenom() != null ? u.getPrenom().toLowerCase() : "";
        String email = u.getEmail() != null ? u.getEmail().toLowerCase() : "";
        String idStr = String.valueOf(u.getId());
        
        return nom.contains(query) || prenom.contains(query) || email.contains(query) || idStr.contains(query);
    }

    @FXML
    void handleSort(ActionEvent event) {
        sortAscending = !sortAscending;
        if (sortAscending) {
            sortBtn.setText("ID ↑");
        } else {
            sortBtn.setText("ID ↓");
        }
        filterAndDisplay();
    }

    private HBox createRow(User u) {
        HBox row = new HBox(20);
        row.getStyleClass().add("admin-user-row");

        // Info: Identifier
        HBox userInfoBox = new HBox(15);
        userInfoBox.setPrefWidth(350.0);
        userInfoBox.setStyle("-fx-alignment: center-left;");
        
        Label initials = new Label(u.getPrenom() != null && !u.getPrenom().isEmpty() ? u.getPrenom().substring(0,1).toUpperCase() : "?");
        initials.getStyleClass().add("admin-user-initials");
        
        VBox namesBox = new VBox(2);
        Label nameLbl = new Label(u.getPrenom() + " " + u.getNom());
        nameLbl.getStyleClass().add("admin-user-name");
        Label idLbl = new Label("ID " + u.getId());
        idLbl.getStyleClass().add("admin-user-id");
        namesBox.getChildren().addAll(nameLbl, idLbl);
        
        userInfoBox.getChildren().addAll(initials, namesBox);

        // Email
        Label emailLbl = new Label(u.getEmail());
        emailLbl.getStyleClass().add("admin-user-email");
        emailLbl.setPrefWidth(250.0);

        // Role Badge
        Label roleLbl = new Label(u.getRole() != null ? u.getRole().toUpperCase() : "UNKNOWN");
        if ("admin".equalsIgnoreCase(u.getRole())) {
            roleLbl.getStyleClass().add("role-badge-admin");
        } else {
            roleLbl.getStyleClass().add("role-badge-secondary");
        }
        HBox roleBox = new HBox(roleLbl);
        roleBox.setPrefWidth(150.0);
        roleBox.setStyle("-fx-alignment: center-left;");

        // Action Buttons
        HBox actionsBox = new HBox(10);
        actionsBox.setStyle("-fx-alignment: center-left;");
        Button btnVoir = new Button("👁 VOIR");
        Button btnModif = new Button("✎ MODIFIER");
        Button btnSuppr = new Button("🗑 SUPPRIMER");
        
        btnVoir.getStyleClass().add("action-btn-edit");
        btnModif.getStyleClass().add("action-btn-edit");
        btnSuppr.getStyleClass().add("action-btn-delete");
        tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnVoir);
        tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnModif);
        tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnSuppr);
        
        btnVoir.setOnAction(e -> handleViewUser(e, u));
        btnModif.setOnAction(e -> handleModifyUser(e, u));
        btnSuppr.setOnAction(e -> handleDeleteUser(u));

        actionsBox.getChildren().addAll(btnVoir, btnModif, btnSuppr);

        row.getChildren().addAll(userInfoBox, emailLbl, roleBox, actionsBox);
        return row;
    }

    private void handleViewUser(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_users_view.fxml"));
            Parent root = loader.load();
            AdminUsersViewController controller = loader.getController();
            controller.initData(user);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to navigate to user profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleModifyUser(ActionEvent event, User user) {
        try {
            if ("client".equalsIgnoreCase(user.getRole())) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_users_edit_client.fxml"));
                Parent root = loader.load();
                AdminUsersEditClientController controller = loader.getController();
                controller.initData(user, null);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
                stage.show();
            } else if ("agent banque".equalsIgnoreCase(user.getRole())) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_users_edit_banque.fxml"));
                Parent root = loader.load();
                AdminUsersEditBanqueController controller = loader.getController();
                controller.initData(user, null);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
                stage.show();
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_users_edit.fxml"));
                Parent root = loader.load();
                AdminUsersEditController controller = loader.getController();
                controller.initData(user);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
                stage.show();
            }
        } catch (IOException e) {
            System.err.println("Failed to navigate to user edit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteUser(User user) {
        UserService userService = new UserService();
        userService.supprimer(user);
        allUsers.remove(user);
        filterAndDisplay();
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
    void handleNavigateUsers(ActionEvent event) {
        // Already here
    }

    @FXML
    void handleNavigateDemandes(ActionEvent event) {
        navigateTo(event, "/DemandeBackView.fxml");
    }

    @FXML
    void handleNavigateTransferts(ActionEvent event) {
        navigateTo(event, "/TransfertBackView.fxml");
    }

    @FXML
    void handleNavigateQuestionnaires(ActionEvent event) {
        navigateTo(event, "/ListeQuestAdmin.fxml");
    }

    @FXML
    void handleNavigateRendezVous(ActionEvent event) {
        navigateTo(event, "/ListeRdvAdmin.fxml");
    }

    @FXML
    void handleNavigateCampagnes(ActionEvent event) {
        navigateTo(event, "/ListeCampagnesAdmin.fxml");
    }

    @FXML
    void handleNavigateCollectes(ActionEvent event) {
        navigateTo(event, "/ListeEntitesAdmin.fxml");
    }


    @FXML
    void handleNavigateAddUser(ActionEvent event) {
        navigateTo(event, "/admin_users_add.fxml");
    }

    private void navigateTo(ActionEvent event, String path) {
        try {
            AdminSidebarController.setCurrentPath(path);
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            themeManager.setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
