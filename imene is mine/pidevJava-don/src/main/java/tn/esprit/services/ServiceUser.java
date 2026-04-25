package tn.esprit.services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import tn.esprit.entities.User;

public class ServiceUser {
    private String url = "jdbc:mysql://localhost:3306/pidev";
    private String user = "root", pwd = "";
    private Connection conn;

    public ServiceUser() {
        try {
            conn = DriverManager.getConnection(url, user, pwd);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }

    public void ajouter(User u) throws SQLException {
        String req = "INSERT INTO user (nom, prenom, email, password, role) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, u.getNom()); pst.setString(2, u.getPrenom());
        pst.setString(3, u.getEmail()); pst.setString(4, u.getPassword());
        pst.setString(5, u.getRole());
        pst.executeUpdate();
    }

    public List<User> afficherAll() throws SQLException {
        List<User> list = new ArrayList<>();
        ResultSet res = conn.createStatement().executeQuery("SELECT * FROM user");
        while (res.next()) {
            list.add(new User(res.getInt("id"), res.getString("nom"), res.getString("prenom"),
                    res.getString("email"), res.getString("password"), res.getString("role")));
        }
        return list;
    }
}