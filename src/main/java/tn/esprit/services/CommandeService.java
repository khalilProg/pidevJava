package tn.esprit.services;

import tn.esprit.entities.Commande;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeService implements IGeneralService<Commande> {
    private final Connection cnx;

    public CommandeService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Commande c) throws SQLException {
        ajouterAndReturnId(c);
    }

    public int ajouterAndReturnId(Commande c) throws SQLException {
        String sql = "INSERT INTO commande (banque_id, client_id, stock_id, reference, quantite, priorite, type_sang, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, c.getBanqueId());
        ps.setInt(2, c.getClientId());
        ps.setInt(3, c.getStockId());
        ps.setInt(4, c.getReference());
        ps.setInt(5, c.getQuantite());
        ps.setString(6, c.getPriorite());
        ps.setString(7, c.getTypeSang());
        ps.setString(8, c.getStatus());
        ps.executeUpdate();

        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            c.setId(generatedKeys.getInt(1));
        }
        System.out.println("Commande ajoutee avec succes!");
        return c.getId();
    }

    @Override
    public void supprimer(Commande c) throws SQLException {
        String sql = "DELETE FROM commande WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, c.getId());
        ps.executeUpdate();
        System.out.println("Commande supprimee!");
    }

    @Override
    public int chercher(Commande c) throws SQLException {
        return c.getId();
    }

    @Override
    public void modifier(Commande c) throws SQLException {
        String sql = "UPDATE commande SET banque_id=?, client_id=?, stock_id=?, reference=?, quantite=?, priorite=?, type_sang=?, status=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, c.getBanqueId());
        ps.setInt(2, c.getClientId());
        ps.setInt(3, c.getStockId());
        ps.setInt(4, c.getReference());
        ps.setInt(5, c.getQuantite());
        ps.setString(6, c.getPriorite());
        ps.setString(7, c.getTypeSang());
        ps.setString(8, c.getStatus());
        ps.setInt(9, c.getId());
        ps.executeUpdate();
        System.out.println("Commande modifiee!");
    }

    @Override
    public List<Commande> recuperer() throws SQLException {
        String sql = "SELECT * FROM commande";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        return mapCommandes(rs);
    }

    public List<Commande> recupererByClientId(int clientId) throws SQLException {
        String sql = "SELECT * FROM commande WHERE client_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, clientId);
        ResultSet rs = ps.executeQuery();
        return mapCommandes(rs);
    }

    private List<Commande> mapCommandes(ResultSet rs) throws SQLException {
        List<Commande> commandes = new ArrayList<>();
        while (rs.next()) {
            Commande c = new Commande(
                    rs.getInt("id"),
                    rs.getInt("banque_id"),
                    rs.getInt("client_id"),
                    rs.getInt("stock_id"),
                    rs.getInt("reference"),
                    rs.getInt("quantite"),
                    rs.getString("priorite"),
                    rs.getString("type_sang"),
                    rs.getString("status")
            );
            commandes.add(c);
        }
        return commandes;
    }
}
