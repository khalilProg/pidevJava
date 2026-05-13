package tn.esprit.services;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import tn.esprit.entities.Questionnaire;

import java.sql.SQLException;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QuestionnaireServiceTest {

    static QuestionnaireService service;
    static int testQuestionnaireId;

    @BeforeAll
    static void setup() {
        service = new QuestionnaireService();
    }

    @Test
    @Order(1)
    void testAjouterQuestionnaire() throws SQLException {
        Questionnaire q = new Questionnaire(
                "TestNom",
                "TestPrenom",
                25,
                "Homme",
                70.0,
                "Aucune",
                1,
                3,
                java.time.LocalDateTime.now(),
                "A+"
        );
        service.ajouter(q);

        List<Questionnaire> questionnaires = service.recuperer();
        assertTrue(questionnaires.stream().anyMatch(qq -> qq.getNom().equals("TestNom")),
                "Should contain the newly added questionnaire");

        testQuestionnaireId = questionnaires.get(questionnaires.size() - 1).getId();
    }

    @Test
    @Order(2)
    void testModifierQuestionnaire() throws SQLException {
        List<Questionnaire> questionnaires = service.recuperer();
        questionnaires.stream()
                .filter(q -> q.getId() == testQuestionnaireId)
                .findFirst()
                .ifPresent(q -> {
                    q.setNom("NomModifie");
                    q.setPrenom("PrenomModifie");
                    q.setPoids(75.0);
                    try {
                        service.modifier(q);
                    } catch (SQLException e) {
                        fail("Modifier threw SQLException: " + e.getMessage());
                    }
                });

        // Verify using stream
        assertTrue(service.recuperer().stream()
                .anyMatch(q -> q.getId() == testQuestionnaireId &&
                        q.getNom().equals("NomModifie") &&
                        q.getPrenom().equals("PrenomModifie") &&
                        q.getPoids() == 75.0));
    }

    @Test
    @Order(3)
    void testSupprimerQuestionnaire() throws SQLException {
        Questionnaire q = service.recuperer().stream()
                .filter(qq -> qq.getId() == testQuestionnaireId)
                .findFirst()
                .orElse(null);

        assertNotNull(q, "Questionnaire should exist before deletion");

        service.supprimer(q);

        // Verify deletion
        assertFalse(service.recuperer().stream()
                        .anyMatch(qq -> qq.getId() == testQuestionnaireId),
                "Questionnaire should be removed from DB");
    }
}