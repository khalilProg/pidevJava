package tn.esprit.mains;

import tn.esprit.entities.Campagne;
import tn.esprit.entities.Client;
import tn.esprit.entities.Questionnaire;
import tn.esprit.services.CampagneService;
import tn.esprit.services.QuestionnaireService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class CampagneMain {
    public static void main(String[] args) {
        CampagneService co = new CampagneService();
        Client c = new Client(2, "A-", LocalDate.of(2003, 10, 17));
        try{
            List<Campagne> campagnes = co.recupererByClient(c);
            if(campagnes.isEmpty()){
                System.out.println("no campagnes available");
            }else{
                System.out.println("Campagnes available:");
                for (Campagne comp : campagnes) {
                    System.out.println(comp.toString());
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
