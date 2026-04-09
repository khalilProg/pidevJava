package tn.esprit.mains;

import tn.esprit.entities.User;
import tn.esprit.entities.client;
import tn.esprit.services.ClientService;
import tn.esprit.services.UserService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ClientMain {
    public static void main(String[] args) throws SQLException {

        UserService userService = new UserService();
        ClientService clientService = new ClientService();

        User userParent = new User("clientuser@test.com", "Trabelsi", "Amine", "pass789", "client", "55667788");
        userService.ajouter(userParent);

        System.out.println("\n===== AJOUT CLIENT =====");
        client cl = new client("A+", LocalDate.of(2025, 6, 15), userParent);
        clientService.ajouter(cl);

        System.out.println("\n===== LISTE DES CLIENTS =====");
        List<client> clients = clientService.recuperer();
        for (client c : clients) {
            System.out.println(c);
        }

        System.out.println("\n===== MODIFICATION CLIENT =====");
        cl.setTypeSang("O-");
        cl.setDernierDon(LocalDate.of(2026, 1, 10));
        clientService.modifier(cl);

        System.out.println("\n===== LISTE APRES MODIFICATION =====");
        clients = clientService.recuperer();
        for (client c : clients) {
            System.out.println(c);
        }

        System.out.println("\n===== SUPPRESSION CLIENT =====");
        clientService.supprimer(cl);

        System.out.println("\n===== LISTE APRES SUPPRESSION =====");
        clients = clientService.recuperer();
        for (client c : clients) {
            System.out.println(c);
        }

        userService.supprimer(userParent);

        System.out.println("\n===== TEST CLIENT TERMINÉ =====");
    }
}
