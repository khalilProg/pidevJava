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
    public int getIdFromDB(Questionnaire questionnaire) throws SQLException {
        String sql = "SELECT id FROM questionnaire WHERE nom=? AND prenom=? AND age=? AND sexe=? AND poids=? AND autres=? AND group_sanguin=?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setString(1, questionnaire.getNom());
        pst.setString(2, questionnaire.getPrenom());
        pst.setInt(3, questionnaire.getAge());
        pst.setString(4, questionnaire.getSexe());
        pst.setDouble(5, questionnaire.getPoids());
        pst.setString(6, questionnaire.getAutres());
//        pst.setTimestamp(7, Timestamp.valueOf(questionnaire.getDate()));
        pst.setString(7, questionnaire.getGroupeSanguin());

        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            int id = rs.getInt("id");
            questionnaire.setId(id);
            return id;
        } else {
            return 0;
        }
    }
    @Override
    public void ajouter(Questionnaire questionnaire) throws SQLException {
        int clientId=1;
        int campaignId=2;
        String sql = "insert into questionnaire(nom,prenom,age,sexe,poids,autres,client_id,campagne_id,date,group_sanguin) values(?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement q = cn.prepareStatement(sql); //, Statement.RETURN_GENERATED_KEYS
        q.setString(1,questionnaire.getNom());
        q.setString(2,questionnaire.getPrenom());
        q.setInt(3,questionnaire.getAge());
        q.setString(4,questionnaire.getSexe());
        q.setDouble(5,questionnaire.getPoids());
        q.setString(6,questionnaire.getAutres());
        q.setInt(7, clientId);
        q.setInt(8, campaignId);
        q.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
        q.setString(10, questionnaire.getGroupeSanguin());
        System.out.println("executing insert...");
        q.executeUpdate();

//        ResultSet rs = q.getGeneratedKeys();
//        if (rs.next()) {
//            int generatedId = rs.getInt(1);
//            questionnaire.setId(generatedId);
//            System.out.println("Inserted Questionnaire ID: " + generatedId);
//        }
    }

    public void supprimer(Questionnaire questionnaire) throws SQLException {
        int id=getIdFromDB(questionnaire);
        // Delete the child first (RendezVous)
        String rdv = "DELETE FROM rendez_vous WHERE questionnaire_id = ?";
        PreparedStatement pstRV = cn.prepareStatement(rdv);
        pstRV.setInt(1, id);
        pstRV.executeUpdate();

        // Delete the parent
        String sqlQ = "DELETE FROM questionnaire WHERE id = ?";
        PreparedStatement q = cn.prepareStatement(sqlQ);
        q.setInt(1, id);
        q.executeUpdate();

    }
    @Override
    public int chercher(Questionnaire questionnaire) throws SQLException {
        int id = getIdFromDB(questionnaire);
        if (id!=0){
            System.out.println("ce quest existe");
        }else{
            System.out.println("ce quest n'existe pas");
        }
        return id;
    }

    @Override
    public void modifier(Questionnaire questionnaire, int id) throws SQLException {
        String sql = "UPDATE questionnaire SET nom = ?, prenom = ?, age = ?, sexe = ?, poids = ?, autres = ?, date = ?, group_sanguin = ? WHERE id=?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setString(1, questionnaire.getNom());
        pst.setString(2, questionnaire.getPrenom());
        pst.setInt(3, questionnaire.getAge());
        pst.setString(4, questionnaire.getSexe());
        pst.setDouble(5, questionnaire.getPoids());
        pst.setString(6, questionnaire.getAutres());
        pst.setTimestamp(7, Timestamp.valueOf(questionnaire.getDate()));
        pst.setString(8, questionnaire.getGroupeSanguin());
        pst.setInt(9, id);
        pst.executeUpdate();
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
                    rs.getTimestamp("date").toLocalDateTime(),
                    rs.getString("group_sanguin"));
            questionnaires.add(q);
        }

        return questionnaires;
    }
}
