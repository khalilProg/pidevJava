package tn.esprit.entities;

public class DossierMed {
    private int id;
    private float taille;
    private float poid;
    private float temperature;
    private String sexe;
    private int contact_urgence;
    private String nom;
    private String prenom;
    private int age;
    private String type_sang;
    private int id_client;
    private int id_don;

    public DossierMed() {}

    public DossierMed(int id, float taille, float poid, float temperature, String sexe, int contact_urgence, String nom, String prenom, int age, String type_sang, int id_client, int id_don) {
        this.id = id;
        this.taille = taille;
        this.poid = poid;
        this.temperature = temperature;
        this.sexe = sexe;
        this.contact_urgence = contact_urgence;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.type_sang = type_sang;
        this.id_client = id_client;
        this.id_don = id_don;
    }

    // --- BUSINESS LOGIC (BMI) ---
    public float calculateBMI() {
        if (taille <= 0) return 0; // Prevent division by zero
        float heightInMeters = taille / 100.0f;
        return poid / (heightInMeters * heightInMeters);
    }

    public String getBMICategory() {
        float bmi = calculateBMI();
        if (bmi == 0) return "INVALID BIOMETRICS";
        if (bmi < 18.5) return "UNDERWEIGHT - Action Required";
        if (bmi < 25) return "STABLE - Optimal for Donation";
        if (bmi < 30) return "OVERWEIGHT - Monitor";
        return "OBESE - Action Required";
    }

    // --- GETTERS AND SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public float getTaille() { return taille; }
    public void setTaille(float taille) { this.taille = taille; }
    public float getPoid() { return poid; }
    public void setPoid(float poid) { this.poid = poid; }
    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }
    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }
    public int getContact_urgence() { return contact_urgence; }
    public void setContact_urgence(int contact_urgence) { this.contact_urgence = contact_urgence; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getType_sang() { return type_sang; }
    public void setType_sang(String type_sang) { this.type_sang = type_sang; }
    public int getId_client() { return id_client; }
    public void setId_client(int id_client) { this.id_client = id_client; }
    public int getId_don() { return id_don; }
    public void setId_don(int id_don) { this.id_don = id_don; }
}