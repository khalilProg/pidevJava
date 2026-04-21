package tn.esprit.services;

import tn.esprit.entities.Questionnaire;
import tn.esprit.entities.RendezVous;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RendezVousService implements IGeneralService<RendezVous> {
    Connection cn;
    public RendezVousService() {
        cn = MyDatabase.getInstance().getCnx();
    }
    @Override
    public void ajouter(RendezVous rendezVous) throws SQLException {
        String sql = "insert into rendez_vous(date_don, status, questionnaire_id, entite_id) values(?,?,?,?)";
        PreparedStatement rdv = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        rdv.setTimestamp(1,Timestamp.valueOf(rendezVous.getDateDon()));
        rdv.setString(2,rendezVous.getStatus());
        rdv.setInt(3, rendezVous.getQuestionnaire_id());
        rdv.setInt(4, rendezVous.getEntite_id());
        System.out.println("executing insert...");
        rdv.executeUpdate();
        ResultSet generatedKeys = rdv.getGeneratedKeys();
        if (generatedKeys.next()) {
            rendezVous.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    public void supprimer(RendezVous rendezVous) throws SQLException {
        String rdv = "DELETE FROM rendez_vous WHERE id = ?";
        PreparedStatement pstRV = cn.prepareStatement(rdv);
        pstRV.setInt(1, rendezVous.getId());
        pstRV.executeUpdate();
    }

    public boolean supprimerForClient(int rdvId) throws SQLException {
        String sql = "UPDATE rendez_vous SET status = 'annulé' WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, rdvId);
        return pst.executeUpdate() > 0;
    }

    public boolean hasRendezVous(int clientId, int campagneId) throws SQLException {
        String sql = "SELECT rv.id FROM rendez_vous rv " +
                "JOIN questionnaire q ON rv.questionnaire_id = q.id " +
                "WHERE q.client_id = ? AND q.campagne_id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, clientId);
        pst.setInt(2, campagneId);
        ResultSet rs = pst.executeQuery();
        return rs.next();
    }

    public int chercher(RendezVous rendezVous) throws SQLException {
        String sql = "SELECT 1 FROM rendez_vous WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, rendezVous.getId());
        ResultSet rs = pst.executeQuery();
        if (rs.next()){
            System.out.println("ce rendez vous existe avec l'id "+rendezVous.getId());
        }else{
            System.out.println("ce rendez vous n'existe pas");
        }
        return rendezVous.getId();
    }

    @Override
    public void modifier(RendezVous rendezVous) throws SQLException {
        if(chercher(rendezVous)== rendezVous.getId()){
            String sql = "UPDATE rendez_vous SET date_don = ?, status = ?, entite_id = ? WHERE id=?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setTimestamp(1, Timestamp.valueOf(rendezVous.getDateDon()));
            pst.setString(2, rendezVous.getStatus());
            pst.setInt(3, rendezVous.getEntite_id());
            pst.setInt(4, rendezVous.getId());
            pst.executeUpdate();
        }
        else {
            System.out.println("ce rendez vous n'existe pas");
        }
    }

    @Override
    public List<RendezVous> recuperer() throws SQLException {
        String sql = "select * from rendez_vous";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<RendezVous> rendezvouet = new ArrayList<>();
        while(rs.next()){
            RendezVous rdv = new RendezVous(
                    rs.getInt("id"),
                    rs.getString("status"),
                    rs.getTimestamp("date_don").toLocalDateTime(),
                    rs.getInt("questionnaire_id"),
                    rs.getInt("entite_id")
            );
            rendezvouet.add(rdv);
        }

        return rendezvouet;
    }

}
