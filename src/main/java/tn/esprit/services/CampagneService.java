package tn.esprit.services;

import tn.esprit.entities.Campagne;
import tn.esprit.entities.EntiteDeCollecte;
import tn.esprit.entities.client;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CampagneService implements IGeneralService<Campagne> {

    Connection cn;

    public CampagneService() {
        cn = MyDatabase.getInstance().getCnx();
    }

    public void verifierUnicite(Campagne c, boolean isAjout) throws SQLException {
        String sql = "SELECT id FROM compagne WHERE titre=?";
        if (!isAjout) {
            sql += " AND id != ?";
        }

        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, c.getTitre());

        if (!isAjout) {
            ps.setInt(2, c.getId());
        }

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            throw new IllegalArgumentException("Une campagne avec ce titre existe déjà !");
        }
    }

    /**
     * Convertit une liste de types de sang ["A+", "O-"] en format JSON pour la BD Symfony.
     * Résultat : "[\"A+\",\"O-\"]"
     */
    public static String typeSangVersJson(String typeSangAffichage) {
        if (typeSangAffichage == null || typeSangAffichage.isEmpty()) return "[]";
        String[] parts = typeSangAffichage.split(",\\s*");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < parts.length; i++) {
            sb.append("\"").append(parts[i].trim()).append("\"");
            if (i < parts.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Convertit les données JSON de la BD vers un format lisible
     * "[\"A+\",\"O-\"]" → "A+, O-"
     */
    public static String jsonVersTypeSang(String json) {
        if (json == null || json.isEmpty() || json.equals("[]")) return "";
        // Nettoyer le JSON : enlever [], " et espaces superflus
        String cleaned = json.replace("[", "").replace("]", "").replace("\"", "");
        String[] parts = cleaned.split(",\\s*");
        return String.join(", ", parts);
    }

    @Override
    public void ajouter(Campagne c) throws SQLException {
        verifierUnicite(c, true);

        cn.setAutoCommit(false);
        try {
            String sql = "INSERT INTO compagne (type_sang, titre, description, date_debut, date_fin, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, typeSangVersJson(c.getTypeSang()));
            ps.setString(2, c.getTitre());
            ps.setString(3, c.getDescription());
            ps.setDate(4, Date.valueOf(c.getDateDebut()));
            ps.setDate(5, Date.valueOf(c.getDateFin()));
            ps.setTimestamp(6, Timestamp.valueOf(c.getCreatedAt()));
            ps.setTimestamp(7, c.getUpdatedAt() != null ? Timestamp.valueOf(c.getUpdatedAt()) : null);

            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                c.setId(rs.getInt(1));
            }

            // Insérer la relation ManyToMany
            if (c.getEntiteDeCollectes() != null) {
                String sqlLiaison = "INSERT INTO compagne_entite_collecte (compagne_id, entite_collecte_id) VALUES (?, ?)";
                PreparedStatement psLiaison = cn.prepareStatement(sqlLiaison);
                for (EntiteDeCollecte e : c.getEntiteDeCollectes()) {
                    psLiaison.setInt(1, c.getId());
                    psLiaison.setInt(2, e.getId());
                    psLiaison.addBatch();
                }
                psLiaison.executeBatch();
            }

            cn.commit();
        } catch (SQLException ex) {
            cn.rollback();
            throw ex;
        } finally {
            cn.setAutoCommit(true);
        }
    }

    @Override
    public void supprimer(Campagne c) {
        try {
            cn.setAutoCommit(false);

            // 1. Supprimer les relations campagne-entité
            String sqlRel = "DELETE FROM compagne_entite_collecte WHERE compagne_id = ?";
            PreparedStatement psRel = cn.prepareStatement(sqlRel);
            psRel.setInt(1, c.getId());
            psRel.executeUpdate();

            // 2. Tenter de supprimer les tables qui dépendent de compagne
            // (rendez_vous, questionnaire, etc.)
            try {
                PreparedStatement psRv = cn.prepareStatement("DELETE FROM rendez_vous WHERE compagne_id = ?");
                psRv.setInt(1, c.getId());
                psRv.executeUpdate();
            } catch (SQLException ignored) {
                // La table n'a peut-être pas cette colonne
            }
            try {
                PreparedStatement psQ = cn.prepareStatement("DELETE FROM questionnaire WHERE compagne_id = ?");
                psQ.setInt(1, c.getId());
                psQ.executeUpdate();
            } catch (SQLException ignored) {
                // La table n'a peut-être pas cette colonne
            }

            // 3. Supprimer la campagne elle-même
            String sql = "DELETE FROM compagne WHERE id = ?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, c.getId());
            ps.executeUpdate();

            cn.commit();
        } catch (SQLException e) {
            try { cn.rollback(); } catch(Exception ex) {}
            throw new RuntimeException("Impossible de supprimer cette campagne. Elle est peut-être liée à des données existantes (rendez-vous, questionnaires, dons...). Supprimez d'abord ces données.");
        } finally {
            try { cn.setAutoCommit(true); } catch(Exception e) {}
        }
    }

    @Override
    public int chercher(Campagne c) {
        return -1;
    }

    public List<Campagne> rechercherParTitre(String titre) {
        List<Campagne> list = new ArrayList<>();

        try {
            String sql = "SELECT * FROM compagne WHERE titre LIKE ?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, "%" + titre + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapCampagne(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

    @Override
    public void modifier(Campagne c) {
        try {
            verifierUnicite(c, false);
            cn.setAutoCommit(false);

            String sql = "UPDATE compagne SET type_sang=?, titre=?, description=?, date_debut=?, date_fin=?, updated_at=? WHERE id=?";
            PreparedStatement ps = cn.prepareStatement(sql);

            ps.setString(1, typeSangVersJson(c.getTypeSang()));
            ps.setString(2, c.getTitre());
            ps.setString(3, c.getDescription());
            ps.setDate(4, Date.valueOf(c.getDateDebut()));
            ps.setDate(5, Date.valueOf(c.getDateFin()));
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(7, c.getId());

            ps.executeUpdate();

            // Re-créer les relations ManyToMany
            String sqlDel = "DELETE FROM compagne_entite_collecte WHERE compagne_id = ?";
            PreparedStatement psDel = cn.prepareStatement(sqlDel);
            psDel.setInt(1, c.getId());
            psDel.executeUpdate();

            if (c.getEntiteDeCollectes() != null) {
                String sqlLiaison = "INSERT INTO compagne_entite_collecte (compagne_id, entite_collecte_id) VALUES (?, ?)";
                PreparedStatement psLiaison = cn.prepareStatement(sqlLiaison);
                for (EntiteDeCollecte e : c.getEntiteDeCollectes()) {
                    psLiaison.setInt(1, c.getId());
                    psLiaison.setInt(2, e.getId());
                    psLiaison.addBatch();
                }
                psLiaison.executeBatch();
            }

            cn.commit();
        } catch (SQLException e) {
            try { cn.rollback(); } catch(Exception ex) {}
            throw new RuntimeException("Erreur de modification : " + e.getMessage());
        } finally {
            try { cn.setAutoCommit(true); } catch(Exception ex) {}
        }
    }

    @Override
    public List<Campagne> recuperer() throws SQLException {
        String sql = "SELECT * FROM compagne";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<Campagne> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapCampagne(rs));
        }

        return list;
    }

    public Campagne getCampagneById(int id) throws SQLException {
        String sql = "SELECT * FROM compagne WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return mapCampagne(rs);
        }
        return null;
    }

    public List<Campagne> recupererByClient(client c) throws SQLException {
        String sql = "SELECT * FROM compagne WHERE type_sang LIKE ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setString(1, "%" + c.getTypeSang() + "%");
        ResultSet rs = pst.executeQuery();
        List<Campagne> campagnes = new ArrayList<>();
        while(rs.next()){
            campagnes.add(mapCampagne(rs));
        }
        return campagnes;
    }

    private Campagne mapCampagne(ResultSet rs) throws SQLException {
        Campagne c = new Campagne(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getString("description"),
                rs.getDate("date_debut").toLocalDate(),
                rs.getDate("date_fin").toLocalDate(),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null,
                jsonVersTypeSang(rs.getString("type_sang"))
        );
        c.setEntiteDeCollectes(chargerEntites(c.getId()));
        return c;
    }

    private List<EntiteDeCollecte> chargerEntites(int campagneId) throws SQLException {
        List<EntiteDeCollecte> list = new ArrayList<>();
        String sql = "SELECT e.* FROM entite_collecte e JOIN compagne_entite_collecte cec ON e.id = cec.entite_collecte_id WHERE cec.compagne_id = ?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, campagneId);
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
        return list;
    }

    public java.util.Map<String, Integer> getCampagnesParMois() throws SQLException {
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT MONTHNAME(date_debut) as mois, COUNT(*) as nb FROM compagne GROUP BY MONTH(date_debut) ORDER BY MONTH(date_debut)";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            stats.put(rs.getString("mois"), rs.getInt("nb"));
        }
        return stats;
    }

    public List<String> getTop3MoinsCampagnes() throws SQLException {
        List<String> top3 = new ArrayList<>();
        String sql = "SELECT MONTHNAME(date_debut) as mois, COUNT(*) as nb FROM compagne GROUP BY MONTH(date_debut) ORDER BY nb DESC LIMIT 3";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            top3.add(rs.getString("mois") + " (" + rs.getInt("nb") + " campagnes)");
        }
        return top3;
    }
}
