package tn.esprit.services;

import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntiteCollecteService {
    Connection cn;

    public EntiteCollecteService() {
        cn = MyDatabase.getInstance().getCnx();
    }

    public EntiteDeCollecte getEntiteById(int id) throws SQLException {
        String sql = "SELECT * FROM entite_de_collecte WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return new EntiteDeCollecte(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("tel"),
                    rs.getString("type"),
                    rs.getString("adresse"),
                    rs.getString("ville")
            );
        }
        return null;
    }

    public List<EntiteDeCollecte> getByCampagneId(int campagneId) throws SQLException {
        // Assuming a many-to-many relationship table 'campagne_entite'
        String sql = "SELECT e.* FROM entite_de_collecte e JOIN campagne_entite ce ON e.id = ce.entite_id WHERE ce.campagne_id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, campagneId);
        ResultSet rs = pst.executeQuery();
        List<EntiteDeCollecte> entites = new ArrayList<>();
        while (rs.next()) {
            EntiteDeCollecte e = new EntiteDeCollecte(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("tel"),
                    rs.getString("type"),
                    rs.getString("adresse"),
                    rs.getString("ville")
            );
            entites.add(e);
        }
        return entites;
    }
}
