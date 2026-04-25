package tn.esprit.entities;

public class User {
    private int id;
    private String nom, prenom, email, password, role;

    public User(String nom, String prenom, String email, String password, String role) {
        this.nom = nom; this.prenom = prenom; this.email = email;
        this.password = password; this.role = role;
    }

    public User(int id, String nom, String prenom, String email, String password, String role) {
        this.id = id; this.nom = nom; this.prenom = prenom;
        this.email = email; this.password = password; this.role = role;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}