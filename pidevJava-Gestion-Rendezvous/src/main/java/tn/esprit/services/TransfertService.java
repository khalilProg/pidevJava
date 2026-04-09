package tn.esprit.services;

import tn.esprit.entities.Demande;
import tn.esprit.entities.Transfert;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransfertService implements IGeneralService<Transfert>{
    Connection cn;
    public TransfertService() {
        cn = MyDatabase.getInstance().getCnx();
    }
    @Override
    public void ajouter(Transfert t) throws SQLException {
        String sql = "INSERT INTO transfert " +
            "(demande_id, stock_id, from_org_id, from_org, to_org_id, to_org, date_envoie, date_reception, quantite, status, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        // 1️⃣ Liens et champs
        pst.setInt(1, t.getDemande() != null ? t.getDemande().getId() : 0);
        pst.setInt(2, t.getStock());
        pst.setInt(3, t.getFromOrgId());
        pst.setString(4, t.getFromOrg());
        pst.setInt(5, t.getToOrgId());
        pst.setString(6, t.getToOrg());

        // 2️⃣ Dates
        pst.setDate(7, t.getDateEnvoie() != null ? java.sql.Date.valueOf(t.getDateEnvoie()) : java.sql.Date.valueOf(LocalDate.now()));
        pst.setDate(8, t.getDateReception() != null ? java.sql.Date.valueOf(t.getDateReception()) : null);
        // 3️⃣ Autres champs
        pst.setInt(9, t.getQuantite());
        pst.setString(10, t.getStatus() != null ? t.getStatus() : "EN_ATTENTE");
        pst.setTimestamp(11, t.getCreatedAt() != null ? Timestamp.valueOf(t.getCreatedAt()) : Timestamp.valueOf(LocalDateTime.now()));
        pst.setTimestamp(12, t.getUpdatedAt() != null ? Timestamp.valueOf(t.getUpdatedAt()) : null);

        System.out.println("Insertion transfert...");
        pst.executeUpdate();

        // 4️⃣ Récupérer l'ID auto-incrémenté
        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            t.setId(rs.getInt(1));
        }
        System.out.println("Transfert ajouté avec succés");
    }

    @Override
    public void supprimer(Transfert transfert) throws SQLException {
        String sqlTransfert = "DELETE FROM transfert WHERE id = ?";
        PreparedStatement pstTransfert = cn.prepareStatement(sqlTransfert);
        pstTransfert.setInt(1, transfert.getId());
        pstTransfert.executeUpdate();
    }

    @Override
    public int chercher(Transfert transfert) throws SQLException {
        String sql = "SELECT 1 FROM transfert WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, transfert.getId());
        ResultSet rs = pst.executeQuery();
        if (rs.next()){
            System.out.println("ce transfert existe avec l'id "+transfert.getId());
        }else{
            System.out.println("ce transfert n'existe pas");
        }
        return transfert.getId();
    }

    @Override
    public void modifier(Transfert t) throws SQLException {
        if (chercher(t) == t.getId()) {
            String sql = "UPDATE transfert SET " +
                "demande_id = ?, stock_id = ?, from_org_id = ?, from_org = ?, " +
                "to_org_id = ?, to_org = ?, date_envoie = ?, date_reception = ?, " +
                "quantite = ?, status = ?, updated_at = ? " +
                "WHERE id = ?";

            PreparedStatement pst = cn.prepareStatement(sql);

            // 1️⃣ Liens et champs
            pst.setInt(1, t.getDemande() != null ? t.getDemande().getId() : 0);
            pst.setInt(2, t.getStock());
            pst.setInt(3, t.getFromOrgId());
            pst.setString(4, t.getFromOrg());
            pst.setInt(5, t.getToOrgId());
            pst.setString(6, t.getToOrg());

            // 2️⃣ Dates
            pst.setDate(7, t.getDateEnvoie() != null ? java.sql.Date.valueOf(t.getDateEnvoie()) : java.sql.Date.valueOf(LocalDate.now()));
            pst.setDate(8, t.getDateReception() != null ? java.sql.Date.valueOf(t.getDateReception()) : null);

            // 3️⃣ Autres champs
            pst.setInt(9, t.getQuantite());
            pst.setString(10, t.getStatus() != null ? t.getStatus() : "EN_ATTENTE");
            pst.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now())); // mise à jour du timestamp
            pst.setInt(12, t.getId());

            pst.executeUpdate();
            System.out.println("Transfert modifié avec succès !");
        } else {
            System.out.println("Transfert n'existe pas avec l'id " + t.getId());
        }
    }

    @Override
    public List<Transfert> recuperer() throws SQLException {
        String sql = "SELECT * FROM transfert";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<Transfert> transferts = new ArrayList<>();

        while (rs.next()) {
            // créer l'objet Demande pour la relation
            Demande demande = new Demande();
            demande.setId(rs.getInt("demande_id")); // on peut récupérer plus si nécessaire

            Transfert t = new Transfert(
                rs.getInt("id"),
                demande,                                   // Demande
                rs.getInt("stock_id"),                     // stock
                rs.getInt("from_org_id"),                  // fromOrgId
                rs.getInt("to_org_id"),                    // toOrgId
                rs.getInt("quantite"),                     // quantite
                rs.getDate("date_envoie") != null ? rs.getDate("date_envoie").toLocalDate() : null,      // dateEnvoie
                rs.getDate("date_reception") != null ? rs.getDate("date_reception").toLocalDate() : null, // dateReception
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null, // createdAt
                rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null, // updatedAt
                rs.getString("from_org"),                  // fromOrg
                rs.getString("to_org"),                    // toOrg
                rs.getString("status")                     // status
            );

            transferts.add(t);
        }

        return transferts;
    }
}
