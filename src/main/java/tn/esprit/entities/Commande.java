package tn.esprit.entities;

public class Commande {
    private int id;
    private int banqueId;
    private int clientId;
    private int stockId;
    private int reference;
    private int quantite;
    private String priorite;
    private String typeSang;
    private String status;

    // Constructor without id (for INSERT)
    public Commande(int banqueId, int clientId, int stockId, int reference, int quantite,
                    String priorite, String typeSang, String status) {
        this.banqueId = banqueId;
        this.clientId = clientId;
        this.stockId = stockId;
        this.reference = reference;
        this.quantite = quantite;
        this.priorite = priorite;
        this.typeSang = typeSang;
        this.status = status;
    }

    // Full constructor (for SELECT)
    public Commande(int id, int banqueId, int clientId, int stockId, int reference, int quantite,
                    String priorite, String typeSang, String status) {
        this.id = id;
        this.banqueId = banqueId;
        this.clientId = clientId;
        this.stockId = stockId;
        this.reference = reference;
        this.quantite = quantite;
        this.priorite = priorite;
        this.typeSang = typeSang;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBanqueId() { return banqueId; }
    public void setBanqueId(int banqueId) { this.banqueId = banqueId; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

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

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", banqueId=" + banqueId +
                ", clientId=" + clientId +
                ", stockId=" + stockId +
                ", reference=" + reference +
                ", quantite=" + quantite +
                ", priorite='" + priorite + '\'' +
                ", typeSang='" + typeSang + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
