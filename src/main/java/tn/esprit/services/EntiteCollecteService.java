package tn.esprit.services;

import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntiteCollecteService implements IGeneralService<EntiteDeCollecte> {

    Connection cn;

    public EntiteCollecteService() {
        cn = MyDatabase.getInstance().getCnx();
    }


    @Override
    public void ajouter(EntiteDeCollecte e) throws SQLException {
        verifierUnicite(e, true);

        String sql = "INSERT INTO entite_collecte (nom, telephone, type, adresse, ville) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cn.prepareStatement(sql);

        ps.setString(1, e.getNom());
        ps.setString(2, e.getTel());
        ps.setString(3, e.getType());
        ps.setString(4, e.getAdresse());
        ps.setString(5, e.getVille());

        ps.executeUpdate();
    }

    @Override
    public void supprimer(EntiteDeCollecte e) {
        try {
            // Supprimer d'abord dans la table de liaison au cas où !
            String sqlRelation = "DELETE FROM compagne_entite_collecte WHERE entite_collecte_id = ?";
            PreparedStatement psRelation = cn.prepareStatement(sqlRelation);
            psRelation.setInt(1, e.getId());
            psRelation.executeUpdate();

            // Supprimer l'entité
            String sql = "DELETE FROM entite_collecte WHERE id = ?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Erreur lors de la suppression de l'entité : " + ex.getMessage());
        }
    }

    @Override
    public int chercher(EntiteDeCollecte e) {
        return -1; // Ne plus utiliser par défaut, on utilise verifierUnicite
    }

    public void verifierUnicite(EntiteDeCollecte e, boolean isAjout) throws SQLException {
        // Vérifier l'unicité du NOM indépendamment
        String sqlNom = isAjout
                ? "SELECT id FROM entite_collecte WHERE nom=?"
                : "SELECT id FROM entite_collecte WHERE nom=? AND id != ?";
        PreparedStatement psNom = cn.prepareStatement(sqlNom);
        psNom.setString(1, e.getNom());
        if (!isAjout) psNom.setInt(2, e.getId());
        ResultSet rsNom = psNom.executeQuery();
        if (rsNom.next()) {
            throw new IllegalArgumentException("Une entité avec ce nom existe déjà !");
        }

        // Vérifier l'unicité du TELEPHONE indépendamment
        String sqlTel = isAjout
                ? "SELECT id FROM entite_collecte WHERE telephone=?"
                : "SELECT id FROM entite_collecte WHERE telephone=? AND id != ?";
        PreparedStatement psTel = cn.prepareStatement(sqlTel);
        psTel.setString(1, e.getTel());
        if (!isAjout) psTel.setInt(2, e.getId());
        ResultSet rsTel = psTel.executeQuery();
        if (rsTel.next()) {
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé par une autre entité !");
        }
    }

    public List<EntiteDeCollecte> rechercherParNom(String nom) {
        List<EntiteDeCollecte> list = new ArrayList<>();

        try {
            String sql = "SELECT * FROM entite_collecte WHERE nom LIKE ?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, "%" + nom + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                EntiteDeCollecte e = new EntiteDeCollecte(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("telephone"),
                        rs.getString("type"),
                        rs.getString("adresse"),
                        rs.getString("ville")
                );
                list.add(e);
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        return list;
    }
    
    @Override
    public void modifier(EntiteDeCollecte e) {
        try {
            verifierUnicite(e, false);

            String sql = "UPDATE entite_collecte SET nom=?, telephone=?, type=?, adresse=?, ville=? WHERE id=?";
            PreparedStatement ps = cn.prepareStatement(sql);

            ps.setString(1, e.getNom());
            ps.setString(2, e.getTel());
            ps.setString(3, e.getType());
            ps.setString(4, e.getAdresse());
            ps.setString(5, e.getVille());
            ps.setInt(6, e.getId());

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException("Erreur base de données : " + ex.getMessage());
        }
    }

    @Override
    public List<EntiteDeCollecte> recuperer() throws SQLException {
        String sql = "SELECT * FROM entite_collecte";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<EntiteDeCollecte> list = new ArrayList<>();

        while (rs.next()) {
            EntiteDeCollecte e = new EntiteDeCollecte(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("telephone"),
                    rs.getString("type"),
                    rs.getString("adresse"),
                    rs.getString("ville")
            );
            list.add(e);
        }

        return list;
    }
}