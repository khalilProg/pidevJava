package tn.esprit.entities;

import java.time.LocalDateTime;

public class Demande {

    private int id;
    private int banque;
    private int client; // ✅ ajouté
    private int quantite;
    private String typeSang, urgence, status;
    private LocalDateTime createdAt, updatedAt;

    // 🔹 constructeur vide
    public Demande(){}

    // 🔹 constructeur complet
    public Demande(int id, int banque, int client, int quantite,
                   String typeSang, String urgence, String status,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.banque = banque;
        this.client = client;
        this.quantite = quantite;
        this.typeSang = typeSang;
        this.urgence = urgence;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 🔹 GETTERS & SETTERS

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBanque() {
        return banque;
    }

    public void setBanque(int banque) {
        this.banque = banque;
    }

    // 👉 utile pour SQL
    public int getIdBanque() {
        return banque;
    }

    public int getClientId() {
        return client;
    }

    public void setClientId(int client) {
        this.client = client;
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

    // 🔹 toString
    @Override
    public String toString() {
        return "Demande{" +
            "id=" + id +
            ", banque=" + banque +
            ", clientId=" + client +
            ", quantite=" + quantite +
            ", typeSang='" + typeSang + '\'' +
            ", urgence='" + urgence + '\'' +
            ", status='" + status + '\'' +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
