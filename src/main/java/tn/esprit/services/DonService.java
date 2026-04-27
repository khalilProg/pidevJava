package tn.esprit.services;

import tn.esprit.tools.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DonService {
    private final Connection cn;

    public DonService() {
        cn = MyDatabase.getInstance().getCnx();
    }

    public int countDonByClient(int clientId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM don WHERE id_client = ?";
        try (PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setInt(1, clientId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }
}
