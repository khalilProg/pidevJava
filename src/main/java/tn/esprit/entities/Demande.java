package tn.esprit.entities;

import java.time.LocalDateTime;
import java.util.List;

public class Demande {

    private int id;
    private int quantite;
    private String typeSang, urgence, status;
    private LocalDateTime createdAt, updatedAt;
    private List<Transfert> transferts;

    public Demande(){}

    public List<Transfert> getTransferts() {
        return transferts;
    }

    public void setTransferts(List<Transfert> transferts) {
        this.transferts = transferts;
    }

    public Demande(int id, int quantite, String typeSang, String urgence, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.quantite = quantite;
        this.typeSang = typeSang;
        this.urgence = urgence;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Demande{" +
                "id=" + id +
                ", quantite=" + quantite +
                ", typeSang='" + typeSang + '\'' +
                ", urgence='" + urgence + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getTypeSang() {
        return typeSang;
    }

    public void setTypeSang(String typeSang) {
        this.typeSang = typeSang;
    }

    public String getUrgence() {
        return urgence;
    }

    public void setUrgence(String urgence) {
        this.urgence = urgence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

}
