package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import tn.esprit.entities.Transfert;
import tn.esprit.services.TransfertService;

import java.time.LocalDate;
import java.util.List;

public class backTransfertController {

    @FXML private TableView<Transfert> tableTransfert;

    @FXML private TableColumn<Transfert, Integer> colId;
    @FXML private TableColumn<Transfert, String> colFrom;
    @FXML private TableColumn<Transfert, String> colTo;
    @FXML private TableColumn<Transfert, Integer> colQuantite;
    @FXML private TableColumn<Transfert, LocalDate> colDateEnvoie;
    @FXML private TableColumn<Transfert, LocalDate> colDateReception;
    @FXML private TableColumn<Transfert, String> colStatus;
    @FXML private TableColumn<Transfert, Void> colActions;

    private final TransfertService service = new TransfertService();
    private List<Transfert> list;

    // ================= INIT =================
    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFrom.setCellValueFactory(new PropertyValueFactory<>("fromOrg"));
        colTo.setCellValueFactory(new PropertyValueFactory<>("toOrg"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colDateEnvoie.setCellValueFactory(new PropertyValueFactory<>("dateEnvoie"));
        colDateReception.setCellValueFactory(new PropertyValueFactory<>("dateReception"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadData();
        addActions();
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

            private final Button btnConfirm = new Button("✔");
            private final Button btnCancel = new Button("✖");
            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");

            {
                // 🎨 STYLE
                btnConfirm.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                btnCancel.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                btnEdit.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: black;");
                btnDelete.setStyle("-fx-background-color: black; -fx-text-fill: white;");

                // ================= CONFIRM =================
                btnConfirm.setOnAction(e -> {
                    Transfert t = getTableView().getItems().get(getIndex());

                    try {
                        t.setStatus("RECU");
                        t.setDateReception(LocalDate.now());

                        service.modifier(t);
                        tableTransfert.refresh();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                // ================= CANCEL =================
                btnCancel.setOnAction(e -> {
                    Transfert t = getTableView().getItems().get(getIndex());

                    try {
                        t.setStatus("ANNULE");
                        service.modifier(t);
                        tableTransfert.refresh();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

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

                Transfert t = getTableView().getItems().get(getIndex());

                HBox actions = new HBox(8);
                actions.setAlignment(Pos.CENTER);

                // 🔥 toujours visibles
                actions.getChildren().addAll(btnEdit, btnDelete);

                // 🔥 seulement si EN_COURS
                if ("EN_COURS".equals(t.getStatus())) {
                    actions.getChildren().add(0, btnConfirm);
                    actions.getChildren().add(1, btnCancel);
                }

                setGraphic(actions);
            }
        });
    }

    // ================= NAVIGATION =================
    @FXML
    private void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DemandeBackView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tableTransfert.getScene().getWindow();
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
    void handleNavigateDemandes(javafx.event.ActionEvent event) {
        navigateTo(event, "/DemandeBackView.fxml");
    }

    @FXML
    void handleLogout(javafx.event.ActionEvent event) {
        navigateTo(event, "/login.fxml");
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
