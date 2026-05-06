package tn.esprit.services;

import tn.esprit.entities.Stock;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
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

    public int getAvailableQuantity(String typeSang) {
        int total = 0;
        try {
            String sql = "SELECT SUM(quantite) as total FROM stock WHERE type_sang=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, typeSang);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public java.util.Map<String, Integer> getStockStats() throws SQLException {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        String sql = "SELECT type_sang, SUM(quantite) as total FROM stock GROUP BY type_sang";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            stats.put(rs.getString("type_sang"), rs.getInt("total"));
        }
        return stats;
    }

    public int getTotalQuantity() throws SQLException {
        String sql = "SELECT SUM(quantite) FROM stock";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) return rs.getInt(1);
        return 0;
    }
    public Stock getStockByOrg(int orgId, String orgType, String bloodType) throws SQLException {
        String sql = "SELECT * FROM stock WHERE type_orgid=? AND LOWER(type_org)=LOWER(?) AND type_sang=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, orgId);
        ps.setString(2, orgType);
        ps.setString(3, bloodType);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Stock(
                rs.getInt("id"),
                rs.getInt("type_orgid"),
                rs.getString("type_org"),
                rs.getString("type_sang"),
                rs.getInt("quantite"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
            );
        }
        return null;
    }

    public int getAvailableQuantityForOrg(int orgId, String orgType, String bloodType) throws SQLException {
        String sql = "SELECT SUM(quantite) FROM stock WHERE type_orgid=? AND LOWER(type_org)=LOWER(?) AND type_sang=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, orgId);
        ps.setString(2, orgType);
        ps.setString(3, bloodType);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public Stock findBestCompatibleStockForBank(int banqueId, String patientBloodType, int requiredQuantity)
            throws SQLException {
        for (String compatibleType : BloodCompatibilityService.compatibleDonorTypesFor(patientBloodType)) {
            Stock stock = getStockByOrg(banqueId, "banque", compatibleType);
            if (stock != null && stock.getQuantite() >= requiredQuantity) {
                return stock;
            }
        }
        return null;
    }

    public Stock findBestCompatibleStockForAnyBank(String patientBloodType, int requiredQuantity,
                                                   Collection<Integer> banqueIds) throws SQLException {
        if (banqueIds == null || banqueIds.isEmpty()) {
            return null;
        }

        for (String compatibleType : BloodCompatibilityService.compatibleDonorTypesFor(patientBloodType)) {
            for (Integer banqueId : banqueIds) {
                if (banqueId == null) {
                    continue;
                }
                Stock stock = getStockByOrg(banqueId, "banque", compatibleType);
                if (stock != null && stock.getQuantite() >= requiredQuantity) {
                    return stock;
                }
            }
        }
        return null;
    }

}
