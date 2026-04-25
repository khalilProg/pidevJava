package tn.esprit.tools; // Assure-toi que c'est bien 'tools' ici

public class SessionManager {
    // Par défaut, on met des valeurs pour éviter que l'app crash
    public static int userId = 1;
    public static String role = "Client";

    public static void login(int id, String userRole) {
        userId = id;
        role = userRole;
        System.out.println("LOG: Session active pour [" + role + "] ID: " + userId);
    }
}