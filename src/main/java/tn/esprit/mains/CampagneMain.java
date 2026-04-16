package tn.esprit.mains;

import tn.esprit.entities.Campagne;
import tn.esprit.services.CampagneService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;

public class CampagneMain {
    public static void main(String[] args) {

        CampagneService service = new CampagneService();
        Scanner sc = new Scanner(System.in);

        System.out.println(" Main Campagne lancé !");

        // 🔹 Création
        Campagne c = new Campagne(
                0,
                "Campagne Don Sang",
                "Collecte nationale",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                LocalDateTime.now(),
                null,
                "[\"A+\",\"O+\"]"
        );

        // 🔹 Ajout
        try {
            service.ajouter(c);
            System.out.println(" Ajout effectué !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        Campagne c2 = new Campagne(
                0,
                "Campagne 2 Don Sang",
                "Collecte nationale2",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                LocalDateTime.now(),
                null,
                "[\"A+\",\"O+\"]"
        );

        // 🔹 Ajout
        try {
            service.ajouter(c2);
            System.out.println(" Ajout effectué !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // 🔹 Affichage
        try {
            System.out.println("\n Liste des campagnes :");
            System.out.println(service.recuperer());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // 🔹 Recherche par titre (input)
        System.out.println("\n Entrez un titre : ");
        String titre = sc.nextLine();

        System.out.println(" Résultat :");
        System.out.println(service.rechercherParTitre(titre));

        // 🔹 Chercher (unicité)
        int idTrouve = service.chercher(c);
        System.out.println("\n🔍 ID trouvé : " + idTrouve);

        // 🔹 Modifier
        if (idTrouve != -1) {
            c.setId(idTrouve);
            c.setDescription("Nouvelle description");
            service.modifier(c);
            System.out.println("✏️ Modification faite !");
        }
        int idTrouve2 = service.chercher(c2);
        System.out.println("\n🔍 ID trouvé : " + idTrouve);
        // 🔹 Supprimer
        if (idTrouve2 != -1) {
            service.supprimer(c2);
            System.out.println("🗑️ Suppression faite !");
        }

        sc.close();
    }
}