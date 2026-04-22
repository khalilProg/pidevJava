package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IGeneralService<User> {
    Connection cn;

    public UserService() {
        cn = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String sql = "INSERT INTO user(nom, prenom, email, password, role, telephone) VALUES(?,?,?,?,?,?)";
        PreparedStatement pst = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, user.getNom());
        pst.setString(2, user.getPrenom());
        pst.setString(3, user.getEmail());
        pst.setString(4, user.getPassword());
        pst.setString(5, user.getRole());
        pst.setString(6, user.getTel());
        pst.executeUpdate();

        ResultSet generatedKeys = pst.getGeneratedKeys();
        if (generatedKeys.next()) {
            user.setId(generatedKeys.getInt(1));
        }
        System.out.println("User ajouté avec id=" + user.getId());
    }

    @Override
    public void supprimer(User user) {
        try {
            // Delete dependent client rows first
            String sqlClient = "DELETE FROM client WHERE user_id = ?";
            PreparedStatement pstClient = cn.prepareStatement(sqlClient);
            pstClient.setInt(1, user.getId());
            pstClient.executeUpdate();

            // Delete dependent banque rows
            String sqlBanque = "DELETE FROM banque WHERE user_id = ?";
            PreparedStatement pstBanque = cn.prepareStatement(sqlBanque);
            pstBanque.setInt(1, user.getId());
            pstBanque.executeUpdate();

            String sql = "DELETE FROM user WHERE id = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, user.getId());
            pst.executeUpdate();
            System.out.println("User supprimé avec id=" + user.getId());
        } catch (SQLException e) {
            System.out.println("Erreur suppression user: " + e.getMessage());
        }
    }

    @Override
    public int chercher(User user) {
        try {
            String sql = "SELECT 1 FROM user WHERE id = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, user.getId());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                System.out.println("Ce user existe avec l'id " + user.getId());
            } else {
                System.out.println("Ce user n'existe pas");
            }
        } catch (SQLException e) {
            System.out.println("Erreur recherche user: " + e.getMessage());
        }
        return user.getId();
    }

    @Override
    public void modifier(User user) {
        try {
            if (chercher(user) == user.getId()) {
                String sql = "UPDATE user SET nom=?, prenom=?, email=?, password=?, role=?, telephone=? WHERE id=?";
                PreparedStatement pst = cn.prepareStatement(sql);
                pst.setString(1, user.getNom());
                pst.setString(2, user.getPrenom());
                pst.setString(3, user.getEmail());
                pst.setString(4, user.getPassword());
                pst.setString(5, user.getRole());
                pst.setString(6, user.getTel());
                pst.setInt(7, user.getId());
                pst.executeUpdate();
                System.out.println("User modifié avec id=" + user.getId());
            } else {
                System.out.println("Ce user n'existe pas");
            }
        } catch (SQLException e) {
            System.out.println("Erreur modification user: " + e.getMessage());
        }
    }

    @Override
    public List<User> recuperer() throws SQLException {
        String sql = "SELECT * FROM user";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            User u = new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("telephone")
            );
            users.add(u);
        }
        return users;
    }

    public int getTotalUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM user";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) return rs.getInt(1);
        return 0;
    }
}
