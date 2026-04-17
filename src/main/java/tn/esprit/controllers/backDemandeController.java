package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import tn.esprit.entities.Demande;
import tn.esprit.services.DemandeService;
import tn.esprit.services.TransfertService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class backDemandeController {

    @FXML private TableView<Demande> tableDemande;
    @FXML private TableColumn<Demande, String> colId;
    @FXML private TableColumn<Demande, String> colBanque;
    @FXML private TableColumn<Demande, String> colType;
    @FXML private TableColumn<Demande, String> colQuantite;
    @FXML private TableColumn<Demande, String> colUrgence;
    @FXML private TableColumn<Demande, String> colStatus;
    @FXML private TableColumn<Demande, String> colDate;
    @FXML private TableColumn<Demande, Void> colActions;

    private DemandeService demandeService = new DemandeService();
    private TransfertService transfertService = new TransfertService();

    private List<Demande> list;

    @FXML
    public void initialize() {

        // ID Formatting ("#12")
        colId.setCellValueFactory(param -> 
            new SimpleStringProperty("#" + param.getValue().getId())
        );
        colId.setStyle("-fx-font-weight: bold; -fx-alignment: center;");

        // Banque Name and Icon
        colBanque.setCellFactory(column -> {
            return new TableCell<Demande, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        Demande d = getTableRow().getItem();
                        HBox box = new HBox(8);
                        box.setAlignment(Pos.CENTER_LEFT);
                        
                        Label icon = new Label("🏛");
                        icon.setStyle("-fx-text-fill: -muted; -fx-font-size: 16px;");
                        
                        Label nameL = new Label();
                        nameL.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                        
                        if (d.getIdBanque() == 1 || d.getIdBanque() == 0) {
                            nameL.setText("Hopital ariena");
                        } else {
                            nameL.setText("Mahmoud El Matri Hospital");
                        }

                        box.getChildren().addAll(icon, nameL);
                        setGraphic(box);
                    }
                }
            };
        });

        // Type Sanguin Styling
        colType.setCellValueFactory(param -> 
            new SimpleStringProperty(param.getValue().getTypeSang())
        );
        colType.setStyle("-fx-text-fill: #e63939; -fx-font-weight: 900; -fx-alignment: center-left;");

        // Quantite format ("50 UNITÉS")
        colQuantite.setCellValueFactory(param -> 
            new SimpleStringProperty(param.getValue().getQuantite() + " UNITÉS")
        );
        colQuantite.setStyle("-fx-font-weight: bold; -fx-alignment: center-left; -fx-text-fill: white;");

        // Urgence Badges
        colUrgence.setCellFactory(column -> {
            return new TableCell<Demande, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Demande d = getTableRow().getItem();
                        Label badge = new Label();
                        badge.setStyle("-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                        
                        String u = (d.getUrgence() != null) ? d.getUrgence().toUpperCase() : "NORMALE";
                        
                        if (u.equals("URGENTE")) {
                            badge.setText("⚠  URGENTE");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(231, 76, 60, 0.2); -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-radius: 12;");
                        } else {
                            badge.setText("NORMALE");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(0, 190, 214, 0.2); -fx-text-fill: #00bed6; -fx-border-color: #00bed6; -fx-border-radius: 12;");
                        }

                        setGraphic(badge);
                        setAlignment(Pos.CENTER_LEFT);
                    }
                }
            };
        });

        // Status Badges
        colStatus.setCellFactory(column -> {
            return new TableCell<Demande, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Demande d = getTableRow().getItem();
                        Label badge = new Label();
                        badge.setStyle("-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                        
                        String statusStr = (d.getStatus() != null) ? d.getStatus().toUpperCase() : "EN_ATTENTE";
                        
                        if (statusStr.equals("VALIDEE") || statusStr.equals("CONFIRME")) {
                            badge.setText("✔  VALIDEE");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(46, 204, 113, 0.2); -fx-text-fill: #2ecc71; -fx-border-color: #2ecc71; -fx-border-radius: 12;");
                        } else if (statusStr.equals("REFUSEE") || statusStr.equals("REFUSE")) {
                            badge.setText("✖  REFUSEE");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(231, 76, 60, 0.2); -fx-text-fill: #e74c3c; -fx-border-color: #e74c3c; -fx-border-radius: 12;");
                        } else {
                            badge.setText("EN_ATTENTE");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(241, 196, 15, 0.2); -fx-text-fill: #f1c40f; -fx-border-color: #f1c40f; -fx-border-radius: 12;");
                        }

                        setGraphic(badge);
                        setAlignment(Pos.CENTER_LEFT);
                    }
                }
            };
        });

        // Date String Formatting
        colDate.setCellValueFactory(param -> {
            if (param.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(param.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new SimpleStringProperty("--/--/----");
        });

        loadData();
        addActions();
    }

    private void loadData() {
        try {
            list = demandeService.recuperer();
            tableDemande.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addActions() {
        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");

            {
                // Round transparent icon buttons mimicking Figma mockup
                btnEdit.setStyle("-fx-background-color: transparent; -fx-border-color: #333; -fx-border-radius: 20; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 12;");
                btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-border-color: white; -fx-border-radius: 20; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 12;"));
                btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-background-color: transparent; -fx-border-color: #333; -fx-border-radius: 20; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 4 12;"));

                btnDelete.setStyle("-fx-background-color: transparent; -fx-border-color: #333; -fx-border-radius: 20; -fx-text-fill: #e63939; -fx-cursor: hand; -fx-padding: 4 12;");
                btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: rgba(230, 57, 57, 0.1); -fx-border-color: #e63939; -fx-border-radius: 20; -fx-text-fill: #e63939; -fx-cursor: hand; -fx-padding: 4 12;"));
                btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: transparent; -fx-border-color: #333; -fx-border-radius: 20; -fx-text-fill: #e63939; -fx-cursor: hand; -fx-padding: 4 12;"));

                btnDelete.setOnAction(e -> {
                    Demande d = getTableView().getItems().get(getIndex());
                    try {
                        demandeService.supprimer(d);
                        tableDemande.getItems().remove(d);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                btnEdit.setOnAction(e -> {
                    Demande d = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/editBackDemande.fxml"));
                        Parent root = loader.load();

                        EditDemandeController controller = loader.getController();
                        controller.setDemande(d);

                        Stage stage = (Stage) tableDemande.getScene().getWindow();
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
                HBox actions = new HBox(8);
                actions.setAlignment(Pos.CENTER_LEFT);
                actions.getChildren().addAll(btnEdit, btnDelete);
                setGraphic(actions);
            }
        });
    }

    @FXML
    private void goToTransfert() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TransfertBackView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tableDemande.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAddDemande() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addBackDemande.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tableDemande.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    void handleLogout(javafx.event.ActionEvent event) {
        navigateTo(event, "/login.fxml");
    }

    private void navigateTo(javafx.event.ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
