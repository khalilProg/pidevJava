package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.entities.Client;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientService implements IGeneralService<Client> {
    Connection cn;

    public ClientService() {
        cn = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Client c) throws SQLException {
        String sql = "INSERT INTO client(type_sang, dernier_don, user_id) VALUES(?,?,?)";
        PreparedStatement pst = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, c.getTypeSang());
        pst.setDate(2, Date.valueOf(c.getDernierDon()));
        pst.setInt(3, c.getUser().getId());
        pst.executeUpdate();

        ResultSet generatedKeys = pst.getGeneratedKeys();
        if (generatedKeys.next()) {
            c.setId(generatedKeys.getInt(1));
        }
        System.out.println("client ajouté avec id=" + c.getId());
    }

    @Override
    public void supprimer(Client c) {
        try {
            String sql = "DELETE FROM client WHERE id = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, c.getId());
            pst.executeUpdate();
            System.out.println("client supprimé avec id=" + c.getId());
        } catch (SQLException e) {
            System.out.println("Erreur suppression client: " + e.getMessage());
        }
    }

    @Override
    public int chercher(Client c) {
        try {
            String sql = "SELECT 1 FROM client WHERE id = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, c.getId());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                System.out.println("Ce client existe avec l'id " + c.getId());
            } else {
                System.out.println("Ce client n'existe pas");
            }
        } catch (SQLException e) {
            System.out.println("Erreur recherche client: " + e.getMessage());
        }
        return c.getId();
    }

    @Override
    public void modifier(Client c) {
        try {
            if (chercher(c) == c.getId()) {
                String sql = "UPDATE client SET type_sang=?, dernier_don=? WHERE id=?";
                PreparedStatement pst = cn.prepareStatement(sql);
                pst.setString(1, c.getTypeSang());
                pst.setDate(2, Date.valueOf(c.getDernierDon()));
                pst.setInt(3, c.getId());
                pst.executeUpdate();
                System.out.println("client modifié avec id=" + c.getId());
            } else {
                System.out.println("Ce client n'existe pas");
            }
        } catch (SQLException e) {
            System.out.println("Erreur modification client: " + e.getMessage());
        }
    }

    @Override
    public List<Client> recuperer() throws SQLException {
        String sql = "SELECT c.id, c.type_sang, c.dernier_don, c.user_id, " +
                "u.nom, u.prenom, u.email, u.password, u.role, u.telephone " +
                "FROM client c JOIN user u ON c.user_id = u.id";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<Client> clients = new ArrayList<>();
        while (rs.next()) {
            User u = new User(
                    rs.getInt("user_id"),
                    rs.getString("email"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("telephone")
            );
            Client cl = new Client(
                    rs.getInt("id"),
                    rs.getString("type_sang"),
                    rs.getDate("dernier_don").toLocalDate(),
                    u
            );
            clients.add(cl);
        }
        return clients;
    }

    public Client getByPhone(String phone) throws SQLException {
        String sql = "SELECT c.id, c.type_sang, c.dernier_don, c.user_id, " +
                "u.nom, u.prenom, u.email, u.password, u.role, u.telephone " +
                "FROM client c JOIN user u ON c.user_id = u.id WHERE u.telephone = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setString(1, phone);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            User u = new User(
                    rs.getInt("user_id"),
                    rs.getString("email"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("telephone")
            );
            return new Client(
                    rs.getInt("id"),
                    rs.getString("type_sang"),
                    rs.getDate("dernier_don").toLocalDate(),
                    u
            );
        }
        return null;
    }
}
