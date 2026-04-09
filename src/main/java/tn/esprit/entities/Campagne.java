package tn.esprit.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Campagne {

    private int id;
    private String titre, description,typeSang;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalDateTime createdAt,updatedAt;
    private List<EntiteDeCollecte> entiteDeCollectes;
    private List<Questionnaire> questionnaires;

    public Campagne() {}

    public List<EntiteDeCollecte> getEntiteDeCollectes() {
        return entiteDeCollectes;
    }

    public void setEntiteDeCollectes(List<EntiteDeCollecte> entiteDeCollectes) {
        this.entiteDeCollectes = entiteDeCollectes;
    }

    public List<Questionnaire> getQuestionnaires() {
        return questionnaires;
    }

    public void setQuestionnaires(List<Questionnaire> questionnaires) {
        this.questionnaires = questionnaires;
    }

    @Override
    public String toString() {
        return "Campagne{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", typeSang='" + typeSang + '\'' +
                '}';
    }

    public Campagne(int id) {
        this.id = id;
    }

    public Campagne(int id, String titre, String description, LocalDate dateDebut, LocalDate dateFin, LocalDateTime createdAt, LocalDateTime updatedAt, String typeSang) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.typeSang = typeSang;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
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

    public String getTypeSang() {
        return typeSang;
    }

    public void setTypeSang(String typeSang) {
        this.typeSang = typeSang;
    }

}
