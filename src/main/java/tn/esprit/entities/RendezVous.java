package tn.esprit.entities;

import java.time.LocalDateTime;

public class RendezVous {
private int id;
private String status;
private LocalDateTime dateDon;
private Questionnaire questionnaire;
private int entite_id;
private int questionnaire_id;

public RendezVous(){}

public RendezVous(String status, LocalDateTime dateDon) {
    this.status = status;
    this.dateDon = dateDon;
}
public RendezVous(int id, String status, LocalDateTime dateDon) {
    this.id = id;
    this.status = status;
    this.dateDon = dateDon;
}
    public RendezVous(String status, LocalDateTime dateDon, int questionnaire_id, int entite_id) {
        this.status = status;
        this.dateDon = dateDon;
        this.questionnaire_id = questionnaire_id;
        this.entite_id = entite_id;
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

public Questionnaire getQuestionnaire() {
    return questionnaire;
}

public void setQuestionnaire(Questionnaire questionnaire) {
    this.questionnaire = questionnaire;
}

    public int getQuestionnaire_id() {
        return questionnaire_id;
    }

    public void setQuestionnaire_id(int questionnaire_id) {
        this.questionnaire_id = questionnaire_id;
    }

    public int getEntite_id() {
        return entite_id;
    }

    public void setEntite_id(int entite_id) {
        this.entite_id = entite_id;
    }
}
