package tn.esprit.mains;

import tn.esprit.entities.Banque;
import tn.esprit.entities.User;
import tn.esprit.services.BanqueService;
import tn.esprit.services.UserService;

import java.sql.SQLException;
import java.util.List;

public class banqueMain {
    public static void main(String[] args) throws SQLException {

        UserService userService = new UserService();
        BanqueService banqueService = new BanqueService();

        User userParent = new User("banqueuser@test.com", "Agence Sfax", "Nord", "pass321", "banque agent", "71998877");
        userService.ajouter(userParent);

        System.out.println("\n===== AJOUT BANQUE =====");
        Banque bq = new Banque("Banque du Sang Tunis", "Av. Habib Bourguiba", "71123456", userParent);
        banqueService.ajouter(bq);

        System.out.println("\n===== LISTE DES BANQUES =====");
        List<Banque> banques = banqueService.recuperer();
        for (Banque b : banques) {
            System.out.println(b);
        }

        System.out.println("\n===== MODIFICATION BANQUE =====");
        bq.setNom("Banque du Sang Sfax");
        bq.setAdresse("Av. 7 Novembre, Sfax");
        banqueService.modifier(bq);

        System.out.println("\n===== LISTE APRES MODIFICATION =====");
        banques = banqueService.recuperer();
        for (Banque b : banques) {
            System.out.println(b);
        }

        System.out.println("\n===== SUPPRESSION BANQUE =====");
        banqueService.supprimer(bq);

        System.out.println("\n===== LISTE APRES SUPPRESSION =====");
        banques = banqueService.recuperer();
        for (Banque b : banques) {
            System.out.println(b);
        }

        userService.supprimer(userParent);

        System.out.println("\n===== TEST BANQUE TERMINÉ =====");
    }
}
