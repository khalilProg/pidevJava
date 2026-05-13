package tn.esprit.services;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import tn.esprit.entities.RendezVous;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RendezVousServiceTest {

    static RendezVousService service;
    static int testRdvId;

    @BeforeAll
    static void setup() {
        service = new RendezVousService();
    }

    @Test
    @Order(1)
    void testAjouterRendezVous() throws SQLException {
        RendezVous rdv = new RendezVous(
                "confirmé",
                LocalDateTime.now().plusDays(1),
                63,
                1   // entite_id
        );
        service.ajouter(rdv);

        List<RendezVous> allRdvs = service.recuperer();
        assertTrue(allRdvs.stream().anyMatch(r -> r.getQuestionnaire_id() == 63 &&
                        r.getEntite_id() == 1),
                "RendezVous should be added to the database");

    }

    @Test
    @Order(2)
    void testModifierRendezVous() throws SQLException {
        // Fetch the rendez-vous to update
        RendezVous rdv = service.recuperer().stream()
                .filter(r -> r.getId() == 54) // use the ID stored from add test
                .findFirst()
                .orElseThrow(() -> new AssertionError("RendezVous not found"));

        // Update status
        rdv.setStatus("annulé");
        service.modifier(rdv);

        // Verify the status is updated in DB
        boolean updated = service.recuperer().stream()
                .anyMatch(r -> r.getId() == 54 &&
                        "annulé".equals(r.getStatus()));

        assertTrue(updated, "RendezVous status should be 'annulé'");
    }

    @Test
    @Order(3)
    void testSupprimerRendezVous() throws SQLException {
        RendezVous rdv = service.recuperer().stream()
                .filter(r -> r.getId() == 33)
                .findFirst()
                .orElse(null);

        assertNotNull(rdv, "RendezVous should exist before deletion");

        service.supprimer(rdv);

        // Verify deletion
        assertFalse(service.recuperer().stream()
                        .anyMatch(r -> r.getId() == 33),
                "RendezVous should be removed from database");
    }

    @Test
    @Order(4)
    void testHasRendezVous() throws SQLException {
        boolean exists = service.hasRendezVous(1, 2);
        // Just prints, but you can assert based on your DB state
        System.out.println("Client 1 has rdv for campagne 1: " + exists);
    }

    @Test
    @Order(5)
    void testSupprimerForClient() throws SQLException {
        // Add a new rdv for testing
        RendezVous rdv = new RendezVous(
                "confirmé",
                LocalDateTime.now().plusDays(1),
                60,
                1
        );
        service.ajouter(rdv);
        int id = service.recuperer().get(service.recuperer().size() - 1).getId();

        boolean success = service.supprimerForClient(id);
        assertTrue(success, "RendezVous should be marked as 'annulé'");

        RendezVous updatedRdv = service.recuperer().stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElse(null);

        assertNotNull(updatedRdv);
        assertEquals("annulé", updatedRdv.getStatus());
    }
}