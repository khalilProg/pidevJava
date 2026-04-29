package tn.esprit.controllers;
import javafx.event.ActionEvent;


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

    @FXML
    public void initialize() {

        colId.setCellValueFactory(param -> 
            new SimpleStringProperty("#" + param.getValue().getId())
        );
        colId.setStyle("-fx-font-weight: bold; -fx-alignment: center;");

        colDemande.setCellValueFactory(param -> {
            Transfert t = param.getValue();
            if (t.getDemande() != null) {
                return new SimpleStringProperty("DEM-" + t.getDemande().getId());
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
        colDemande.setStyle("-fx-font-weight: bold; -fx-alignment: center-left; -fx-text-fill: white;");

        colFrom.setCellFactory(column -> createMultilineCell("fromOrg"));
        colTo.setCellFactory(column -> createMultilineCell("toOrg"));

        colQuantite.setCellValueFactory(param -> 
            new SimpleStringProperty(param.getValue().getQuantite() + " UNITÉS")
        );
        colQuantite.setStyle("-fx-font-weight: bold; -fx-alignment: center-left; -fx-text-fill: white;");

        colDateEnvoie.setCellValueFactory(param -> {
            LocalDate date = param.getValue().getDateEnvoie();
            if (date == null) return new SimpleStringProperty("--/--/----");
            return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });

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
                        
                        String statusStr = (t.getStatus() != null) ? t.getStatus().toUpperCase() : "EN_ATTENTE";
                        
                        if (statusStr.equals("EN_COURS") || statusStr.equals("EN COURS")) {
                            badge.setText("🚚  EN COURS");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(241, 196, 15, 0.2); -fx-text-fill: #f1c40f; -fx-border-color: #f1c40f; -fx-border-radius: 12;");
                        } else if (statusStr.equals("RECU") || statusStr.equals("REÇU")) {
                            badge.setText("✔  REÇU");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(46, 204, 113, 0.2); -fx-text-fill: #2ecc71; -fx-border-color: #2ecc71; -fx-border-radius: 12;");
                        } else if (statusStr.equals("EN_ATTENTE") || statusStr.equals("EN ATTENTE")) {
                            badge.setText("⏳  EN ATTENTE");
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(52, 152, 219, 0.2); -fx-text-fill: #3498db; -fx-border-color: #3498db; -fx-border-radius: 12;");
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

    private void loadData() {
        try {
            list = service.recuperer();
            tableTransfert.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addActions() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button("🗑");
            private final Button btnAccept = new Button("🗸");
            private final Button btnReject = new Button("✖");

            {
                btnDelete.getStyleClass().add("action-btn-delete");
                btnAccept.getStyleClass().add("action-btn-edit");
                btnReject.getStyleClass().add("action-btn-delete");
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnDelete);
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnAccept);
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnReject);

                btnDelete.setOnAction(e -> {
                    Transfert t = getTableView().getItems().get(getIndex());
                    try {
                        service.supprimer(t);
                        loadData();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                btnAccept.setOnAction(e -> {
                    Transfert t = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(t, "RECU");
                });

                btnReject.setOnAction(e -> {
                    Transfert t = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(t, "REFUSE");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                
                Transfert t = getTableRow().getItem();
                HBox actions = new HBox(8);
                actions.setAlignment(Pos.CENTER_LEFT);

                // Only show Accept/Reject if status is EN_COURS
                String status = (t.getStatus() != null) ? t.getStatus().toUpperCase() : "";
                if (status.equals("EN_COURS") || status.equals("EN COURS")) {
                    actions.getChildren().addAll(btnAccept, btnReject);
                }
                
                actions.getChildren().add(btnDelete);
                setGraphic(actions);
            }
        });
    }

    private void handleUpdateStatus(Transfert t, String newStatus) {
        try {
            t.setStatus(newStatus);
            service.modifier(t);
            tableTransfert.refresh();
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
    void handleNavigateDemandes(javafx.event.ActionEvent event) {
        navigateTo(event, "/DemandeBackView.fxml");
    }

    @FXML
    void handleNavigateTransferts(javafx.event.ActionEvent event) {
        // Already here
    }

    @FXML
    void handleNavigateQuestionnaires(javafx.event.ActionEvent event) {
        navigateTo(event, "/ListeQuestAdmin.fxml");
    }

    @FXML
    void handleNavigateRendezVous(javafx.event.ActionEvent event) {
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
    void handleLogout(javafx.event.ActionEvent event) {
        navigateTo(event, "/login.fxml");
    }

    private void navigateTo(javafx.event.ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
