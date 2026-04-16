package tn.esprit.entities;

import java.time.LocalDateTime;

public class RendezVous {
    private int id;
    private String status;

    public RendezVous(){}

    public RendezVous(int id, String status, LocalDateTime dateDon) {
        this.id = id;
        this.status = status;
        this.dateDon = dateDon;
    }

    @Override
    public String toString() {
        return "RendezVous{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", dateDon=" + dateDon +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDateDon() {
        return dateDon;
    }

    public void setDateDon(LocalDateTime dateDon) {
        this.dateDon = dateDon;
    }

    private LocalDateTime dateDon;
}
