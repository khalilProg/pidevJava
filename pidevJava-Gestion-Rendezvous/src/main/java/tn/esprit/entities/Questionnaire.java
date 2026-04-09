package tn.esprit.entities;

import com.mysql.cj.xdevapi.Client;

import java.time.LocalDateTime;

public class Questionnaire {
    private int id, age;
    private String nom, prenom, sexe, autres, groupeSanguin;
    private double poids;
    private RendezVous rendezVous;
    private Campagne campagne;
    private client client;

    public Questionnaire() {}

    public RendezVous getRendezVous() {
        return rendezVous;
    }

    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;
    }
    public Questionnaire(int id, String nom, String prenom, int age, String sexe, double poids, String autres, LocalDateTime date, String groupeSanguin) {
        this.id = id;
        this.age = age;
        this.nom = nom;
        this.prenom = prenom;
        this.sexe = sexe;
        this.autres = autres;
        this.groupeSanguin = groupeSanguin;
        this.poids = poids;
        this.date = date;
    }
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

    public Questionnaire(Questionnaire q) {
        this.age = q.getAge();
        this.nom = q.getNom();
        this.prenom = q.getPrenom();
        this.sexe = q.getSexe();
        this.autres = q.getAutres();
        this.groupeSanguin = q.getGroupeSanguin();
        this.poids = q.getPoids();
        this.date = q.getDate();
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

    public Campagne getCampagne() {
        return campagne;
    }

    public void setCampagne(Campagne campagne) {
        this.campagne = campagne;
    }

    public client getClient() {
        return client;
    }

    public void setClient(client client) {
        this.client = client;
    }
}
