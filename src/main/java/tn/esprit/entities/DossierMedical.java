package tn.esprit.entities;

public class DossierMedical {
    private int id,contact_urgence,age;
    private String sexe, nom, prenom, typeSang;
    private double taille, poid, temperature;

    @Override
    public String toString() {
        return "DossierMedical{" +
                "id=" + id +
                ", contact_urgence=" + contact_urgence +
                ", age=" + age +
                ", sexe='" + sexe + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", typeSang='" + typeSang + '\'' +
                ", taille=" + taille +
                ", poid=" + poid +
                ", temperature=" + temperature +
                '}';
    }

    public DossierMedical(){}

    public DossierMedical(int id, int contact_urgence, int age, String sexe, String nom, String prenom, String typeSang, double taille, double poid, double temperature) {
        this.id = id;
        this.contact_urgence = contact_urgence;
        this.age = age;
        this.sexe = sexe;
        this.nom = nom;
        this.prenom = prenom;
        this.typeSang = typeSang;
        this.taille = taille;
        this.poid = poid;
        this.temperature = temperature;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContact_urgence() {
        return contact_urgence;
    }

    public void setContact_urgence(int contact_urgence) {
        this.contact_urgence = contact_urgence;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
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

    public String getTypeSang() {
        return typeSang;
    }

    public void setTypeSang(String typeSang) {
        this.typeSang = typeSang;
    }

    public double getTaille() {
        return taille;
    }

    public void setTaille(double taille) {
        this.taille = taille;
    }

    public double getPoid() {
        return poid;
    }

    public void setPoid(double poid) {
        this.poid = poid;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
