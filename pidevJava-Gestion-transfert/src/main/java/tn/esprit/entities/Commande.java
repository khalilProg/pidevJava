package tn.esprit.entities;

public class Commande {
    private int id, reference, quantite;
    private String priorite, typeSang, status;

    public Commande() {}

    public Commande(int id, int reference, int quantite, String priorite,
                   String typeSang, String status, Banque banque,
                   User client, Stock stock) {
        this.id = id;
        this.reference = reference;
        this.quantite = quantite;
        this.priorite = priorite;
        this.typeSang = typeSang;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReference() { return reference; }
    public void setReference(int reference) { this.reference = reference; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getTypeSang() { return typeSang; }
    public void setTypeSang(String typeSang) { this.typeSang = typeSang; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}
