/*package tn.esprit.mains;

import tn.esprit.tools.MyDatabase;
import java.sql.Connection;

public class MainApp {
    public static void main(String[] args) {
        // On appelle l'instance (ceci déclenche le constructeur et donc la connexion)
        Connection cnx = MyDatabase.getInstance().getCnx();

        if (cnx != null) {
            System.out.println("🚀 Test réussi : La connexion est active !");
        } else {
            System.out.println("RTFM ❌ Test échoué : Vérifiez si XAMPP/WAMP est lancé.");
        }
    }
}
*/