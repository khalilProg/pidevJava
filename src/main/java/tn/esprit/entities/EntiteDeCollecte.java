package tn.esprit.entities;

import java.util.List;

public class EntiteDeCollecte {
    private int id;
    private String nom, tel, type, adresse, ville;
    private List<Campagne> campagnes;
    private List<RendezVous> rendezVouses;

    public EntiteDeCollecte() {}

    public List<Campagne> getCampagnes() {
        return campagnes;
    }

    public void setCampagnes(List<Campagne> campagnes) {
        this.campagnes = campagnes;
    }

    public List<RendezVous> getRendezVouses() {
        return rendezVouses;
    }

    public void setRendezVouses(List<RendezVous> rendezVouses) {
        this.rendezVouses = rendezVouses;
    }

    public EntiteDeCollecte(int id, String nom, String tel, String type, String adresse, String ville) {
        this.id = id;
        this.nom = nom;
        this.tel = tel;
        this.type = type;
        this.adresse = adresse;
        this.ville = ville;
    }

    @Override
    public String toString() {
        return "EntiteDeCollecte{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", tel='" + tel + '\'' +
                ", type='" + type + '\'' +
                ", adresse='" + adresse + '\'' +
                ", ville='" + ville + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }
}
