package org.example.dao;

import org.example.model.Groupe;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupeDAO {

    private Connection connection;

    public GroupeDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // Créer un groupe
    public Long create(Groupe groupe) {
        String sql = "INSERT INTO groupes (matricule, niveau, directeur_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, groupe.getMatricule());
            stmt.setString(2, groupe.getNiveau().getValeur());
            stmt.setLong(3, groupe.getDirecteurId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Trouver par ID
    public Groupe findById(int id) {
        String sql = "SELECT * FROM groupes WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Groupe groupe = new Groupe();
                groupe.setId(rs.getInt("id"));
                groupe.setMatricule(rs.getString("matricule"));

                // Gestion du niveau (enum)
                String niveauStr = rs.getString("niveau");
                if (niveauStr != null && !niveauStr.trim().isEmpty()) {
                    try {
                        groupe.setNiveau(Groupe.Niveau.fromString(niveauStr));
                    } catch (IllegalArgumentException e) {
                        // Valeur par défaut
                        groupe.setNiveau(Groupe.Niveau.PREMIERE_ANNEE);
                    }
                }

                groupe.setDirecteurId(rs.getInt("directeur_id"));

                // Dates de création/modification
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    groupe.setCreatedAt(createdAt.toLocalDateTime());
                }

                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    groupe.setUpdatedAt(updatedAt.toLocalDateTime());
                }

                return groupe;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Trouver par matricule
    public Optional<Groupe> findByMatricule(String matricule) {
        String sql = "SELECT * FROM groupes WHERE matricule = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, matricule);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToGroupe(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Trouver tous les groupes
    public List<Groupe> findAll() {
        List<Groupe> groupes = new ArrayList<>();
        String sql = "SELECT * FROM groupes ORDER BY matricule";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                groupes.add(mapResultSetToGroupe(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupes;
    }

    // Trouver par directeur
    public List<Groupe> findByDirecteurId(int directeurId) {
        List<Groupe> groupes = new ArrayList<>();
        String sql = "SELECT * FROM groupes WHERE directeur_id = ? ORDER BY matricule";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, directeurId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                groupes.add(mapResultSetToGroupe(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupes;
    }

    public List<Groupe> findByFormateurId(int formateurId) {
        List<Groupe> groupes = new ArrayList<>();

        // Si vous avez une table de liaison groupe_modules
        String sql = """
        SELECT DISTINCT g.* FROM groupes g
        INNER JOIN new_groupe_new_module gm ON g.id = gm.groupe_id
        INNER JOIN modules m ON gm.module_id = m.id
        WHERE m.formateur_id = ?
        ORDER BY g.matricule
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, formateurId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                groupes.add(mapResultSetToGroupe(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupes;
    }

    // Méthode pour charger un groupe avec ses modules
    public Optional<Groupe> findByIdWithModules(int id) {
        String sql = "SELECT * FROM groupes WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Groupe groupe = mapResultSetToGroupe(rs);

                // Charger les modules via ModuleDAO
                ModuleDAO moduleDAO = new ModuleDAO();
                List<org.example.model.Module> modules = moduleDAO.findByGroupeId(id);
                groupe.setModules(modules);

                return Optional.of(groupe);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    // Trouver par niveau
    public List<Groupe> findByNiveau(Groupe.Niveau niveau) {
        List<Groupe> groupes = new ArrayList<>();
        String sql = "SELECT * FROM groupes WHERE niveau = ? ORDER BY matricule";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, niveau.getValeur());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                groupes.add(mapResultSetToGroupe(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupes;
    }

    // Trouver par directeur et niveau
    public List<Groupe> findByDirecteurIdAndNiveau(Long directeurId, Groupe.Niveau niveau) {
        List<Groupe> groupes = new ArrayList<>();
        String sql = "SELECT * FROM groupes WHERE directeur_id = ? AND niveau = ? ORDER BY matricule";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, directeurId);
            stmt.setString(2, niveau.getValeur());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                groupes.add(mapResultSetToGroupe(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupes;
    }

    // Compter tous les groupes
    public int countAll() {
        String sql = "SELECT COUNT(*) as total FROM groupes";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Compter par directeur
    public int countByDirecteurId(int directeurId) {
        String sql = "SELECT COUNT(*) as total FROM groupes WHERE directeur_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, directeurId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Compter par niveau
    public int countByNiveau(Groupe.Niveau niveau) {
        String sql = "SELECT COUNT(*) as total FROM groupes WHERE niveau = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, niveau.getValeur());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Mettre à jour un groupe
    public boolean update(Groupe groupe) {
        String sql = "UPDATE groupes SET matricule = ?, niveau = ?, directeur_id = ?, updated_at = NOW() WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, groupe.getMatricule());
            stmt.setString(2, groupe.getNiveau().getValeur());
            stmt.setLong(3, groupe.getDirecteurId());
            stmt.setLong(4, groupe.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Supprimer un groupe
    public boolean delete(int id) {
        String sql = "DELETE FROM groupes WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Vérifier si le matricule existe déjà
    public boolean matriculeExists(String matricule) {
        String sql = "SELECT COUNT(*) as count FROM groupes WHERE matricule = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, matricule);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Rechercher par matricule ou niveau
    public List<Groupe> search(String keyword) {
        List<Groupe> groupes = new ArrayList<>();
        String sql = "SELECT * FROM groupes WHERE matricule LIKE ? OR niveau LIKE ? ORDER BY matricule";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                groupes.add(mapResultSetToGroupe(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupes;
    }

    // Méthode utilitaire pour mapper ResultSet à Groupe
    private Groupe mapResultSetToGroupe(ResultSet rs) throws SQLException {
        Groupe groupe = new Groupe();
        groupe.setId(rs.getInt("id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            groupe.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            groupe.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        groupe.setMatricule(rs.getString("matricule"));
        groupe.setNiveau(Groupe.Niveau.fromString(rs.getString("niveau")));
        groupe.setDirecteurId(rs.getInt("directeur_id"));

        return groupe;
    }
}