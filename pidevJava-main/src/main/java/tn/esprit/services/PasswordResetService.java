package tn.esprit.services;

import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

public class PasswordResetService {
    private Connection cn;

    public PasswordResetService() {
        cn = MyDatabase.getInstance().getCnx();
    }

    public String createToken(int userId) throws SQLException {
        // Generate a 6 character pseudo-random code for easier typing since we are not clicking a link
        String rawToken = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        
        String sql = "INSERT INTO password_reset_token (token, expires_at, created_at, used, user_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = cn.prepareStatement(sql);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(1);
        
        pst.setString(1, rawToken);
        pst.setTimestamp(2, Timestamp.valueOf(expiresAt));
        pst.setTimestamp(3, Timestamp.valueOf(now));
        pst.setBoolean(4, false); // 0 representing false for tinyint
        pst.setInt(5, userId);
        
        pst.executeUpdate();
        return rawToken;
    }

    public int findValidToken(String token) throws SQLException {
        // Returns tokenId if valid, -1 if not
        String sql = "SELECT id FROM password_reset_token WHERE token = ? AND used = 0 AND expires_at > ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setString(1, token);
        pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1;
    }

    public void markTokenUsed(int tokenId) throws SQLException {
        String sql = "UPDATE password_reset_token SET used = 1 WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, tokenId);
        pst.executeUpdate();
    }
}
