package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Demande;
import tn.esprit.entities.Transfert;
import tn.esprit.services.DemandeService;
import tn.esprit.services.TransfertService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class backDemandeController {

    @FXML private TableView<Demande> tableDemande;
    @FXML private TableColumn<Demande, Integer> colId;
    @FXML private TableColumn<Demande, String> colType;
    @FXML private TableColumn<Demande, Integer> colQuantite;
    @FXML private TableColumn<Demande, String> colStatus;
    @FXML private TableColumn<Demande, Void> colActions;

    private DemandeService demandeService = new DemandeService();
    private TransfertService transfertService = new TransfertService();

    private List<Demande> list;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeSang"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

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

            private final Button btnValidate = new Button("✔");
            private final Button btnReject = new Button("✖");
            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");

            {

                // ================= STYLE =================
                btnValidate.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                btnReject.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                btnEdit.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: black;");
                btnDelete.setStyle("-fx-background-color: black; -fx-text-fill: white;");

                btnValidate.setStyle(btnValidate.getStyle() + "; -fx-cursor: hand;");
                btnReject.setStyle(btnReject.getStyle() + "; -fx-cursor: hand;");
                btnEdit.setStyle(btnEdit.getStyle() + "; -fx-cursor: hand;");
                btnDelete.setStyle(btnDelete.getStyle() + "; -fx-cursor: hand;");

                // ================= VALIDATE =================
                btnValidate.setOnAction(e -> {

                    Demande d = getTableView().getItems().get(getIndex());

                    try {
                        Dialog<ButtonType> dialog = new Dialog<>();
                        dialog.setTitle("Planifier le transfert");

                        DatePicker dateEnvoie = new DatePicker();
                        DatePicker dateReception = new DatePicker();

                        VBox content = new VBox(10,
                            new Label("Date d'envoi"),
                            dateEnvoie,
                            new Label("Date de réception"),
                            dateReception
                        );

                        dialog.getDialogPane().setContent(content);
                        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                        dialog.getDialogPane().getStylesheets().add(
                            getClass().getResource("/style.css").toExternalForm()
                        );
                        dialog.getDialogPane().getStyleClass().add("planification-dialog");

                        dialog.showAndWait().ifPresent(response -> {

                            if (response == ButtonType.OK) {

                                try {
                                    d.setStatus("CONFIRME");
                                    demandeService.modifier(d);

                                    Transfert t = new Transfert();
                                    t.setDemande(d);

                                    // FIX OBLIGATOIRE
                                    t.setFromOrgId(1);
                                    t.setToOrgId(d.getIdBanque());

                                    t.setFromOrg("BloodLink Central");
                                    t.setToOrg("Banque " + d.getIdBanque());

                                    t.setStock(1);

                                    // dates popup
                                    t.setDateEnvoie(dateEnvoie.getValue());
                                    t.setDateReception(dateReception.getValue());

                                    t.setQuantite(d.getQuantite());
                                    t.setStatus("EN_COURS");
                                    t.setCreatedAt(LocalDateTime.now());
                                    t.setUpdatedAt(LocalDateTime.now());

                                    transfertService.ajouter(t);

                                    tableDemande.refresh();

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                // ================= REJECT =================
                btnReject.setOnAction(e -> {

                    Demande d = getTableView().getItems().get(getIndex());

                    try {
                        d.setStatus("REFUSE");
                        demandeService.modifier(d);
                        tableDemande.refresh();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                // ================= DELETE =================
                btnDelete.setOnAction(e -> {

                    Demande d = getTableView().getItems().get(getIndex());

                    try {
                        demandeService.supprimer(d);
                        tableDemande.getItems().remove(d);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                // ================= EDIT =================
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

                Demande d = getTableView().getItems().get(getIndex());

                HBox box = new HBox(8);
                box.setAlignment(Pos.CENTER);

                // EDIT toujours visible
                box.getChildren().add(btnEdit);

                // autres actions seulement EN_ATTENTE
                if ("EN_ATTENTE".equals(d.getStatus())) {
                    box.getChildren().addAll(btnValidate, btnReject, btnDelete);
                }

                setGraphic(box);
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
