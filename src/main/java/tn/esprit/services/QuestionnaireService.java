package tn.esprit.services;

import tn.esprit.entities.Campagne;
import tn.esprit.entities.Client;
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
        String sql = "insert into questionnaire(nom,prenom,age,sexe,poids,autres,client_id,campagne_id,date,group_sanguin) values(?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement q = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        q.setString(1,questionnaire.getNom());
        q.setString(2,questionnaire.getPrenom());
        q.setInt(3,questionnaire.getAge());
        q.setString(4,questionnaire.getSexe());
        q.setDouble(5,questionnaire.getPoids());
        q.setString(6,questionnaire.getAutres());
        q.setInt(7, questionnaire.getClientId());
        q.setInt(8, questionnaire.getCampagneId());
        q.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
        q.setString(10, questionnaire.getGroupeSanguin());
        System.out.println("executing insert...");
        q.executeUpdate();
        // generated id to use fil ajouter rendez vous
        ResultSet rs = q.getGeneratedKeys();
        if (rs.next()) {
            questionnaire.setId(rs.getInt(1));
        }
    }

    public void supprimer(Questionnaire questionnaire) throws SQLException {

        String rdv = "DELETE FROM rendez_vous WHERE questionnaire_id = ?";
        PreparedStatement pstRV = cn.prepareStatement(rdv);
        pstRV.setInt(1, questionnaire.getId());
        pstRV.executeUpdate();

        String qq = "DELETE FROM questionnaire WHERE id = ?";
        PreparedStatement q = cn.prepareStatement(qq);
        q.setInt(1, questionnaire.getId());
        q.executeUpdate();

    }
    @Override
    public int chercher(Questionnaire questionnaire) throws SQLException {
        String sql = "SELECT 1 FROM questionnaire WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, questionnaire.getId());
        ResultSet rs = pst.executeQuery();
        if (rs.next()){
            System.out.println("ce questionnaire existe avec l'id "+questionnaire.getId());
        }else{
            System.out.println("ce questionnaire n'existe pas");
        }
        return questionnaire.getId();
    }

    @Override
    public void modifier(Questionnaire questionnaire) throws SQLException {
        if(chercher(questionnaire)== questionnaire.getId()){
            String sql = "UPDATE questionnaire SET nom = ?, prenom = ?, age = ?, sexe = ?, poids = ?, autres = ?, date = ?, group_sanguin = ? WHERE id=?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, questionnaire.getNom());
            pst.setString(2, questionnaire.getPrenom());
            pst.setInt(3, questionnaire.getAge());
            pst.setString(4, questionnaire.getSexe());
            pst.setDouble(5, questionnaire.getPoids());
            pst.setString(6, questionnaire.getAutres());
            pst.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            pst.setString(8, questionnaire.getGroupeSanguin());
            pst.setInt(9, questionnaire.getId());
            pst.executeUpdate();
        }
        else {
            System.out.println("ce quest n'existe pas");
        }

    }

    @Override
    public List<Questionnaire> recuperer() throws SQLException {
        String sql = "select * from questionnaire";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<Questionnaire> questionnaires = new ArrayList<>();
        while(rs.next()){
            Questionnaire q = new Questionnaire(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getInt("age"),
                    rs.getString("sexe"),
                    rs.getDouble("poids"),
                    rs.getString("autres"),
                    rs.getInt("client_id"),
                    rs.getInt("campagne_id"),
                    rs.getTimestamp("date").toLocalDateTime(),
                    rs.getString("group_sanguin"));
            questionnaires.add(q);
        }

        return questionnaires;
    }

    public Questionnaire getQuestionnaireById(int id) throws SQLException {
        String sql = "SELECT * FROM questionnaire WHERE id = ?";
        PreparedStatement st = cn.prepareStatement(sql);
        st.setInt(1, id);
        ResultSet rs = st.executeQuery();

        if (rs.next()) {
            return new Questionnaire(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getInt("age"),
                    rs.getString("sexe"),
                    rs.getDouble("poids"),
                    rs.getString("autres"),
                    rs.getInt("client_id"),
                    rs.getInt("campagne_id"),
                    rs.getTimestamp("date").toLocalDateTime(),
                    rs.getString("group_sanguin")
            );
        }
        return null;
    }
}
