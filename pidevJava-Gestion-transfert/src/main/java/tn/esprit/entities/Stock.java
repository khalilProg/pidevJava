package tn.esprit.entities;

import java.time.LocalDateTime;
import java.util.List;

public class Stock {
    private int id,quantite,typeOrgId;
    private String typeOrg,typeSang;
    private LocalDateTime createdAt;
    private List<Transfert> transferts;

    public List<Transfert> getTransferts() {
        return transferts;
    }

    public void setTransferts(List<Transfert> transferts) {
        this.transferts = transferts;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                ", quantite=" + quantite +
                ", typeOrgId=" + typeOrgId +
                ", typeOrg='" + typeOrg + '\'' +
                ", typeSang='" + typeSang + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", commandes=" + commandes +
                '}';
    }

    private LocalDateTime updatedAt;
    private List<Commande> commandes;
    public Stock(){}

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

    public int getTypeOrgId() {
        return typeOrgId;
    }

    public void setTypeOrgId(int typeOrgId) {
        this.typeOrgId = typeOrgId;
    }

    public String getTypeOrg() {
        return typeOrg;
    }

    public void setTypeOrg(String typeOrg) {
        this.typeOrg = typeOrg;
    }

    public String getTypeSang() {
        return typeSang;
    }

    public void setTypeSang(String typeSang) {
        this.typeSang = typeSang;
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

    public List<Commande> getCommandes() {
        return commandes;
    }

    public void setCommandes(List<Commande> commandes) {
        this.commandes = commandes;
    }

    public Stock(int id, int quantite, int typeOrgId, String typeOrg, String typeSang, LocalDateTime createdAt, LocalDateTime updatedAt, List<Commande> commandes) {
        this.id = id;
        this.quantite = quantite;
        this.typeOrgId = typeOrgId;
        this.typeOrg = typeOrg;
        this.typeSang = typeSang;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.commandes = commandes;
    }
}
