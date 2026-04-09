package tn.esprit.services;

import tn.esprit.entities.Questionnaire;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuestionnaireService implements IGeneralService<Questionnaire> {
    Connection cn;

    public QuestionnaireService() {
        cn = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Questionnaire questionnaire) throws SQLException {
        int clientId = 1;
        int campaignId = 2;
        String sql = "INSERT INTO questionnaire(nom,prenom,age,sexe,poids,autres,client_id,campagne_id,date,group_sanguin) VALUES(?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement q = cn.prepareStatement(sql);
        q.setString(1, questionnaire.getNom());
        q.setString(2, questionnaire.getPrenom());
        q.setInt(3, questionnaire.getAge());
        q.setString(4, questionnaire.getSexe());
        q.setDouble(5, questionnaire.getPoids());
        q.setString(6, questionnaire.getAutres());
        q.setInt(7, clientId);
        q.setInt(8, campaignId);
        q.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
        q.setString(10, questionnaire.getGroupeSanguin());
        System.out.println("executing insert...");
        q.executeUpdate();
    }

    @Override
    public void supprimer(Questionnaire questionnaire) {
        try {
            String rdv = "DELETE FROM rendez_vous WHERE questionnaire_id = ?";
            PreparedStatement pstRV = cn.prepareStatement(rdv);
            pstRV.setInt(1, questionnaire.getId());
            pstRV.executeUpdate();

            String qq = "DELETE FROM questionnaire WHERE id = ?";
            PreparedStatement q = cn.prepareStatement(qq);
            q.setInt(1, questionnaire.getId());
            q.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur suppression questionnaire: " + e.getMessage());
        }
    }

    @Override
    public int chercher(Questionnaire questionnaire) {
        try {
            String sql = "SELECT 1 FROM questionnaire WHERE id = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, questionnaire.getId());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                System.out.println("Ce questionnaire existe avec l'id " + questionnaire.getId());
            } else {
                System.out.println("Ce questionnaire n'existe pas");
            }
        } catch (SQLException e) {
            System.out.println("Erreur recherche questionnaire: " + e.getMessage());
        }
        return questionnaire.getId();
    }

    @Override
    public void modifier(Questionnaire questionnaire) {
        try {
            if (chercher(questionnaire) == questionnaire.getId()) {
                String sql = "UPDATE questionnaire SET nom=?, prenom=?, age=?, sexe=?, poids=?, autres=?, date=?, group_sanguin=? WHERE id=?";
                PreparedStatement pst = cn.prepareStatement(sql);
                pst.setString(1, questionnaire.getNom());
                pst.setString(2, questionnaire.getPrenom());
                pst.setInt(3, questionnaire.getAge());
                pst.setString(4, questionnaire.getSexe());
                pst.setDouble(5, questionnaire.getPoids());
                pst.setString(6, questionnaire.getAutres());
                pst.setTimestamp(7, Timestamp.valueOf(questionnaire.getDate()));
                pst.setString(8, questionnaire.getGroupeSanguin());
                pst.setInt(9, questionnaire.getId());
                pst.executeUpdate();
            } else {
                System.out.println("Ce questionnaire n'existe pas");
            }
        } catch (SQLException e) {
            System.out.println("Erreur modification questionnaire: " + e.getMessage());
        }
    }

    @Override
    public List<Questionnaire> recuperer() throws SQLException {
        String sql = "SELECT * FROM questionnaire";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<Questionnaire> questionnaires = new ArrayList<>();
        while (rs.next()) {
            Questionnaire q = new Questionnaire(
                    rs.getInt("id"),
                    rs.getInt("age"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("sexe"),
                    rs.getString("autres"),
                    rs.getString("group_sanguin"),
                    rs.getDouble("poids"),
                    rs.getTimestamp("date").toLocalDateTime()
            );
            questionnaires.add(q);
        }
        return questionnaires;
    }
}
