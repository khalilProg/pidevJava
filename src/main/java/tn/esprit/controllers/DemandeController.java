package tn.esprit.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

import tn.esprit.entities.Commande;
import tn.esprit.entities.Demande;
import tn.esprit.services.CommandeService;
import tn.esprit.services.DemandeService;

import java.sql.SQLException;
import java.time.LocalDateTime;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
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
    @FXML private Button btnTransfert;
    @FXML private TextField txtBanque;
    @FXML private TextField txtType;
    @FXML private TextField txtQuantite;
    @FXML private ComboBox<String> comboUrgence;
    @FXML private TableView<Commande> tableCommande;

    @FXML private TableColumn<Commande, String> colCmdId;
    @FXML private TableColumn<Commande, String> colCmdClient;
    @FXML private TableColumn<Commande, String> colCmdType;
    @FXML private TableColumn<Commande, String> colCmdQte;
    @FXML private TableColumn<Commande, String> colCmdStatus;
    @FXML private TableColumn<Commande, Void> colCmdActions;

    private final CommandeService commandeService = new CommandeService();
    private ObservableList<Commande> commandes;
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
        initCommandeTable();
        loadCommandes();
    }

    private void loadCommandes() {
        try {
            commandes = FXCollections.observableArrayList(commandeService.recuperer());
            tableCommande.setItems(commandes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initCommandeTable() {
        colCmdId.setCellValueFactory(c ->
            new SimpleStringProperty("#" + c.getValue().getId())
        );

        colCmdClient.setCellValueFactory(c ->
            new SimpleStringProperty("Client #" + c.getValue().getClientId())
        );

        colCmdType.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getTypeSang())
        );

        colCmdQte.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getQuantite() + " unités")
        );

        colCmdStatus.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getStatus())
        );

        colCmdActions.setCellFactory(col -> new TableCell<>() {

            private final Button btnValider = new Button("✔");
            private final Button btnRefuser = new Button("✖");

            {
                btnValider.setOnAction(e -> updateStatus("VALIDEE"));
                btnRefuser.setOnAction(e -> updateStatus("REFUSEE"));
            }

            private void updateStatus(String status) {
                Commande cmd = getTableView().getItems().get(getIndex());

                try {
                    cmd.setStatus(status);
                    commandeService.modifier(cmd);
                    tableCommande.refresh();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Commande cmd = getTableRow().getItem();

                HBox box = new HBox(10);
                box.setAlignment(Pos.CENTER);

                if (cmd.getStatus() == null ||
                    cmd.getStatus().equalsIgnoreCase("EN_ATTENTE")) {
                    box.getChildren().addAll(btnValider, btnRefuser);
                }

                setGraphic(box);
            }
        });
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
                btnEdit.getStyleClass().add("action-btn-edit");
                btnDelete.getStyleClass().add("action-btn-delete");
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnEdit);
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(btnDelete);

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
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
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
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
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


    @FXML
    private void goToTransfert() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TransfertView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) tableDemande.getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void goToDemande(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DemandeBackView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void exportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(tableDemande.getScene().getWindow());

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                document.add(new Paragraph("Liste des Demandes"));
                document.add(new Paragraph(" "));

                PdfPTable pdfTable = new PdfPTable(6);
                pdfTable.setWidthPercentage(100);

                pdfTable.addCell("ID");
                pdfTable.addCell("Banque");
                pdfTable.addCell("Type");
                pdfTable.addCell("Quantité");
                pdfTable.addCell("Urgence");
                pdfTable.addCell("Status");

                for (Demande d : list) {
                    pdfTable.addCell(String.valueOf(d.getId()));
                    pdfTable.addCell(String.valueOf(d.getIdBanque()));
                    pdfTable.addCell(d.getTypeSang() != null ? d.getTypeSang() : "");
                    pdfTable.addCell(String.valueOf(d.getQuantite()));
                    pdfTable.addCell(d.getUrgence() != null ? d.getUrgence() : "");
                    pdfTable.addCell(d.getStatus() != null ? d.getStatus() : "");
                }

                document.add(pdfTable);
                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Exportation PDF réussie !");
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de l'exportation PDF : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
