package tn.esprit.mains;

import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.services.EntiteCollecteService;

import java.sql.SQLException;
import java.util.Scanner;

public class EntiteCollecteMain {
    public static void main(String[] args) {

        EntiteCollecteService service = new EntiteCollecteService();
        Scanner sc = new Scanner(System.in);

        System.out.println(" Main EntiteCollecte lancé !");

        // 🔹 1. AJOUT
        EntiteDeCollecte e = new EntiteDeCollecte(
                0,
                "test",
                "22345671",
                "Hopital",
                "Tunis Centre",
                "Tunis");

        try {
            service.ajouter(e);
            System.out.println(" Ajout tenté !");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        EntiteDeCollecte e2 = new EntiteDeCollecte(
                0,
                "test2",
                "22345672",
                "Hopital",
                "Tunis Centre",
                "Tunis");

        try {
            service.ajouter(e);
            System.out.println(" Ajout tenté !");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        // 🔹 2. AFFICHAGE
        try {
            System.out.println("\n Liste complète :");
            System.out.println(service.recuperer());
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        // 🔹 3. RECHERCHE PAR NOM (INPUT)
        System.out.println("\n Entrez un nom à rechercher : ");
        String nomRecherche = sc.nextLine();

        System.out.println(" Résultat de recherche :");
        System.out.println(service.rechercherParNom(nomRecherche));

        // 🔹 4. CHERCHER (unicité → retourne ID)
        int idTrouve = service.chercher(e);
        System.out.println("\n  ID trouvé : " + idTrouve);

        // 🔹 5. MODIFIER
        if (idTrouve != -1) {
            System.out.println("\n Modification de la ville...");
            e.setId(idTrouve);
            e.setVille("Sfax");
            service.modifier(e);
            System.out.println(" Modification faite !");
        } else {
            System.out.println(" Impossible de modifier (non trouvé)");
        }

        int idTrouve2 = service.chercher(e2);
        // 🔹 6. SUPPRIMER
        if (idTrouve2 != -1) {
            System.out.println("\n Suppression...");
            service.supprimer(e);
            System.out.println(" Suppression faite !");
        } else {
            System.out.println(" Impossible de supprimer (non trouvé)");
        }

        sc.close();
    }
}