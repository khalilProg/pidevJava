package tn.esprit.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import tn.esprit.entities.Transfert;
import tn.esprit.services.TransfertService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class backTransfertController {

    @FXML private TableView<Transfert> tableTransfert;

    @FXML private TableColumn<Transfert, String> colId;
    @FXML private TableColumn<Transfert, String> colDemande;
    @FXML private TableColumn<Transfert, String> colFrom;
    @FXML private TableColumn<Transfert, String> colTo;
    @FXML private TableColumn<Transfert, String> colQuantite;
    @FXML private TableColumn<Transfert, String> colDateEnvoie;
    @FXML private TableColumn<Transfert, String> colStatus;
    @FXML private TableColumn<Transfert, Void> colActions;

    private final TransfertService service = new TransfertService();
    private List<Transfert> list;

    // ================= INIT =================
    @FXML
    public void initialize() {

        // ID Formatting ("#6")
        colId.setCellValueFactory(param -> 
            new SimpleStringProperty("#" + param.getValue().getId())
        );
        colId.setStyle("-fx-font-weight: bold; -fx-alignment: center;");

        // Demande Formatting ("DEM-12")
        colDemande.setCellValueFactory(param -> {
            Transfert t = param.getValue();
            if (t.getDemande() != null) {
                return new SimpleStringProperty("DEM-" + t.getDemande().getId());
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
        colDemande.setStyle("-fx-font-weight: bold; -fx-alignment: center-left; -fx-text-fill: white;");

        // From Org formatting (Multiline)
        colFrom.setCellFactory(column -> createMultilineCell("fromOrg"));

        // To Org formatting (Multiline)
        colTo.setCellFactory(column -> createMultilineCell("toOrg"));

        // Quantite format ("50 UNITÉS")
        colQuantite.setCellValueFactory(param -> 
            new SimpleStringProperty(param.getValue().getQuantite() + " UNITÉS")
        );
        colQuantite.setStyle("-fx-font-weight: bold; -fx-alignment: center-left; -fx-text-fill: white;");

        // Date Envoie
        colDateEnvoie.setCellValueFactory(param -> {
            LocalDate date = param.getValue().getDateEnvoie();
            if (date == null) return new SimpleStringProperty("--/--/----");
            return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });

        // Status Badges
        colStatus.setCellFactory(column -> {
            return new TableCell<Transfert, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Transfert t = getTableRow().getItem();
                        Label badge = new Label();
                        badge.setStyle("-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                        
                        String statusStr = (t.getStatus() != null) ? t.getStatus().toUpperCase() : "INCONNU";
                        
                        if (statusStr.equals("EN_COURS") || statusStr.equals("EN COURS")) {
                            badge.setText("🚚  EN COURS");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(241, 196, 15, 0.2); -fx-text-fill: #f1c40f; -fx-border-color: #f1c40f; -fx-border-radius: 12;");
                        } else if (statusStr.equals("RECU") || statusStr.equals("REÇU")) {
                            badge.setText("✔  REÇU");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(46, 204, 113, 0.2); -fx-text-fill: #2ecc71; -fx-border-color: #2ecc71; -fx-border-radius: 12;");
                        } else {
                            badge.setText("✖  " + statusStr);
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(231, 76, 60, 0.2); -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-radius: 12;");
                        }

                        setGraphic(badge);
                        setAlignment(Pos.CENTER_LEFT);
                    }
                }
            };
        });

        loadData();
        addActions();
    }

    private TableCell<Transfert, String> createMultilineCell(String field) {
        return new TableCell<Transfert, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Transfert t = getTableRow().getItem();
                    VBox box = new VBox(2);
                    box.setAlignment(Pos.CENTER_LEFT);
                    
                    Label nameLbl = new Label();
                    nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    
                    Label idLbl = new Label();
                    idLbl.setStyle("-fx-text-fill: -muted; -fx-font-size: 10px;");
                    
                    if (field.equals("fromOrg")) {
                        nameLbl.setText(t.getFromOrg() != null ? t.getFromOrg().toUpperCase() : "INCONNU");
                        idLbl.setText("ID: " + t.getFromOrgId());
                    } else {
                        nameLbl.setText(t.getToOrg() != null ? t.getToOrg().toUpperCase() : "INCONNU");
                        idLbl.setText("ID: " + t.getToOrgId());
                    }
                    
                    box.getChildren().addAll(nameLbl, idLbl);
                    setGraphic(box);
                }
            }
        };
    }

    // ================= LOAD =================
    private void loadData() {
        try {
            list = service.recuperer();
            tableTransfert.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ACTIONS =================
    private void addActions() {
        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnEdit = new Button("✏ MODIFIER");
            private final Button btnDelete = new Button("🗑 SUPPRIMER");

            {
                // 🎨 STYLES BASED ON MOCKUP
                btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
                btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px; -fx-background-radius: 15;"));
                btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;"));

                btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #e63939; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;");
                btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: rgba(230, 57, 57, 0.1); -fx-text-fill: #e63939; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px; -fx-background-radius: 15;"));
                btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #e63939; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px;"));

                // ================= DELETE =================
                btnDelete.setOnAction(e -> {
                    Transfert t = getTableView().getItems().get(getIndex());
                    try {
                        service.supprimer(t);
                        tableTransfert.getItems().remove(t);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                // ================= EDIT =================
                btnEdit.setOnAction(e -> {
                    Transfert t = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/editBackTransfert.fxml"));
                        Parent root = loader.load();

                        // 🔥 envoyer transfert au controller edit
                        EditTransfertController controller = loader.getController();
                        controller.setTransfert(t);

                        Stage stage = (Stage) tableTransfert.getScene().getWindow();
                        stage.setScene(new Scene(root));

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                HBox actions = new HBox(15);
                actions.setAlignment(Pos.CENTER_LEFT);
                actions.getChildren().addAll(btnEdit, btnDelete);
                setGraphic(actions);
            }
        });
    }

    @FXML
    void handleNavigateDashboard(javafx.event.ActionEvent event) {
        navigateTo(event, "/admin_dashboard.fxml");
    }

    @FXML
    void handleNavigateUsers(javafx.event.ActionEvent event) {
        navigateTo(event, "/admin_users.fxml");
    }

    @FXML
    void handleNavigateDemandes(javafx.event.ActionEvent event) {
        navigateTo(event, "/DemandeBackView.fxml");
    }

    @FXML
    void handleLogout(javafx.event.ActionEvent event) {
        navigateTo(event, "/login.fxml");
    }
    
    @FXML
    private void retour() {
        // Safe navigation logic depending on flow
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableTransfert.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(javafx.event.ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
