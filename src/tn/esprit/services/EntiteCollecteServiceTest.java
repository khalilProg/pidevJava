package tn.esprit.services;

import org.junit.jupiter.api.*;
import tn.esprit.entities.EntiteDeCollecte;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntiteCollecteServiceTest {

    static EntiteCollecteService service;

    @BeforeAll
    static void setup() {
        service = new EntiteCollecteService();
    }

    static int testEntiteId;

    @Test
    @Order(1)
    void testGetByCampagneId() throws SQLException {
        List<EntiteDeCollecte> entities = service.getByCampagneId(1); // assume campagne ID 1 exists
        assertFalse(entities.isEmpty(), "Should return at least one entity");
        testEntiteId = entities.get(0).getId();

        // stream verification
        assertTrue(entities.stream().allMatch(e -> e.getId() > 0), "All entities should have valid IDs");
    }

    @Test
    @Order(2)
    void testGetEntiteById() throws SQLException {
        EntiteDeCollecte entite = service.getEntiteById(testEntiteId);
        assertNotNull(entite, "Entity should not be null");
        assertEquals(testEntiteId, entite.getId(), "IDs should match");
        assertNotNull(entite.getNom(), "Name should not be null");
    }

    @Test
    @Order(3)
    void testInvalidId() throws SQLException {
        EntiteDeCollecte entite = service.getEntiteById(-1);
        assertNull(entite, "Invalid ID should return null");
    }
}