package tn.esprit.mains;

import tn.esprit.entities.Campagne;

import tn.esprit.entities.Client;
import tn.esprit.entities.Questionnaire;
import tn.esprit.services.QuestionnaireService;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class QuestionnaireMain {
    public static void main(String[] args) throws SQLException {
        QuestionnaireService qs = new QuestionnaireService();
        Questionnaire q4 = new Questionnaire("bhy","joujou",23, "femme",70,"", LocalDateTime.now(), "A+");
        Questionnaire q5 = new Questionnaire("ben haj yahia","wajd",24, "femme",80,"", LocalDateTime.now(), "A+");
        Questionnaire q6 = new Questionnaire("ben yahia","wajd",50, "homme",80,"", LocalDateTime.now(), "A+");
//        //ajout
//        try {
//            qs.ajouter(q6);
//          //qs.ajouter(q5);
//            System.out.println("questionnaire ajouté!");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }

//        //suppression
//        try {
//            Questionnaire q7 = new Questionnaire(51,"ben yahia","wajd",50, "homme",80,"", LocalDateTime.now(), "A+");
//            qs.supprimer(q7);
//            System.out.println("questionnaire supprimé!");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }

        //recuperation
        try {
            List<Questionnaire> questionnaires = qs.recuperer();

            System.out.println("Questionnaires in the database:");
            for (Questionnaire q : questionnaires) {
                System.out.println(q);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

//        //modif
//        Questionnaire q7 = new Questionnaire(52,"ben yahia","wajouda",50, "homme",50,"", LocalDateTime.now(), "A+");
//        q7.setPoids(60);
//        q7.setPrenom("joujou");
//        try {
//            qs.modifier(q7);
//            System.out.println("questionnaire modifié!");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }

//        //chercher
//        Questionnaire q7 = new Questionnaire(51,"ben yahia","wajouda",50, "homme",50,"", LocalDateTime.now(), "A+");
//        try{
//            qs.chercher(q7);
//        } catch (SQLException e){
//            System.out.println(e.getMessage());
//        }

    }
}
