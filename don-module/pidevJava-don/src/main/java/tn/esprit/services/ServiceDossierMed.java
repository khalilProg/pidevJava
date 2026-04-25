package tn.esprit.services;

import tn.esprit.entities.DossierMed;
import tn.esprit.tools.ComboItem;
import tn.esprit.tools.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDossierMed {

    private Connection conn;

    public ServiceDossierMed() {
        conn = DataSource.getInstance().getConn();
    }

    public void ajouter(DossierMed dm) throws SQLException {
        String req = "INSERT INTO dossier_med (taille, poid, temperature, sexe, contact_urgence, nom, prenom, age, type_sang, id_client, id_don) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setFloat(1, dm.getTaille());
            pst.setFloat(2, dm.getPoid());
            pst.setFloat(3, dm.getTemperature());
            pst.setString(4, dm.getSexe());
            pst.setInt(5, dm.getContact_urgence());
            pst.setString(6, dm.getNom());
            pst.setString(7, dm.getPrenom());
            pst.setInt(8, dm.getAge());
            pst.setString(9, dm.getType_sang());
            pst.setInt(10, dm.getId_client());
            pst.setInt(11, dm.getId_don());
            pst.executeUpdate();
        }
    }

    public List<DossierMed> afficherAll() throws SQLException {
        List<DossierMed> list = new ArrayList<>();
        String req = "SELECT * FROM dossier_med";
        try (Statement ste = conn.createStatement();
             ResultSet res = ste.executeQuery(req)) {
            while (res.next()) {
                list.add(new DossierMed(
                        res.getInt("id"), res.getFloat("taille"), res.getFloat("poid"),
                        res.getFloat("temperature"), res.getString("sexe"), res.getInt("contact_urgence"),
                        res.getString("nom"), res.getString("prenom"), res.getInt("age"),
                        res.getString("type_sang"), res.getInt("id_client"), res.getInt("id_don")
                ));
            }
        }
        return list;
    }

    public DossierMed getByClientId(int clientId) throws SQLException {
        String req = "SELECT * FROM dossier_med WHERE id_client = ?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setInt(1, clientId);
            try (ResultSet res = pst.executeQuery()) {
                if (res.next()) {
                    return new DossierMed(
                            res.getInt("id"), res.getFloat("taille"), res.getFloat("poid"),
                            res.getFloat("temperature"), res.getString("sexe"), res.getInt("contact_urgence"),
                            res.getString("nom"), res.getString("prenom"), res.getInt("age"),
                            res.getString("type_sang"), res.getInt("id_client"), res.getInt("id_don")
                    );
                }
            }
        }
        return null;
    }

    public void modifier(DossierMed dm) throws SQLException {
        String req = "UPDATE dossier_med SET nom=?, prenom=?, age=?, type_sang=?, " +
                "taille=?, poid=?, temperature=?, sexe=?, contact_urgence=? WHERE id=?";
        try (PreparedStatement pst = conn.prepareStatement(req)) {
            pst.setString(1, dm.getNom());
            pst.setString(2, dm.getPrenom());
            pst.setInt(3, dm.getAge());
            pst.setString(4, dm.getType_sang());
            pst.setFloat(5, dm.getTaille());
            pst.setFloat(6, dm.getPoid());
            pst.setFloat(7, dm.getTemperature());
            pst.setString(8, dm.getSexe());
            pst.setInt(9, dm.getContact_urgence());
            pst.setInt(10, dm.getId());
            pst.executeUpdate();
            System.out.println("✅ Database Updated: Clinical record synchronization complete.");
        }
    }

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM dossier_med WHERE id = ?";
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

    public List<ComboItem> getDonComboItems() throws SQLException {
        List<ComboItem> items = new ArrayList<>();
        String req = "SELECT id, type_don, quantite FROM don";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while(rs.next()) {
                String donDetails = "Don #" + rs.getInt("id") + " (" + rs.getString("type_don") + " - " + rs.getFloat("quantite") + "ml)";
                items.add(new ComboItem(rs.getInt("id"), donDetails));
            }
        }
        return items;
    }
}