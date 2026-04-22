package tn.esprit.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;

public class GoogleCalendarOAuthService {

    private static final String APPLICATION_NAME = "BloodLink";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private Calendar calendarService;

    public Calendar getCalendarService() throws Exception {
        if (calendarService == null) {
            authorize();
        }
        return calendarService;
    }

    private void authorize() throws Exception {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        InputStream in = getClass().getResourceAsStream("/credentials.json");
        if (in == null) throw new Exception("credentials.json not found in resources!");

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY,
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in)),
                Collections.singleton(CalendarScopes.CALENDAR))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

        calendarService = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public String addRendezVous(String patientName, String campagne,
                                LocalDateTime dateTime, String entite) throws Exception {
        Event event = new Event()
                .setSummary("🩸 Don de sang — " + campagne)
                .setLocation(entite)
                .setDescription("Rendez-vous BloodLink pour " + patientName
                        + "\nCampagne : " + campagne
                        + "\nEntité de collecte : " + entite);

        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(
                        dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                .setTimeZone("Africa/Tunis");

        EventDateTime end = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(
                        dateTime.plusMinutes(45).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
                .setTimeZone("Africa/Tunis");

        event.setStart(start);
        event.setEnd(end);

        Event created = getCalendarService().events()
                .insert("primary", event)
                .execute();

        return created.getHtmlLink();
    }
}