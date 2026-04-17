package tn.esprit.services;

import org.junit.jupiter.api.*;
import tn.esprit.entities.Campagne;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CampagneServiceTest {

    static CampagneService service;

    @BeforeAll
    static void setup() {
        service = new CampagneService();
    }

    static int testCampagneId;

    @Test
    @Order(1)
    void testRecuperer() throws SQLException {
        List<Campagne> campagnes = service.recuperer();
        assertFalse(campagnes.isEmpty(), "Should return at least one campagne");

        // store one ID for later
        testCampagneId = campagnes.get(0).getId();

        // Stream verification: all titres should not be empty
        assertTrue(campagnes.stream().allMatch(c -> c.getTitre() != null && !c.getTitre().isEmpty()));
    }

    @Test
    @Order(2)
    void testGetCampagneById() throws SQLException {
        Campagne c = service.getCampagneById(testCampagneId);
        assertNotNull(c, "Campagne should not be null");
        assertEquals(testCampagneId, c.getId(), "IDs should match");
        assertNotNull(c.getTitre(), "Titre should not be null");
    }

    @Test
    @Order(3)
    void testInvalidId() throws SQLException {
        Campagne c = service.getCampagneById(-1);
        assertNull(c, "Invalid ID should return null");
    }
}