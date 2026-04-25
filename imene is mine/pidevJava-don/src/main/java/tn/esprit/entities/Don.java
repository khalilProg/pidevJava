package tn.esprit.entities;

public class Don {
    private int id;
    private int id_client;
    private int id_entite;
    private float quantite;
    private String type_don;

    public Don() {}
    public Don(int id, int id_client, int id_entite, float quantite, String type_don) {
        this.id = id; this.id_client = id_client; this.id_entite = id_entite;
        this.quantite = quantite; this.type_don = type_don;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getId_client() { return id_client; }
    public void setId_client(int id_client) { this.id_client = id_client; }
    public int getId_entite() { return id_entite; }
    public void setId_entite(int id_entite) { this.id_entite = id_entite; }
    public float getQuantite() { return quantite; }
    public void setQuantite(float quantite) { this.quantite = quantite; }
    public String getType_don() { return type_don; }
    public void setType_don(String type_don) { this.type_don = type_don; }
}