package tn.esprit.mains;

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
////            qs.ajouter(q5);
//            System.out.println("questionnaire ajouté!");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }
//
//        System.out.println(qs.getIdFromDB(q6));
//        suppression
//        try {
//            qs.supprimer(q6);
//            System.out.println("questionnaire supprimé!");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }

//        //recuperation
//        try {
//            List<Questionnaire> questionnaires = qs.recuperer();
//
//            System.out.println("Questionnaires in the database:");
//            for (Questionnaire q : questionnaires) {
//                System.out.println(q);
//            }
//
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }
//
//        //modif
//        Questionnaire backUp = new Questionnaire(q6);
//        int id = qs.getIdFromDB(backUp);
//        q6.setPoids(60);
//        q6.setPrenom("jowjow");
//
//        try {
//            qs.modifier(q6, id);
//            System.out.println("questionnaire modifié!");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }

        //chercher
        try{
            qs.chercher(q6);
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }
}
