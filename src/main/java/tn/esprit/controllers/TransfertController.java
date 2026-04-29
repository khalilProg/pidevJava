package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import tn.esprit.entities.Demande;
import tn.esprit.entities.Transfert;
import tn.esprit.services.TransfertService;

public class TransfertController {

    @FXML private TableView<Transfert> tableTransfert;

    @FXML private TableColumn<Transfert, Integer> colId;
    @FXML private TableColumn<Transfert, String> colDemande;
    @FXML private TableColumn<Transfert, String> colFrom;
    @FXML private TableColumn<Transfert, String> colTo;
    @FXML private TableColumn<Transfert, Integer> colQuantite;
    @FXML private TableColumn<Transfert, Integer> colStock;
    @FXML private TableColumn<Transfert, String> colDateEnvoie;
    @FXML private TableColumn<Transfert, String> colDateReception;
    @FXML private TableColumn<Transfert, String> colStatus;
    @FXML private TableColumn<Transfert, Void> colActions;
    private Demande demande;

    private TransfertService service = new TransfertService();
    private ObservableList<Transfert> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // ⚠️ demande (objet)
        colDemande.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getDemande() != null ?
                    String.valueOf(cell.getValue().getDemande().getId()) : "N/A"
            )
        );

        colFrom.setCellValueFactory(new PropertyValueFactory<>("fromOrg"));
        colTo.setCellValueFactory(new PropertyValueFactory<>("toOrg"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // dates → string
        colDateEnvoie.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getDateEnvoie() != null ?
                    cell.getValue().getDateEnvoie().toString() : "-"
            )
        );

        colDateReception.setCellValueFactory(cell ->
            new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getDateReception() != null ?
                    cell.getValue().getDateReception().toString() : "-"
            )
        );

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadData();
        addActions();

        tableTransfert.setItems(list);
    }

    // 🔥 LOAD DATA
    private void loadData() {
        try {
            list.setAll(service.recuperer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setDemande(Demande demande) {
        this.demande = demande;
        loadData(); // reload avec filtre
    }

    // 🔥 DELETE BUTTON
    private void addActions() {

        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnDelete = new Button("🗑");
            private final Button btnValidate = new Button("✔");
            private final Button btnReject = new Button("✖");

            {

                // ================= DELETE =================
                btnDelete.setOnAction(e -> {
                    Transfert t = getTableView().getItems().get(getIndex());

                    try {
                        service.supprimer(t);
                        list.remove(t);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                // ================= VALIDATE =================
                btnValidate.setOnAction(e -> {
                    Transfert t = getTableView().getItems().get(getIndex());

                    try {
                        t.setStatus("RECU");
                        service.modifier(t);
                        getTableView().refresh();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                // ================= REJECT =================
                btnReject.setOnAction(e -> {
                    Transfert t = getTableView().getItems().get(getIndex());

                    try {
                        t.setStatus("REFUSEE");
                        service.modifier(t);
                        getTableView().refresh();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                // styles
                btnDelete.getStyleClass().add("action-btn-delete");
                btnValidate.getStyleClass().add("action-btn-edit");
                btnReject.getStyleClass().add("action-btn-delete");
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnDelete);
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnValidate);
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnReject);
            }

            private final HBox pane = new HBox(8, btnValidate, btnReject, btnDelete);

            {
                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Transfert t = getTableView().getItems().get(getIndex());

                    // 🔥 CONDITION IMPORTANTE
                    if ("EN_COURS".equals(t.getStatus())) {
                        setGraphic(pane);
                    } else {
                        setGraphic(null); // ❌ pas de boutons
                    }
                }
            }
        });
    }

    // 🔥 OPEN FORM
    @FXML
    private void openForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addTransfert.fxml"));
            Parent root = loader.load();

            addTrasnfertController controller = loader.getController();

            // 🔥 passer la demande
            controller.setDemande(demande);

            Stage stage = new Stage();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔥 RETOUR
    @FXML
    private void retour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutDemande.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tableTransfert.getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
