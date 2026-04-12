package tn.esprit.services;

import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.tools.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EntiteCollecteService {
    Connection cn;
    public EntiteCollecteService() {
        cn = MyDatabase.getInstance().getCnx();

    }

    public List<EntiteDeCollecte> getByCampagneId(int campagneId) throws SQLException {

        String sql = "SELECT e.* FROM entite_collecte e JOIN compagne_entite_collecte ce ON e.id = ce.entite_collecte_id WHERE ce.compagne_id = ?";
        PreparedStatement st = cn.prepareStatement(sql);
        st.setInt(1, campagneId);
        ResultSet rs = st.executeQuery();

        List<EntiteDeCollecte> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new EntiteDeCollecte(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("adresse"),
                    rs.getString("telephone"),
                    rs.getString("type"),
                    rs.getString("ville")

            ));
        }
        return list;
    }

    public EntiteDeCollecte getEntiteById(int id) throws SQLException {
        String sql = "SELECT * FROM entite_collecte WHERE id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new EntiteDeCollecte(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("type"),
                        rs.getString("ville")
                );
            }
        }
        return null;
    }
}
