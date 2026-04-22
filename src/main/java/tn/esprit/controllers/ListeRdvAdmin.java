package tn.esprit.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;
import tn.esprit.entities.User;
import tn.esprit.services.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ListeRdvAdmin {

    @FXML private CalendarView calendarView;
    @FXML private TableView<RendezVous> tableView;
    @FXML private TableColumn<RendezVous, String> dateColumn;
    @FXML private TableColumn<RendezVous, String> statusColumn;
//    @FXML private TableColumn<RendezVous, String> qIdColumn;
    @FXML private TableColumn<RendezVous, String> entiteColumn;
    @FXML private TableColumn<RendezVous, Void> actionsColumn;
    private final RendezVousService rdvService = new RendezVousService();
    @FXML private Button exportBtn;
    @FXML private BarChart<String, Integer> rdvChart;
    @FXML private TableView<RendezVous> rdvTable;

    @FXML public void initialize() {
        try {
            setupTableColumns();
            loadCalendarEntries();
            showTableForDate(null);

            // Update table when date changes
            calendarView.dateProperty().addListener((obs, oldDate, newDate) -> showTableForDate(newDate));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateDon().toString())
        );
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus())
        );
//        qIdColumn.setCellValueFactory(cellData ->
//                new SimpleStringProperty(String.valueOf(cellData.getValue().getQuestionnaire_id()))
//        );
        entiteColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(
                        new EntiteCollecteService().getEntiteById(cellData.getValue().getEntite_id()).getNom()
                );
            } catch (SQLException e) {
                e.printStackTrace();
                return new SimpleStringProperty("N/A");
            }
        });

        //Actions column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button updateBtn = new Button("Update");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(5, updateBtn, deleteBtn);
            {
                deleteBtn.setOnAction(e -> {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    try {
                        rdvService.supprimer(rdv);
                        getTableView().getItems().remove(rdv);
                        loadCalendarEntries(); // refresh calendar
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                updateBtn.setOnAction(e -> {
                    try {
                        RendezVous rdv = getTableView().getItems().get(getIndex());
                        Questionnaire q = new QuestionnaireService().getQuestionnaireById(rdv.getQuestionnaire_id());

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateRdvAdmin.fxml"));
                        Parent root = loader.load();

                        UpdateRdvAdmin controller = loader.getController();

                        controller.setData(rdv, q);
                        controller.setCampagne(new CampagneService().getCampagneById(q.getCampagneId()));

                        tableView.getScene().setRoot(root);
                    } catch (IOException | SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    RendezVous rdv = getTableView().getItems().get(getIndex());
                    updateBtn.setVisible(rdv.getDateDon().isAfter(LocalDateTime.now()));
                    setGraphic(container);
                }

            }
        });
    }

    private void loadCalendarEntries() throws SQLException {
        // creation cal
        Calendar rdvCalendar = new Calendar("Rendez-vous");
        rdvCalendar.setStyle(Calendar.Style.STYLE4);

        // cal source
        CalendarSource source = new CalendarSource("Calendrier Admin");
        source.getCalendars().add(rdvCalendar);

        // clear existing sources and add mine
        calendarView.getCalendarSources().clear();
        calendarView.getCalendarSources().add(source);

        List<RendezVous> rdvs = rdvService.recuperer();

        for (RendezVous rdv : rdvs) {
            if (rdv.getDateDon() != null) {
                Entry<RendezVous> entry = new Entry<>("RDV #" + rdv.getId());
                entry.setUserObject(rdv);
                // search bar
                entry.setTitle(rdv.getStatus());
                // Set start and end date/time
                entry.changeStartDate(rdv.getDateDon().toLocalDate());
                entry.changeStartTime(rdv.getDateDon().toLocalTime());
                entry.changeEndTime(rdv.getDateDon().toLocalTime().plusHours(1)); // à verifier bch nrodha 15min

                // Add entry to the calendar
                rdvCalendar.addEntry(entry);
            }
        }
    }

    private void showTableForDate(LocalDate date) {
        try {
            if(date!=null) {
                List<RendezVous> rdvs = rdvService.recuperer().stream()
                        .filter(r -> r.getDateDon() != null && r.getDateDon().toLocalDate().equals(date))
                        .toList();

                tableView.setItems(FXCollections.observableArrayList(rdvs));
            }else{
                List<RendezVous> rdvs = rdvService.recuperer();
                tableView.setItems(FXCollections.observableArrayList(rdvs));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML private void handleExport() {
        ObservableList<RendezVous> data = tableView.getItems();
        if (data.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Aucune donnée à exporter !").showAndWait();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setInitialFileName("RendezVous.xls");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xls"));
        File file = fc.showSaveDialog(exportBtn.getScene().getWindow());
        if (file == null) return;

        try (var wb = new org.apache.poi.hssf.usermodel.HSSFWorkbook();
             var out = new java.io.FileOutputStream(file)) {

            var sheet = wb.createSheet("RendezVous");
            //title
            var titleRow  = sheet.createRow(0);
            var titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Historique des Rendez-Vous");

            var titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined.WHITE.getIndex());

            var titleStyle = wb.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined.RED.getIndex());
            titleStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            titleCell.setCellStyle(titleStyle);

            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 9));

            // Header
            String[] headers = {"Nom", "Prénom", "Âge", "Sexe", "Poids", "Groupe Sanguin", "Autres informations", "Date du rendez vous", "Statut", "Entité de collecte"};
            var headerRow = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++)
                headerRow.createCell(i).setCellValue(headers[i]);

            // Data
            for (int i = 0; i < data.size(); i++) {
                var row = sheet.createRow(i + 2);
                RendezVous rdv = data.get(i);

                try {
                    Questionnaire q = new QuestionnaireService().getQuestionnaireById(rdv.getQuestionnaire_id());

                    row.createCell(0).setCellValue(q.getNom());
                    row.createCell(1).setCellValue(q.getPrenom());
                    row.createCell(2).setCellValue(q.getAge());
                    row.createCell(3).setCellValue(q.getSexe());
                    row.createCell(4).setCellValue(q.getPoids());
                    row.createCell(5).setCellValue(q.getGroupeSanguin());
                    row.createCell(6).setCellValue(q.getAutres());
                } catch (Exception e) {
                    row.createCell(0).setCellValue("N/A");
                }

                row.createCell(7).setCellValue(rdv.getDateDon().toString());
                row.createCell(8).setCellValue(rdv.getStatus());

                try {
                    row.createCell(9).setCellValue(
                            new EntiteCollecteService().getEntiteById(rdv.getEntite_id()).getNom()
                    );
                } catch (Exception e) {
                    row.createCell(9).setCellValue("N/A");
                }
            }

            // Auto size all columns
            for (int i = 0; i < headers.length; i++)
                sheet.autoSizeColumn(i);

            wb.write(out);
            new Alert(Alert.AlertType.INFORMATION, "Fichier exporté avec succès !").showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleGenerateReport() {
        ObservableList<RendezVous> filteredRdvs = tableView.getItems(); // filtered based on selected date
        if (filteredRdvs.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Aucune donnée disponible pour le rapport !").showAndWait();
            return;
        }

        try {
            // Map each rendez-vous to include Questionnaire data
            List<RendezVous> rdvWithQuestionnaire = filteredRdvs.stream().toList();

            // Generate PDF report
            String filePath = System.getProperty("user.home") + "/Desktop/RendezVousReport.pdf";
            new AiReportService().generatePdfReport(rdvWithQuestionnaire, filePath);

            new Alert(Alert.AlertType.INFORMATION, "Rapport généré avec succès à : " + filePath).show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération du rapport : " + e.getMessage()).show();
        }
    }

//    public void updateChart() {
//        XYChart.Series<String, Integer> series = new XYChart.Series<>();
//        series.setName("Rendez-vous");
//
//        // 1. Initialize a Map for the 7 days of the week
//        Map<String, Integer> stats = new LinkedHashMap<>();
//        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
//        for (String day : days) stats.put(day, 0);
//
//        // 2. Optimized: One pass through the existing table data
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
//
//        for (RendezVous rdv : rdvTable.getItems()) {
//            // Only count if it's within the current week (optional logic)
//            String dayName = rdv.getDate().format(formatter); // e.g. "Mon"
//            if (stats.containsKey(dayName)) {
//                stats.put(dayName, stats.get(dayName) + 1);
//            }
//        }
//
//        // 3. Populate the series
//        stats.forEach((day, count) -> {
//            series.getData().add(new XYChart.Data<>(day, count));
//        });
//
//        rdvChart.getData().clear();
//        rdvChart.getData().add(series);
//    }
}
