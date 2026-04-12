package tn.esprit.services;
import tn.esprit.entities.Campagne;
import tn.esprit.entities.Client;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampagneService implements IGeneralService<Campagne> {
    Connection cn;
    public CampagneService() {
        cn = MyDatabase.getInstance().getCnx();
    }
    @Override
    public void ajouter(Campagne campagne) throws SQLException {

    }

    @Override
    public void supprimer(Campagne campagne) throws SQLException {

    }

    @Override
    public int chercher(Campagne campagne) throws SQLException {
        return 0;
    }

    @Override
    public void modifier(Campagne campagne) throws SQLException {

    }

    @Override
    public List<Campagne> recuperer() throws SQLException {
        return List.of();
    }

    public List<Campagne> recupererByClient(Client client) throws SQLException {
        String sql = " SELECT DISTINCT c.* FROM compagne c WHERE c.type_sang LIKE CONCAT('%', ?, '%') AND c.date_fin > CURRENT_DATE AND date_debut >= ?";
        PreparedStatement st = cn.prepareStatement(sql);
        st.setString(1, client.getTypeSang());
        java.sql.Date stMinStartDate = java.sql.Date.valueOf(client.getDernierDon().plusWeeks(3));
        st.setDate(2, stMinStartDate);
        ResultSet rs = st.executeQuery();
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
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
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
        }
        return null;
    }
}
