package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Campagne;
import tn.esprit.services.CampagneService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class CampagneCalendarController extends BaseFront implements Initializable {

    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;

    private YearMonth currentYearMonth;
    private CampagneService service = new CampagneService();
    private List<Campagne> allCampagnes = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applySessionUser();
        currentYearMonth = YearMonth.now();
        try {
            allCampagnes = service.recuperer();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        drawCalendar();
    }

    private void drawCalendar() {
        calendarGrid.getChildren().clear();
        monthYearLabel.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase() + " " + currentYearMonth.getYear());

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Mon, ..., 7 = Sun
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // Fill empty slots before the first day of the month
        int currentColumn = dayOfWeek - 1;
        int currentRow = 0;

        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = currentYearMonth.atDay(i);
            VBox dayBox = createDayBox(i, date);
            calendarGrid.add(dayBox, currentColumn, currentRow);

            currentColumn++;
            if (currentColumn > 6) {
                currentColumn = 0;
                currentRow++;
            }
        }
    }

    private VBox createDayBox(int dayNumber, LocalDate date) {
        VBox vbox = new VBox(5);
        vbox.getStyleClass().add("calendar-day-box");
        if (date.equals(LocalDate.now())) {
            vbox.getStyleClass().add("calendar-day-today");
        }

        Label lblDay = new Label(String.valueOf(dayNumber));
        lblDay.getStyleClass().add("calendar-day-number");
        vbox.getChildren().add(lblDay);

        // Add campaigns for this day
        for (Campagne c : allCampagnes) {
            if ((date.isEqual(c.getDateDebut()) || date.isAfter(c.getDateDebut())) &&
                (date.isEqual(c.getDateFin()) || date.isBefore(c.getDateFin()))) {
                
                Label lblCampaign = new Label(c.getTitre());
                lblCampaign.getStyleClass().add("calendar-event-tag");
                lblCampaign.setMaxWidth(Double.MAX_VALUE);
                vbox.getChildren().add(lblCampaign);
            }
        }

        return vbox;
    }

    @FXML
    void previousMonth(ActionEvent event) {
        currentYearMonth = currentYearMonth.minusMonths(1);
        drawCalendar();
    }

    @FXML
    void nextMonth(ActionEvent event) {
        currentYearMonth = currentYearMonth.plusMonths(1);
        drawCalendar();
    }

    @FXML
    void goToGrid(ActionEvent event) {
        switchScene(event, "/cnts_agent_home.fxml");
    }

    @FXML
    @Override
    public void goToAccueil(javafx.event.Event event) {
        switchScene(event, "/cnts_agent_home.fxml");
    }

    @FXML
    @Override
    public void goToCampagnes(javafx.event.Event event) {
        switchScene(event, "/cnts_agent_home.fxml");
    }
}
