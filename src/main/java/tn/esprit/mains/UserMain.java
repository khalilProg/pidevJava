package tn.esprit.mains;

import tn.esprit.entities.User;
import tn.esprit.services.UserService;

import java.sql.SQLException;
import java.util.List;

public class UserMain {
    public static void main(String[] args) throws SQLException {

        UserService userService = new UserService();

        System.out.println("===== AJOUT USER =====");
        User u1 = new User("khalil@test.com", "Ben Ali", "Khalil", "pass123", "client", "12345678");
        userService.ajouter(u1);

        User u2 = new User("agence@test.com", "Agence Tunis", "Central", "pass456", "banque agent", "98765432");
        userService.ajouter(u2);

        System.out.println("\n===== LISTE DES USERS =====");
        List<User> users = userService.recuperer();
        for (User u : users) {
            System.out.println(u);
        }

        System.out.println("\n===== MODIFICATION USER =====");
        u1.setNom("Ben Ali Updated");
        u1.setTel("11111111");
        userService.modifier(u1);

        System.out.println("\n===== LISTE APRES MODIFICATION =====");
        users = userService.recuperer();
        for (User u : users) {
            System.out.println(u);
        }

        System.out.println("\n===== SUPPRESSION USER =====");
        userService.supprimer(u1);
        userService.supprimer(u2);

        System.out.println("\n===== LISTE APRES SUPPRESSION =====");
        users = userService.recuperer();
        for (User u : users) {
            System.out.println(u);
        }

        System.out.println("\n===== TEST USER TERMINÉ =====");
    }
}
