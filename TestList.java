import java.sql.Connection;
import tn.esprit.tools.MyDatabase;
import tn.esprit.services.*;
import tn.esprit.entities.*;
import java.util.List;

public class TestList {
    public static void main(String[] args) throws Exception {
        RendezVousService rdvService = new RendezVousService();
        QuestionnaireService qsService = new QuestionnaireService();
        List<RendezVous> rendezvous = rdvService.recuperer();
        int currentClientId = 15; // khalilboujemaa23@gmail.com
        System.out.println("Total rendezvous in DB: " + rendezvous.size());
        
        int matchCount = 0;
        for (RendezVous rdv : rendezvous) {
            Questionnaire q = qsService.getQuestionnaireById(rdv.getQuestionnaire_id());
            if (q != null) {
                System.out.println("Found Q: " + q.getId() + " Client: " + q.getClientId() + " Status: " + rdv.getStatus());
                if (q.getClientId() == currentClientId && !isCancelled(rdv.getStatus())) {
                    matchCount++;
                }
            } else {
                System.out.println("Q is null for RDV: " + rdv.getId() + " QID: " + rdv.getQuestionnaire_id());
            }
        }
        System.out.println("Matches for client 15: " + matchCount);
    }
    
    private static boolean isCancelled(String status) {
        return status != null && (status.equalsIgnoreCase("annule")
                || status.equalsIgnoreCase("annulee")
                || status.equalsIgnoreCase("annulé")
                || status.equalsIgnoreCase("annulée"));
    }
}
