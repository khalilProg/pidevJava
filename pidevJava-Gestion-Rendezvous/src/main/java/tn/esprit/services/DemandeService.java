package tn.esprit.services;
import tn.esprit.entities.Banque;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import tn.esprit.entities.Demande;

import java.sql.Connection;
import java.sql.SQLException;


public class DemandeService implements IGeneralService<Demande>{
    Connection cn;
    public DemandeService() {
        cn = MyDatabase.getInstance().getCnx();
    }
    @Override
    public void ajouter(Demande demande) throws SQLException {
        String sql = "INSERT INTO demande " +
            "(id_banque, type_sang, quantite, urgence, status, created_at, updated_at,client_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?,?)";

        PreparedStatement q = cn.prepareStatement(sql);

        q.setInt(1, demande.getIdBanque());
        q.setString(2, demande.getTypeSang());
        q.setInt(3, demande.getQuantite());
        q.setString(4, demande.getUrgence());
        q.setInt(8, demande.getClientId());
        // status par défaut si null
        if (demande.getStatus() == null) {
            q.setString(5, "EN_ATTENTE");
        } else {
            q.setString(5, demande.getStatus());
        }

        // dates
        q.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

        if (demande.getUpdatedAt() != null) {
            q.setTimestamp(7, Timestamp.valueOf(demande.getUpdatedAt()));
        } else {
            q.setTimestamp(7, null);
        }

        System.out.println("Insertion demande...");
        q.executeUpdate();
    }

    @Override
    public void supprimer(Demande demande) throws SQLException {
        // 1️⃣ Supprimer les transferts liés (si tu as une table transfert avec demande_id)
        String sqlTransfert = "DELETE FROM transfert WHERE demande_id = ?";
        PreparedStatement pstTransfert = cn.prepareStatement(sqlTransfert);
        pstTransfert.setInt(1, demande.getId());
        pstTransfert.executeUpdate();

        // 2️⃣ Supprimer la demande
        String sqlDemande = "DELETE FROM demande WHERE id = ?";
        PreparedStatement pstDemande = cn.prepareStatement(sqlDemande);
        pstDemande.setInt(1, demande.getId());
        pstDemande.executeUpdate();

        System.out.println("Demande et ses transferts supprimés avec succès !");
    }

    @Override
    public int chercher(Demande demande) throws SQLException {
        String sql = "SELECT 1 FROM demande WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, demande.getId());
        ResultSet rs = pst.executeQuery();
        if (rs.next()){
            System.out.println("ce demande existe avec l'id "+demande.getId());
        }else{
            System.out.println("ce demande n'existe pas");
        }
        return demande.getId();
    }

    @Override
    public void modifier(Demande d) throws SQLException {
        if (chercher(d) == d.getId()) {
            String sql = "UPDATE demande SET id_banque = ?, client_id = ?, quantite = ?, type_sang = ?, urgence = ?, status = ?, updated_at = ? WHERE id = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, d.getIdBanque());
            pst.setInt(2, d.getClientId());
            pst.setInt(3, d.getQuantite());
            pst.setString(4, d.getTypeSang());
            pst.setString(5, d.getUrgence());
            pst.setString(6, d.getStatus());
            pst.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now())); // mise à jour du timestamp
            pst.setInt(8, d.getId());
            pst.executeUpdate();
            System.out.println("Demande modifiée avec succès !");
        } else {
            System.out.println("Demande n'existe pas avec l'id " + d.getId());
        }
    }

    @Override
    public List<Demande> recuperer() throws SQLException {
        String sql = "SELECT * FROM demande";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<Demande> demandes = new ArrayList<>();

        while (rs.next()) {
            Demande d = new Demande(
                rs.getInt("id"),
                rs.getInt("id_banque"),
                rs.getInt("client_id"),
                rs.getInt("quantite"),
                rs.getString("type_sang"),
                rs.getString("urgence"),
                rs.getString("status"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
            );
            demandes.add(d);
        }

        return demandes;
    }
}
