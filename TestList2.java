import java.sql.Connection;
import tn.esprit.tools.MyDatabase;
import tn.esprit.services.*;
import tn.esprit.entities.*;

public class TestList2 {
    public static void main(String[] args) throws Exception {
        CampagneService cs = new CampagneService();
        Campagne c = cs.getCampagneById(2);
        if (c == null) {
            System.out.println("Campagne is null!");
        } else {
            System.out.println("Campagne Title: " + c.getTitre());
        }
    }
}
