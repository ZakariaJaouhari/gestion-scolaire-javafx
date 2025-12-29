package org.example.dao;

import org.example.model.Formateur;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FormateurDAO {

    private Connection connection;

    public FormateurDAO() {
        // Même correction ici
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        this.connection = dbConnection.getConnection();
    }

    // Créer un formateur
    public Long create(Formateur formateur) {
        String sql = "INSERT INTO formateurs (nom, prenom, matricule, sexe, date_naissance, " +
                "situation, CIN, date_recrutement, email, password, directeur_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, formateur.getNom());
            stmt.setString(2, formateur.getPrenom());
            stmt.setString(3, formateur.getMatricule());
            stmt.setString(4, formateur.getSexe().getValeur());
            stmt.setDate(5, Date.valueOf(formateur.getDateNaissance()));
            stmt.setString(6, formateur.getSituation().getValeur());
            stmt.setString(7, formateur.getCin());
            stmt.setDate(8, Date.valueOf(formateur.getDateRecrutement()));
            stmt.setString(9, formateur.getEmail());
            stmt.setString(10, formateur.getPassword());
            stmt.setInt(11, formateur.getDirecteurId());

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
    public Formateur findById(int id) {
        String sql = "SELECT * FROM formateurs WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Formateur formateur = new Formateur();
                formateur.setId(rs.getInt("id"));
                formateur.setMatricule(rs.getString("matricule"));
                formateur.setNom(rs.getString("nom"));
                formateur.setPrenom(rs.getString("prenom"));

                // Gestion du sexe (enum)
                String sexeStr = rs.getString("sexe");
                if (sexeStr != null && !sexeStr.trim().isEmpty()) {
                    try {
                        formateur.setSexe(Formateur.Sexe.fromString(sexeStr));
                    } catch (IllegalArgumentException e) {
                        // Valeur par défaut si le sexe n'est pas valide
                        formateur.setSexe(Formateur.Sexe.HOMME);
                    }
                }

                // Date de naissance
                Date dateNaissance = rs.getDate("date_naissance");
                if (dateNaissance != null) {
                    formateur.setDateNaissance(dateNaissance.toLocalDate());
                }

                // Situation (enum)
                String situationStr = rs.getString("situation");
                if (situationStr != null && !situationStr.trim().isEmpty()) {
                    try {
                        formateur.setSituation(Formateur.Situation.fromString(situationStr));
                    } catch (IllegalArgumentException e) {
                        // Valeur par défaut
                        formateur.setSituation(Formateur.Situation.CELIBATAIRE);
                    }
                }

                formateur.setCin(rs.getString("cin"));

                // Date de recrutement
                Date dateRecrutement = rs.getDate("date_recrutement");
                if (dateRecrutement != null) {
                    formateur.setDateRecrutement(dateRecrutement.toLocalDate());
                }

                formateur.setEmail(rs.getString("email"));
                formateur.setPassword(rs.getString("password"));
                formateur.setDirecteurId(rs.getInt("directeur_id"));

                // Dates de création/modification
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    formateur.setCreatedAt(createdAt.toLocalDateTime());
                }

                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    formateur.setUpdatedAt(updatedAt.toLocalDateTime());
                }

                return formateur;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // Trouver par email
    public Optional<Formateur> findByEmail(String email) {
        String sql = "SELECT * FROM formateurs WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToFormateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }



    // Trouver par directeur
    public List<Formateur> findByDirecteurId(int directeurId) {
        List<Formateur> formateurs = new ArrayList<>();
        String sql = "SELECT * FROM formateurs WHERE directeur_id = ? ORDER BY nom, prenom";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, directeurId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                formateurs.add(mapResultSetToFormateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return formateurs;
    }


    // Compter tous les formateurs
    public int countByDirecteurId(int directeurId) {
        String sql = "SELECT COUNT(*) as total FROM formateurs WHERE directeur_id = ?";

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

    // Mettre à jour un formateur
    public boolean update(Formateur formateur) {
        String sql = "UPDATE formateurs SET nom = ?, prenom = ?, matricule = ?, " +
                "sexe = ?, date_naissance = ?, situation = ?, CIN = ?, " +
                "date_recrutement = ?, email = ?, password = ?, " +
                "directeur_id = ?, updated_at = NOW() " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, formateur.getNom());
            stmt.setString(2, formateur.getPrenom());
            stmt.setString(3, formateur.getMatricule());
            stmt.setString(4, formateur.getSexe().getValeur());
            stmt.setDate(5, Date.valueOf(formateur.getDateNaissance()));
            stmt.setString(6, formateur.getSituation().getValeur());
            stmt.setString(7, formateur.getCin());
            stmt.setDate(8, Date.valueOf(formateur.getDateRecrutement()));
            stmt.setString(9, formateur.getEmail());
            stmt.setString(10, formateur.getPassword());
            stmt.setInt(11, formateur.getDirecteurId());
            stmt.setInt(12, formateur.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Supprimer un formateur
    public boolean delete(int id) {
        String sql = "DELETE FROM formateurs WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public int countModules(int formateurId) {
        String sql = "SELECT COUNT(*) as total FROM modules WHERE formateur_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, formateurId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countGroupes(int formateurId) {
        String sql = """
        SELECT COUNT(DISTINCT gngm.groupe_id) as total
        FROM new_groupe_new_module gngm
        JOIN modules m ON gngm.module_id = m.id
        WHERE m.formateur_id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, formateurId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countStagiaires(int formateurId) {
        String sql = """
        SELECT COUNT(DISTINCT e.id) as total
        FROM etudiant e
        JOIN groupes g ON e.groupe_id = g.id
        JOIN new_groupe_new_module gngm ON g.id = gngm.groupe_id
        JOIN modules m ON gngm.module_id = m.id
        WHERE m.formateur_id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, formateurId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Formateur findByModuleAndGroupe(int moduleId, int groupeId) {
        String sql = """
        SELECT f.* 
        FROM formateurs f
        INNER JOIN modules m ON f.id = m.formateur_id
        INNER JOIN new_groupe_new_module gm ON m.id = gm.module_id
        WHERE gm.module_id = ? AND gm.groupe_id = ?
        LIMIT 1
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, moduleId);
            stmt.setInt(2, groupeId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToFormateur(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    // Méthode utilitaire pour mapper ResultSet à Formateur
    private Formateur mapResultSetToFormateur(ResultSet rs) throws SQLException {
        Formateur formateur = new Formateur();
        formateur.setId(rs.getInt("id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            formateur.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            formateur.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        formateur.setNom(rs.getString("nom"));
        formateur.setPrenom(rs.getString("prenom"));
        formateur.setMatricule(rs.getString("matricule"));
        formateur.setSexe(Formateur.Sexe.fromString(rs.getString("sexe")));

        Date dateNaissance = rs.getDate("date_naissance");
        if (dateNaissance != null) {
            formateur.setDateNaissance(dateNaissance.toLocalDate());
        }

        formateur.setSituation(Formateur.Situation.fromString(rs.getString("situation")));
        formateur.setCin(rs.getString("CIN"));

        Date dateRecrutement = rs.getDate("date_recrutement");
        if (dateRecrutement != null) {
            formateur.setDateRecrutement(dateRecrutement.toLocalDate());
        }

        formateur.setEmail(rs.getString("email"));
        formateur.setPassword(rs.getString("password"));
        formateur.setDirecteurId(rs.getInt("directeur_id"));

        return formateur;
    }
}