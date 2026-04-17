package tn.esprit.mains;

import tn.esprit.entities.Demande;
import tn.esprit.entities.Transfert;
import tn.esprit.services.TransfertService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TransfertMain {
    public static void main(String[] args) throws SQLException {
        TransfertService ts = new TransfertService();
        Demande d= new Demande(5,1,1,5,"A+","Haute","En attente", LocalDateTime.now(),LocalDateTime.now());
        Transfert t=new Transfert();
        t.setDemande(d);
        t.setStock(1);
        t.setFromOrgId(0);
        t.setFromOrg("Bloodlink");
        t.setToOrgId(1);
        t.setToOrg("clinique taoufik");
        t.setDateEnvoie(LocalDate.now());
        t.setDateReception(LocalDate.now());
        t.setQuantite(30);
        t.setStatus("En attente");
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        //Ajout
        //try {
          //ts.ajouter(t);
       // } catch (SQLException e) {
         // System.out.println(e.getMessage());
        //}
        //Suppression
       // try {
         //     Transfert t2 = new Transfert();
           //   t2.setId(5);
             // ts.supprimer(t2);
             //System.out.println("transfert supprimé!");
             //} catch (SQLException e) {
              // System.out.println(e.getMessage());
            // }
        //Modification
        //t.setId(3);
         //t.setQuantite(60);
        //t.setToOrgId(3);
         //try {
          // ts.modifier(t);
        //} catch (SQLException e) {
         // System.out.println(e.getMessage());
        //}
        try {
            List<Transfert> transferts = ts.recuperer();

            System.out.println("vos transferts:");
            for (Transfert t4 : transferts) {
                System.out.println(t4);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
