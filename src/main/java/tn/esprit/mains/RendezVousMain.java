package tn.esprit.mains;

import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;
import tn.esprit.services.QuestionnaireService;
import tn.esprit.services.RendezVousService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class RendezVousMain {
    public static void main(String[] args) {
        RendezVousService rdv = new RendezVousService();
        RendezVous rd1 = new RendezVous("annulé", LocalDateTime.of(2026, 2, 5, 15, 0));

//        //ajout
//        try {
//            rdv.ajouter(rd1);
//            System.out.println("rendez vous ajouté!");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }

//        //suppression
//        try {
//            RendezVous rd2 = new RendezVous(20,"annulé", LocalDateTime.of(2026, 2, 5, 15, 0));
//            rdv.supprimer(rd2);
//            System.out.println("rendez vous supprimé!");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }

//        //chercher
//        RendezVous rd2 = new RendezVous(2,"annulé", LocalDateTime.of(2026, 2, 5, 15, 0));
//        try{
//            rdv.chercher(rd2);
//        } catch (SQLException e){
//            System.out.println(e.getMessage());
//        }

//        //modif
//        RendezVous rd2 = new RendezVous(2,"annulé", LocalDateTime.of(2026, 2, 5, 15, 0));
//        rd2.setStatus("confirmé");
//        try {
//            rdv.modifier(rd2);
//            System.out.println("rendez vous modifié!");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }

//        //recuperation
//        try {
//            List<RendezVous> rendevouet = rdv.recuperer();
//
//            System.out.println("Rendez vous in the database:");
//            for (RendezVous rv : rendevouet) {
//                System.out.println(rv);
//            }
//
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }

    }
}
