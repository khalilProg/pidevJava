package tn.esprit.entities;

import java.sql.Timestamp;

public class Stock {
    private int id;
    private int typeOrgid;
    private String typeOrg;
    private String typeSang;
    private int quantite;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Stock() {}

    public Stock(int typeOrgid, String typeOrg, String typeSang, int quantite) {
        this.typeOrgid = typeOrgid;
        this.typeOrg = typeOrg;
        this.typeSang = typeSang;
        this.quantite = quantite;
    }

    public Stock(int id, int typeOrgid, String typeOrg, String typeSang, int quantite, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.typeOrgid = typeOrgid;
        this.typeOrg = typeOrg;
        this.typeSang = typeSang;
        this.quantite = quantite;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public int getId() { return id; }
    public int getTypeOrgid() { return typeOrgid; }
    public String getTypeOrg() { return typeOrg; }
    public String getTypeSang() { return typeSang; }
    public int getQuantite() { return quantite; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    public void setId(int id) { this.id = id; }
    public void setTypeOrgid(int typeOrgid) { this.typeOrgid = typeOrgid; }
    public void setTypeOrg(String typeOrg) { this.typeOrg = typeOrg; }
    public void setTypeSang(String typeSang) { this.typeSang = typeSang; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                ", typeOrgid=" + typeOrgid +
                ", typeOrg='" + typeOrg + '\'' +
                ", typeSang='" + typeSang + '\'' +
                ", quantite=" + quantite +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
