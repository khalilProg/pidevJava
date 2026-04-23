package tn.esprit.services;

import tn.esprit.entities.Banque;
import tn.esprit.entities.User;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BanqueService implements IGeneralService<Banque> {
    Connection cn;

    public BanqueService() {
        cn = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Banque banque) throws SQLException {
        String sql = "INSERT INTO banque(user_id, nom, adresse, telephone) VALUES(?,?,?,?)";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, banque.getUser().getId());
        pst.setString(2, banque.getNom());
        pst.setString(3, banque.getAdresse());
        pst.setString(4, banque.getTelephone());
        pst.executeUpdate();
        banque.setId(banque.getUser().getId());
        System.out.println("Banque ajoutée avec user_id=" + banque.getId());
    }

    @Override
    public void supprimer(Banque banque) {
        try {
            String sql = "DELETE FROM banque WHERE user_id = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, banque.getId());
            pst.executeUpdate();
            System.out.println("Banque supprimée avec user_id=" + banque.getId());
        } catch (SQLException e) {
            System.out.println("Erreur suppression banque: " + e.getMessage());
        }
    }

    @Override
    public int chercher(Banque banque) {
        try {
            String sql = "SELECT 1 FROM banque WHERE user_id = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, banque.getId());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                System.out.println("Cette banque existe avec user_id=" + banque.getId());
            } else {
                System.out.println("Cette banque n'existe pas");
            }
        } catch (SQLException e) {
            System.out.println("Erreur recherche banque: " + e.getMessage());
        }
        return banque.getId();
    }

    @Override
    public void modifier(Banque banque) {
        try {
            if (chercher(banque) == banque.getId()) {
                String sql = "UPDATE banque SET nom=?, adresse=?, telephone=? WHERE user_id=?";
                PreparedStatement pst = cn.prepareStatement(sql);
                pst.setString(1, banque.getNom());
                pst.setString(2, banque.getAdresse());
                pst.setString(3, banque.getTelephone());
                pst.setInt(4, banque.getId());
                pst.executeUpdate();
                System.out.println("Banque modifiée avec user_id=" + banque.getId());
            } else {
                System.out.println("Cette banque n'existe pas");
            }
        } catch (SQLException e) {
            System.out.println("Erreur modification banque: " + e.getMessage());
        }
    }

    @Override
    public List<Banque> recuperer() throws SQLException {
        String sql = "SELECT id, nom, adresse, telephone FROM banque";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<Banque> banques = new ArrayList<>();
        while (rs.next()) {
            Banque b = new Banque(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("adresse"),
                    rs.getString("telephone"),
                    null
            );
            banques.add(b);
        }
        return banques;
    }

    public Banque getById(int id) throws SQLException {
        String sql = "SELECT id, nom, adresse, telephone FROM banque WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (!rs.next()) {
            return null;
        }
        return new Banque(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("adresse"),
                rs.getString("telephone"),
                null
        );
    }
}
