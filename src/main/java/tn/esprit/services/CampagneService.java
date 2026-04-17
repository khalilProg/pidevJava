package tn.esprit.services;

import tn.esprit.entities.Campagne;
import tn.esprit.entities.client;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampagneService {
    Connection cn;

    public CampagneService() {
        cn = MyDatabase.getInstance().getCnx();
    }

    public List<Campagne> recuperer() throws SQLException {
        String sql = "SELECT * FROM compagne";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<Campagne> campagnes = new ArrayList<>();
        while(rs.next()){
            Campagne c = new Campagne(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getDate("date_debut").toLocalDate(),
                    rs.getDate("date_fin").toLocalDate(),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                    rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null,
                    rs.getString("type_sang")
            );
            campagnes.add(c);
        }
        return campagnes;
    }

    public Campagne getCampagneById(int id) throws SQLException {
        String sql = "SELECT * FROM compagne WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return new Campagne(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getDate("date_debut").toLocalDate(),
                    rs.getDate("date_fin").toLocalDate(),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                    rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null,
                    rs.getString("type_sang")
            );
        }
        return null;
    }

    public List<Campagne> recupererByClient(client c) throws SQLException {
        // A naive implementation returning all suitable campaigns based on blood type
        String sql = "SELECT * FROM compagne WHERE type_sang LIKE ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setString(1, "%" + c.getTypeSang() + "%");
        ResultSet rs = pst.executeQuery();
        List<Campagne> campagnes = new ArrayList<>();
        while(rs.next()){
            Campagne camp = new Campagne(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("description"),
                    rs.getDate("date_debut").toLocalDate(),
                    rs.getDate("date_fin").toLocalDate(),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                    rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null,
                    rs.getString("type_sang")
            );
            campagnes.add(camp);
        }
        return campagnes;
    }
}
