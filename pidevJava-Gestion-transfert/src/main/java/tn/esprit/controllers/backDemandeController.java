package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
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

                // ================= VALIDATE =================
                btnValidate.setOnAction(e -> {
                    Demande d = getTableView().getItems().get(getIndex());

                    try {
                        // popup dates
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

                                    // 👉 dates choisies dans popup
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

                    // simple exemple (tu peux ouvrir popup)
                    System.out.println("Edit: " + d.getId());
                });
            }

            private final HBox pane = new HBox(8, btnValidate, btnReject, btnEdit, btnDelete);

            {
                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Demande d = getTableView().getItems().get(getIndex());

                if ("EN_ATTENTE".equals(d.getStatus())) {
                    setGraphic(pane);
                } else {
                    // 🔥 état confirmé/refusé
                    // tu peux choisir ce que tu affiches
                    setGraphic(btnDelete); // ou null si tu veux clean UI
                }
            }
        });
    }
}
