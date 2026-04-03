package tn.esprit.entities;

import java.time.LocalDateTime;
import java.util.List;

public class Don {

    private int id;
    private String typeDon;
    private double quantite;
    private LocalDateTime date, createdAt, updatedAt;
    //entite_id ??
    private int entiteId = 1;
    private List<DossierMedical> dossierMedicals;

    public int getId() {
        return id;
    }

    public List<DossierMedical> getDossierMedicals() {
        return dossierMedicals;
    }

    public void setDossierMedicals(List<DossierMedical> dossierMedicals) {
        this.dossierMedicals = dossierMedicals;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Don{" +
                "id=" + id +
                ", typeDon='" + typeDon + '\'' +
                ", quantite=" + quantite +
                ", date=" + date +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", entiteId=" + entiteId +
                '}';
    }

    public Don(){}

    public Don(int id, String typeDon, double quantite, LocalDateTime date, LocalDateTime createdAt, LocalDateTime updatedAt, int entiteId) {
        this.id = id;
        this.typeDon = typeDon;
        this.quantite = quantite;
        this.date = date;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.entiteId = entiteId;
    }

    public String getTypeDon() {
        return typeDon;
    }

    public void setTypeDon(String typeDon) {
        this.typeDon = typeDon;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getEntiteId() {
        return entiteId;
    }

    public void setEntiteId(int entiteId) {
        this.entiteId = entiteId;
    }

}
