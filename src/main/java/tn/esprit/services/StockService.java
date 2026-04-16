package tn.esprit.services;

import tn.esprit.entities.Stock;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockService implements IGeneralService<Stock> {

    Connection cnx;

    public StockService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Stock s) throws SQLException {
        String sql = "INSERT INTO stock (type_orgid, type_org, type_sang, quantite, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, s.getTypeOrgid());
        ps.setString(2, s.getTypeOrg());
        ps.setString(3, s.getTypeSang());
        ps.setInt(4, s.getQuantite());
        ps.setTimestamp(5, s.getCreatedAt());
        ps.setTimestamp(6, s.getUpdatedAt()); // Null or Timestamp
        ps.executeUpdate();
        System.out.println("Stock ajouté avec succès!");
    }

    @Override
    public void modifier(Stock s) throws SQLException {
        String sql = "UPDATE stock SET type_orgid=?, type_org=?, type_sang=?, quantite=?, updated_at=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, s.getTypeOrgid());
        ps.setString(2, s.getTypeOrg());
        ps.setString(3, s.getTypeSang());
        ps.setInt(4, s.getQuantite());
        ps.setTimestamp(5, s.getUpdatedAt());
        ps.setInt(6, s.getId());
        ps.executeUpdate();
        System.out.println("Stock modifié avec succès!");
    }

    @Override
    public void supprimer(Stock s) throws SQLException {
        String sql = "DELETE FROM stock WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, s.getId());
        ps.executeUpdate();
        System.out.println("Stock supprimé!");
    }

    @Override
    public int chercher(Stock s) throws SQLException {
        return s.getId();
    }

    @Override
    public List<Stock> recuperer() throws SQLException {
        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT * FROM stock ORDER BY created_at DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Stock s = new Stock(
                    rs.getInt("id"),
                    rs.getInt("type_orgid"),
                    rs.getString("type_org"),
                    rs.getString("type_sang"),
                    rs.getInt("quantite"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at")
            );
            stocks.add(s);
        }
        return stocks;
    }
}
