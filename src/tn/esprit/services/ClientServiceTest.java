package tn.esprit.services;

import org.junit.jupiter.api.*;
import tn.esprit.entities.Client;
import tn.esprit.entities.User;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientServiceTest {

    static ClientService service;

    @BeforeAll
    static void setup() {
        service = new ClientService();
    }

    static final String EXISTING_PHONE = "+21695122960"; // set a phone number that exists in DB
    static final String NON_EXISTING_PHONE = "0000000000";

    @Test
    @Order(1)
    void testGetByPhoneExists() throws SQLException {
        Client client = service.getByPhone(EXISTING_PHONE);
        assertNotNull(client, "Client should exist for the given phone");
        assertNotNull(client.getUser(), "User object should be populated");
        assertEquals(EXISTING_PHONE, client.getUser().getTel(), "Phone should match");

        // Stream-like verification: make sure name and prenom are not empty
        assertTrue(!client.getUser().getNom().isEmpty() && !client.getUser().getPrenom().isEmpty());
        assertNotNull(client.getTypeSang(), "Type de sang should not be null");
        assertNotNull(client.getDernierDon(), "Dernier don should not be null");
    }

    @Test
    @Order(2)
    void testGetByPhoneNotExists() throws SQLException {
        Client client = service.getByPhone(NON_EXISTING_PHONE);
        assertNull(client, "Should return null for non-existing phone");
    }
}