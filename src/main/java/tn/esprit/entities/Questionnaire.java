package tn.esprit.entities;

import java.time.LocalDateTime;

public class Questionnaire {
    private int id, age, clientId, campagneId;
    private String nom, prenom, sexe, autres, groupeSanguin;
    private double poids;

    public Questionnaire() {}

    public Questionnaire(String nom, String prenom, int age, String sexe, double poids, String autres, LocalDateTime date, String groupeSanguin) {
        this.age = age;
        this.nom = nom;
        this.prenom = prenom;
        this.sexe = sexe;
        this.autres = autres;
        this.groupeSanguin = groupeSanguin;
        this.poids = poids;
        this.date = date;
    }
    public Questionnaire(int id, String nom, String prenom, int age, String sexe, double poids, String autres, LocalDateTime date, String groupSanguin) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.sexe = sexe;
        this.poids = poids;
        this.autres = autres;
        this.date = date;
        this.groupeSanguin = groupSanguin;
    }

    public Questionnaire(String nom, String prenom, int age, String sexe, double poids, String autres, int clientId, int campagneId, LocalDateTime date, String groupSanguin) {
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.sexe = sexe;
        this.poids = poids;
        this.autres = autres;
        this.clientId =  clientId;
        this.campagneId = campagneId;
        this.date = date;
        this.groupeSanguin = groupSanguin;
    }

    public Questionnaire(int id, String nom, String prenom, int age, String sexe, double poids, String autres, int clientId, int campagneId, LocalDateTime date, String groupSanguin) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.sexe = sexe;
        this.poids = poids;
        this.autres = autres;
        this.clientId =  clientId;
        this.campagneId = campagneId;
        this.date = date;
        this.groupeSanguin = groupSanguin;
    }

    @Override
    public String toString() {
        return "Questionnaire{" +
                "id=" + id +
                ", age=" + age +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", sexe='" + sexe + '\'' +
                ", autres='" + autres + '\'' +
                ", groupeSanguin='" + groupeSanguin + '\'' +
                ", poids=" + poids +
                ", date=" + date +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getAutres() {
        return autres;
    }

    public void setAutres(String autres) {
        this.autres = autres;
    }

    public String getGroupeSanguin() {
        return groupeSanguin;
    }

    public void setGroupeSanguin(String groupeSanguin) {
        this.groupeSanguin = groupeSanguin;
    }

    public double getPoids() {
        return poids;
    }

    public void setPoids(double poids) {
        this.poids = poids;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    private LocalDateTime date;

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getCampagneId() {
        return campagneId;
    }

    public void setCampagneId(int campagneId) {
        this.campagneId = campagneId;
    }
}
