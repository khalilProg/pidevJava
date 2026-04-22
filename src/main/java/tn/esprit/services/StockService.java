package tn.esprit.services;

import tn.esprit.entities.Stock;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockService {
    private Connection cn;

    public StockService() {
        cn = MyDatabase.getInstance().getCnx();
    }

    public int getAvailableQuantity(String typeSang) throws SQLException {
        String sql = "SELECT SUM(quantite) as total FROM stock WHERE type_sang = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setString(1, typeSang);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }

    public List<Stock> recuperer() throws SQLException {
        String sql = "SELECT * FROM stock";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<Stock> list = new ArrayList<>();
        while (rs.next()) {
            Stock s = new Stock();
            s.setId(rs.getInt("id"));
            s.setQuantite(rs.getInt("quantite"));
            s.setTypeSang(rs.getString("type_sang"));
            s.setTypeOrg(rs.getString("type_org"));
            s.setTypeOrgId(rs.getInt("type_org_id"));
            list.add(s);
        }
        return list;
    }

    public java.util.Map<String, Integer> getStockStats() throws SQLException {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        String sql = "SELECT type_sang, SUM(quantite) as total FROM stock GROUP BY type_sang";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            stats.put(rs.getString("type_sang"), rs.getInt("total"));
        }
        return stats;
    }

    public int getTotalQuantity() throws SQLException {
        String sql = "SELECT SUM(quantite) FROM stock";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) return rs.getInt(1);
        return 0;
    }
}
