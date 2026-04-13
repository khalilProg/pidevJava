package tn.esprit.services;

import tn.esprit.entities.Client;
import tn.esprit.entities.User;
import tn.esprit.tools.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientService {
    Connection cn;
    public ClientService() {
        cn = MyDatabase.getInstance().getCnx();
    }
    public Client getByPhone(String phone) throws SQLException {
        String sql = "SELECT c.id as client_id, c.type_sang, c.dernier_don, u.id as user_id, u.nom, u.prenom, u.telephone " +
                "FROM client c " +
                "JOIN user u ON c.user_id = u.id " +
                "WHERE u.telephone = ?";

        PreparedStatement st = cn.prepareStatement(sql);
        st.setString(1, phone);
        ResultSet rs = st.executeQuery();

        if (rs.next()) {
            Client client = new Client();
            client.setId(rs.getInt("client_id"));
            client.setTypeSang(rs.getString("type_sang"));
            client.setDernierDon(rs.getDate("dernier_don").toLocalDate());

            // populate the user object
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setNom(rs.getString("nom"));
            user.setPrenom(rs.getString("prenom"));
            user.setTel(rs.getString("telephone"));

            client.setUser(user);
            return client;
        }

        return null; // not found
    }
}
