package tn.esprit.entities;

import java.time.LocalDateTime;
import java.time.LocalDate;

public class Transfert {

    private LocalDate dateEnvoie, dateReception;
    private LocalDateTime createdAt, updatedAt;
    private String fromOrg, toOrg,status;
    private int id,fromOrgId,toOrgId,quantite,stock;
    private Demande demande;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFromOrgId() {
        return fromOrgId;
    }

    public void setFromOrgId(int fromOrgId) {
        this.fromOrgId = fromOrgId;
    }

    public int getToOrgId() {
        return toOrgId;
    }

    public void setToOrgId(int toOrgId) {
        this.toOrgId = toOrgId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public LocalDate getDateEnvoie() {
        return dateEnvoie;
    }

    public void setDateEnvoie(LocalDate dateEnvoie) {
        this.dateEnvoie = dateEnvoie;
    }

    public LocalDate getDateReception() {
        return dateReception;
    }

    public void setDateReception(LocalDate dateReception) {
        this.dateReception = dateReception;
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

    public String getFromOrg() {
        return fromOrg;
    }

    public void setFromOrg(String fromOrg) {
        this.fromOrg = fromOrg;
    }

    public String getToOrg() {
        return toOrg;
    }

    public void setToOrg(String toOrg) {
        this.toOrg = toOrg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Transfert{" +
                "id=" + id +
                ", fromOrgId=" + fromOrgId +
                ", toOrgId=" + toOrgId +
                ", quantite=" + quantite +
                ", dateEnvoie=" + dateEnvoie +
                ", dateReception=" + dateReception +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", fromOrg='" + fromOrg + '\'' +
                ", toOrg='" + toOrg + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public Transfert() {}

    public Transfert(int id,Demande demande,int stock, int fromOrgId, int toOrgId, int quantite, LocalDate dateEnvoie, LocalDate dateReception, LocalDateTime createdAt, LocalDateTime updatedAt, String fromOrg, String toOrg, String status) {
        this.id = id;
        this.demande = demande;
        this.stock = stock;
        this.fromOrgId = fromOrgId;
        this.toOrgId = toOrgId;
        this.quantite = quantite;
        this.dateEnvoie = dateEnvoie;
        this.dateReception = dateReception;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.fromOrg = fromOrg;
        this.toOrg = toOrg;
        this.status = status;
    }


    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Demande getDemande() {
        return demande;
    }

    public void setDemande(Demande demande) {
        this.demande = demande;
    }
}
