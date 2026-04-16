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
        PreparedStatement rdv = cn.prepareStatement(sql);
        rdv.setTimestamp(1,Timestamp.valueOf(rendezVous.getDateDon().plusHours(1)));
        rdv.setString(2,rendezVous.getStatus());
        rdv.setInt(3,rendezVous.getQuestionnaire_id());
        rdv.setInt(4,rendezVous.getEntite_id());
        System.out.println("executing insert...");
        rdv.executeUpdate();
    }

    @Override
    public void supprimer(RendezVous rendezVous) throws SQLException {
        String rdv = "DELETE FROM rendez_vous WHERE id = ?";
        PreparedStatement pstRV = cn.prepareStatement(rdv);
        pstRV.setInt(1, rendezVous.getId());
        pstRV.executeUpdate();
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
            String sql = "UPDATE rendez_vous SET date_don = ?, status = ? WHERE id=?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setTimestamp(1, Timestamp.valueOf(rendezVous.getDateDon().plusHours(1)));
            pst.setString(2, rendezVous.getStatus());
            pst.setInt(3, rendezVous.getId());
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

    public boolean hasRendezVous(int clientId, int campagneId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM rendez_vous r JOIN questionnaire q ON r.questionnaire_id = q.id WHERE q.client_id = ? AND q.campagne_id = ?";
        PreparedStatement st = cn.prepareStatement(sql);
        st.setInt(1, clientId);
        st.setInt(2, campagneId);
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            return rs.getInt("total") > 0;
        }
        return false;
    }


    public boolean supprimerForClient(int rendezVousId) throws SQLException {
        String sql = "UPDATE rendez_vous SET status = 'annulé' WHERE id = ?";
        PreparedStatement st = cn.prepareStatement(sql);
        st.setInt(1, rendezVousId);
        int updated = st.executeUpdate();
        return updated > 0;
    }


}
