package tn.esprit.entities;

import javax.print.Doc;
import java.time.LocalDate;
import java.util.List;

public class client {

    private int id;
    private String typeSang;
    private LocalDate dernierDon;
    private User user;
    private List<Commande> commandes;
    private List<Demande> demandes;
    private List<Don> dons;
    private List<DossierMedical> dossierMedicals;
    private List<Questionnaire> questionnaires;

    public List<Commande> getCommandes() {
        return commandes;
    }

    public void setCommandes(List<Commande> commandes) {
        this.commandes = commandes;
    }

    public List<Demande> getDemandes() {
        return demandes;
    }

    public void setDemandes(List<Demande> demandes) {
        this.demandes = demandes;
    }

    public List<Don> getDons() {
        return dons;
    }

    public void setDons(List<Don> dons) {
        this.dons = dons;
    }

    public List<DossierMedical> getDossierMedicals() {
        return dossierMedicals;
    }

    public void setDossierMedicals(List<DossierMedical> dossierMedicals) {
        this.dossierMedicals = dossierMedicals;
    }

    public List<Questionnaire> getQuestionnaires() {
        return questionnaires;
    }

    public void setQuestionnaires(List<Questionnaire> questionnaires) {
        this.questionnaires = questionnaires;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "client{" +
                "id=" + id +
                ", typeSang='" + typeSang + '\'' +
                ", dernierDon=" + dernierDon +
                ", user=" + user +
                '}';
    }

    public void setId(int id) {
        this.id = id;
    }

    public client(){}
    public client(int id, String typeSang, LocalDate dernierDon, User user) {
        this.id = id;
        this.typeSang = typeSang;
        this.dernierDon = dernierDon;
        this.user = user;
    }

    public String getTypeSang() {
        return typeSang;
    }

    public void setTypeSang(String typeSang) {
        this.typeSang = typeSang;
    }

    public LocalDate getDernierDon() {
        return dernierDon;
    }

    public void setDernierDon(LocalDate dernierDon) {
        this.dernierDon = dernierDon;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
