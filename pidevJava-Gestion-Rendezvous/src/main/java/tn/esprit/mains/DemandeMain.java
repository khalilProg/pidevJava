package tn.esprit.mains;

import tn.esprit.entities.Demande;
import tn.esprit.entities.Banque;
import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.client;
import tn.esprit.services.DemandeService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DemandeMain {
    public static void main(String[] args) throws SQLException {
        DemandeService ds = new DemandeService();

        // Créer un objet Banque avec ID statique
        Banque banque = new Banque();
        banque.setId(1);
        client client= new client();
        client.setId(1);// ID existant dans la base

        // Créer la demande
        Demande d = new Demande();
        d.setBanque(1);       // lien avec la banque
        d.setClientId(1);          // ID client statique déjà existant
        d.setQuantite(5);
        d.setTypeSang("O+");
        d.setUrgence("Haute");
        d.setStatus("En attente");
        d.setCreatedAt(LocalDateTime.now());
        d.setUpdatedAt(LocalDateTime.now());
     //Ajout
        //try {
          //  ds.ajouter(d);
        //} catch (SQLException e) {
          //  System.out.println(e.getMessage());
        //}


       // System.out.println("Demande ajoutée avec succès !")
        // Suppression
      //  try {
        //    Demande d1=new Demande(10,1,1,5,"A+","Haute","En attente",LocalDateTime.now(),LocalDateTime.now());
          //  ds.supprimer(d1);
           // System.out.println("demande supprimé!");
       // } catch (SQLException e) {
         //   System.out.println(e.getMessage());
       // }
        //Modification
      //  Demande d2=new Demande(5,1,1,5,"A+","Haute","En attente",LocalDateTime.now(),LocalDateTime.now());
       // d2.setQuantite(60);
        //d2.setTypeSang("A-");
       // try {
         //   ds.modifier(d2);
        //} catch (SQLException e) {
          //  System.out.println(e.getMessage());
        //}
        //recuperation
        try {
            List<Demande> demandes = ds.recuperer();

            System.out.println("vos demandes:");
            for (Demande d3 : demandes) {
                System.out.println(d3);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
