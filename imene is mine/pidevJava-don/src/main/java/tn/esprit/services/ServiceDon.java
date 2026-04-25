package tn.esprit.services;

import tn.esprit.entities.Don;
import tn.esprit.tools.ComboItem;
import tn.esprit.tools.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDon {

    private Connection conn;

    public ServiceDon() {
        conn = DataSource.getInstance().getConn();
    }

    public void ajouter(Don d) throws SQLException {
        String req = "INSERT INTO don (id_client, id_entite, quantite, type_don) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, d.getId_client());
            pst.setInt(2, d.getId_entite());
            pst.setFloat(3, d.getQuantite());
            pst.setString(4, d.getType_don());
            pst.executeUpdate();
            System.out.println("✅ Donation successfully logged.");
        }
    }

    public List<Don> afficherAll() throws SQLException {
        List<Don> list = new ArrayList<>();
        String req = "SELECT * FROM don";
        try (Statement ste = conn.createStatement();
             ResultSet res = ste.executeQuery(req)) {
            while (res.next()) {
                list.add(new Don(
                        res.getInt("id"),
                        res.getInt("id_client"),
                        res.getInt("id_entite"),
                        res.getFloat("quantite"),
                        res.getString("type_don")
                ));
            }
        }
        return list;
    }

    public void modifier(Don d) throws SQLException {
        String req = "UPDATE don SET quantite = ?, type_don = ? WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setFloat(1, d.getQuantite());
            pst.setString(2, d.getType_don());
            pst.setInt(3, d.getId());
            pst.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM don WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    // --- COMBOBOX DATA FETCHERS ---

    public List<ComboItem> getClientComboItems() throws SQLException {
        List<ComboItem> items = new ArrayList<>();

        // THE FIX: JOINing the 'client' table with the 'user' table
        String req = "SELECT c.id, u.nom, u.prenom FROM client c JOIN user u ON c.id = u.id";

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while(rs.next()) {
                String fullName = rs.getString("nom").toUpperCase() + ", " + rs.getString("prenom");
                items.add(new ComboItem(rs.getInt("id"), fullName));
            }
        } catch (SQLException e) {
            System.err.println("Database join failed: " + e.getMessage());
            String fallbackReq = "SELECT id FROM client";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(fallbackReq)) {
                while(rs.next()) items.add(new ComboItem(rs.getInt("id"), "Client ID: " + rs.getInt("id")));
            }
        }
        return items;
    }

    public List<ComboItem> getEntiteComboItems() throws SQLException {
        List<ComboItem> items = new ArrayList<>();
        String req = "SELECT id, nom FROM entitecollecte";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while(rs.next()) {
                items.add(new ComboItem(rs.getInt("id"), rs.getString("nom")));
            }
        } catch (SQLException e) {
            String fallbackReq = "SELECT id FROM entitecollecte";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(fallbackReq)) {
                while(rs.next()) items.add(new ComboItem(rs.getInt("id"), "Center ID: " + rs.getInt("id")));
            }
        }
        return items;
    }
}