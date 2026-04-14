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
import javafx.stage.Modality;
import javafx.stage.Stage;

import tn.esprit.entities.Demande;
import tn.esprit.services.DemandeService;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class DemandeController {

    @FXML private TableView<Demande> tableDemande;
    @FXML private TableColumn<Demande, Integer> colId;
    @FXML private TableColumn<Demande, Integer> colBanque;
    @FXML private TableColumn<Demande, String> colType;
    @FXML private TableColumn<Demande, Integer> colQuantite;
    @FXML private TableColumn<Demande, String> colUrgence;
    @FXML private TableColumn<Demande, String> colStatus;
    @FXML private TableColumn<Demande, Void> colActions;
    @FXML private Button btnAdd;
    @FXML private TextField txtBanque;
    @FXML private TextField txtType;
    @FXML private TextField txtQuantite;
    @FXML private ComboBox<String> comboUrgence;
    private DemandeService service = new DemandeService();

    private ObservableList<Demande> list = FXCollections.observableArrayList();

    private int idCounter = 1;

    @FXML

    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBanque.setCellValueFactory(new PropertyValueFactory<>("idBanque"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeSang"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colUrgence.setCellValueFactory(new PropertyValueFactory<>("urgence"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        btnAdd.setOnAction(e -> openForm());

        addActionButtons();

        // 🔥 charger données depuis DB
        loadData();

        tableDemande.setItems(list);
    }

    public void loadData() {
        try {
            list.clear();
            list.addAll(service.recuperer());
            tableDemande.setItems(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Ajouter une demande depuis formulaire
    public void addDemandeToTable(Demande d) {
        d.setId(idCounter++);
        d.setCreatedAt(LocalDateTime.now());
        list.add(d);
    }

    // ✅ boutons edit + delete
    private void addActionButtons() {

        colActions.setCellFactory(param -> new TableCell<>() {

            private final Button btnEdit = new Button("✏");
            private final Button btnDelete = new Button("🗑");

            {
                btnEdit.setStyle("-fx-background-color: #e63939; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: black; -fx-text-fill: white;");

                btnEdit.setOnAction(e -> {

                    Demande d = getTableView().getItems().get(getIndex());

                    openEditForm(d);
                });

                btnDelete.setOnAction(e -> {

                    Demande d = getTableView().getItems().get(getIndex());

                    try {
                        service.supprimer(d);
                        list.remove(d);

                        System.out.println("Supprimé avec succès");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            private final HBox pane = new HBox(10, btnEdit, btnDelete);

            {
                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    // ✅ ouvrir formulaire et récupérer données

    private void openForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addDemande.fxml"));
            Parent root = loader.load();

            addDemandeController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void openEditForm(Demande d) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addDemande.fxml"));
            Parent root = loader.load();

            addDemandeController controller = loader.getController();
            controller.setEditData(d); // 🔥 on envoie les données

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DemandeController mainController;

    public void setMainController(DemandeController controller) {
        this.mainController = controller;
    }


    private void showEditAlert(Demande d) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Demande ajoutée !");
        alert.show();
    }


}
