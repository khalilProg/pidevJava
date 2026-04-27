package tn.esprit.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;
import tn.esprit.services.CampagneService;
import tn.esprit.services.EntiteCollecteService;
import tn.esprit.services.QuestionnaireService;
import tn.esprit.services.RendezVousService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListeRdvAdmin {

    @FXML private TableView<RendezVous> tableView;
    @FXML private TableColumn<RendezVous, Integer> idColumn;
    @FXML private TableColumn<RendezVous, RendezVous> donneurColumn;
    @FXML private TableColumn<RendezVous, RendezVous> dateLieuColumn;
    @FXML private TableColumn<RendezVous, RendezVous> campagneColumn;
    @FXML private TableColumn<RendezVous, String> statusColumn;
    @FXML private TableColumn<RendezVous, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker dateFilter;
    @FXML private Button exportBtn;
    @FXML private Button totalRdvBtn;

    private final RendezVousService rdvService = new RendezVousService();
    private final QuestionnaireService questionnaireService = new QuestionnaireService();
    private final CampagneService campagneService = new CampagneService();
    private final EntiteCollecteService entiteService = new EntiteCollecteService();
    private ObservableList<RendezVous> rendezVousData = FXCollections.observableArrayList();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
        loadData();
        setupFilters();
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : "#" + item);
                setStyle(empty ? "" : "-fx-font-weight: 800; -fx-text-fill: -admin-table-strong; -fx-font-size: 12px; -fx-alignment: center-left;");
            }
        });

        donneurColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        donneurColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(RendezVous rdv, boolean empty) {
                super.updateItem(rdv, empty);
                if (empty || rdv == null) {
                    setGraphic(null);
                    return;
                }
                try {
                    Questionnaire q = questionnaireService.getQuestionnaireById(rdv.getQuestionnaire_id());
                    HBox box = new HBox(12);
                    box.setAlignment(Pos.CENTER_LEFT);

                    String prenom = q.getPrenom() == null ? "" : q.getPrenom().trim();
                    String nom = q.getNom() == null ? "" : q.getNom().trim();
                    Label circle = new Label(initials(prenom, nom));
                    circle.getStyleClass().add("initials-circle");

                    Label nameLbl = new Label((nom.toUpperCase() + " " + prenom.toLowerCase()).trim());
                    nameLbl.getStyleClass().add("donneur-name");
                    box.getChildren().addAll(circle, nameLbl);
                    setGraphic(box);
                } catch (SQLException e) {
                    setGraphic(new Label("Inconnu"));
                }
            }
        });

        dateLieuColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        dateLieuColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(RendezVous rdv, boolean empty) {
                super.updateItem(rdv, empty);
                if (empty || rdv == null) {
                    setGraphic(null);
                    return;
                }
                try {
                    String entiteNom = entiteService.getEntiteById(rdv.getEntite_id()).getNom();
                    String dateStr = rdv.getDateDon() == null ? "" : FORMATTER.format(rdv.getDateDon());

                    VBox box = new VBox(2);
                    box.setAlignment(Pos.CENTER_LEFT);
                    Label dateLbl = new Label(dateStr);
                    dateLbl.getStyleClass().add("admin-table-strong");
                    Label lieuLbl = new Label(entiteNom);
                    lieuLbl.getStyleClass().add("admin-table-muted");
                    box.getChildren().addAll(dateLbl, lieuLbl);
                    setGraphic(box);
                } catch (SQLException e) {
                    setGraphic(new Label("Erreur"));
                }
            }
        });

        campagneColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        campagneColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(RendezVous rdv, boolean empty) {
                super.updateItem(rdv, empty);
                if (empty || rdv == null) {
                    setGraphic(null);
                    return;
                }
                try {
                    Questionnaire q = questionnaireService.getQuestionnaireById(rdv.getQuestionnaire_id());
                    Label badge = new Label(campagneService.getCampagneById(q.getCampagneId()).getTitre());
                    badge.getStyleClass().add("badge-campagne");
                    setGraphic(badge);
                } catch (SQLException e) {
                    setGraphic(new Label("Erreur"));
                }
            }
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge-status");
                    setGraphic(badge);
                }
            }
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("action-icon-btn");
                deleteBtn.getStyleClass().addAll("action-icon-btn", "action-icon-delete");
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(editBtn);
                tn.esprit.tools.AnimationUtils.applyHoverAnimation(deleteBtn);
                container.setAlignment(Pos.CENTER_LEFT);

                deleteBtn.setOnAction(e -> deleteRendezVous(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> openEditScreen(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    editBtn.setVisible(rdv.getDateDon() != null && rdv.getDateDon().isAfter(LocalDateTime.now()));
                    editBtn.setManaged(editBtn.isVisible());
                    setGraphic(container);
                }
            }
        });
    }

    private void loadData() {
        try {
            List<RendezVous> rendezVous = rdvService.recuperer();
            rendezVousData = FXCollections.observableArrayList(rendezVous);
            updateTotal();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupFilters() {
        if (statusFilter != null) {
            statusFilter.setItems(FXCollections.observableArrayList("", "confirme", "confirmé", "annule", "annulé"));
        }

        FilteredList<RendezVous> filteredData = new FilteredList<>(rendezVousData, p -> true);
        Runnable refresh = () -> {
            filteredData.setPredicate(this::matchesFilters);
            updateTotal(filteredData.size());
        };

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        }
        if (statusFilter != null) {
            statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        }
        if (dateFilter != null) {
            dateFilter.valueProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        }

        SortedList<RendezVous> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
        updateTotal(filteredData.size());
    }

    private boolean matchesFilters(RendezVous rdv) {
        String query = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().toLowerCase().trim();
        String status = statusFilter == null ? null : statusFilter.getValue();

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase(rdv.getStatus())) {
            return false;
        }
        if (dateFilter != null && dateFilter.getValue() != null
                && (rdv.getDateDon() == null || !dateFilter.getValue().equals(rdv.getDateDon().toLocalDate()))) {
            return false;
        }
        if (query.isEmpty()) {
            return true;
        }

        try {
            Questionnaire q = questionnaireService.getQuestionnaireById(rdv.getQuestionnaire_id());
            String campagne = campagneService.getCampagneById(q.getCampagneId()).getTitre();
            String entite = entiteService.getEntiteById(rdv.getEntite_id()).getNom();
            return contains(q.getNom(), query)
                    || contains(q.getPrenom(), query)
                    || contains(q.getGroupeSanguin(), query)
                    || contains(campagne, query)
                    || contains(entite, query)
                    || contains(rdv.getStatus(), query)
                    || (rdv.getDateDon() != null && rdv.getDateDon().toString().toLowerCase().contains(query));
        } catch (SQLException e) {
            return false;
        }
    }

    @FXML
    private void handleResetFilters() {
        if (searchField != null) {
            searchField.clear();
        }
        if (statusFilter != null) {
            statusFilter.getSelectionModel().clearSelection();
        }
        if (dateFilter != null) {
            dateFilter.setValue(null);
        }
    }

    @FXML
    private void handleExport() {
        ObservableList<RendezVous> data = tableView.getItems();
        if (data == null || data.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Aucune donnee a exporter.").showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les rendez-vous");
        fileChooser.setInitialFileName("RendezVous.xls");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel 97-2003", "*.xls"));
        File file = fileChooser.showSaveDialog(exportBtn.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (HSSFWorkbook workbook = new HSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
            var sheet = workbook.createSheet("RendezVous");
            var titleRow = sheet.createRow(0);
            var titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Historique des Rendez-Vous");

            var titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());

            var titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            String[] headers = {"Nom", "Prenom", "Age", "Sexe", "Poids", "Groupe sanguin", "Campagne", "Date", "Statut", "Entite"};
            var headerRow = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            for (int i = 0; i < data.size(); i++) {
                RendezVous rdv = data.get(i);
                var row = sheet.createRow(i + 2);
                writeExportRow(row, rdv);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            new Alert(Alert.AlertType.INFORMATION, "Fichier exporte avec succes.").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur export: " + e.getMessage()).showAndWait();
        }
    }

    private void writeExportRow(org.apache.poi.ss.usermodel.Row row, RendezVous rdv) {
        try {
            Questionnaire q = questionnaireService.getQuestionnaireById(rdv.getQuestionnaire_id());
            row.createCell(0).setCellValue(q.getNom());
            row.createCell(1).setCellValue(q.getPrenom());
            row.createCell(2).setCellValue(q.getAge());
            row.createCell(3).setCellValue(q.getSexe());
            row.createCell(4).setCellValue(q.getPoids());
            row.createCell(5).setCellValue(q.getGroupeSanguin());
            row.createCell(6).setCellValue(campagneService.getCampagneById(q.getCampagneId()).getTitre());
        } catch (Exception e) {
            row.createCell(0).setCellValue("N/A");
        }

        row.createCell(7).setCellValue(rdv.getDateDon() == null ? "" : rdv.getDateDon().toString());
        row.createCell(8).setCellValue(rdv.getStatus());
        try {
            row.createCell(9).setCellValue(entiteService.getEntiteById(rdv.getEntite_id()).getNom());
        } catch (Exception e) {
            row.createCell(9).setCellValue("N/A");
        }
    }

    private void deleteRendezVous(RendezVous rdv) {
        try {
            rdvService.supprimer(rdv);
            rendezVousData.remove(rdv);
            updateTotal(tableView.getItems().size());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void openEditScreen(RendezVous rdv) {
        try {
            Questionnaire q = questionnaireService.getQuestionnaireById(rdv.getQuestionnaire_id());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateRdvAdmin.fxml"));
            Parent root = loader.load();

            UpdateRdvAdmin controller = loader.getController();
            controller.setData(rdv, q);
            controller.setCampagne(campagneService.getCampagneById(q.getCampagneId()));

            tableView.getScene().setRoot(root);
        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String initials(String prenom, String nom) {
        String init1 = prenom == null || prenom.isBlank() ? "" : prenom.substring(0, 1).toUpperCase();
        String init2 = nom == null || nom.isBlank() ? "" : nom.substring(0, 1).toUpperCase();
        return init1 + init2;
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private void updateTotal() {
        updateTotal(rendezVousData.size());
    }

    private void updateTotal(int count) {
        if (totalRdvBtn != null) {
            totalRdvBtn.setText(count + " RDV");
        }
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
        navigateTo(event, "/admin_users.fxml");
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
    }

    @FXML
    void handleNavigateCampagnes(ActionEvent event) {
        navigateTo(event, "/ListeCampagnesAdmin.fxml");
    }

    @FXML
    void handleNavigateCollectes(ActionEvent event) {
        navigateTo(event, "/ListeEntitesAdmin.fxml");
    }

    private void navigateTo(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            tn.esprit.tools.ThemeManager.getInstance().setScene(stage, root);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to navigate to " + path + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
