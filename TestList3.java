import java.sql.Connection;
import tn.esprit.tools.MyDatabase;
import tn.esprit.services.*;
import tn.esprit.entities.*;

public class TestList3 {
    public static void main(String[] args) throws Exception {
        EntiteCollecteService es = new EntiteCollecteService();
        EntiteCollecte e = es.getEntiteById(1);
        if (e == null) {
            System.out.println("Entite is null!");
        } else {
            System.out.println("Entite Title: " + e.getNom());
        }
    }
}
