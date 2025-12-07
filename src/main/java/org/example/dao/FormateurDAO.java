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
                "situation, CIN, date_recrutement, email, password, profile_picture, directeur_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            stmt.setString(11, formateur.getProfilePicture());
            stmt.setInt(12, formateur.getDirecteurId());

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
    public Optional<Formateur> findById(int id) {
        String sql = "SELECT * FROM formateurs WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToFormateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Trouver par matricule
    public Optional<Formateur> findByMatricule(String matricule) {
        String sql = "SELECT * FROM formateurs WHERE matricule = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, matricule);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToFormateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
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

    // Trouver par CIN
    public Optional<Formateur> findByCin(String cin) {
        String sql = "SELECT * FROM formateurs WHERE CIN = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cin);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToFormateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Trouver tous les formateurs
    public List<Formateur> findAll() {
        List<Formateur> formateurs = new ArrayList<>();
        String sql = "SELECT * FROM formateurs ORDER BY nom, prenom";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                formateurs.add(mapResultSetToFormateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return formateurs;
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

    // Trouver par sexe
    public List<Formateur> findBySexe(Formateur.Sexe sexe) {
        List<Formateur> formateurs = new ArrayList<>();
        String sql = "SELECT * FROM formateurs WHERE sexe = ? ORDER BY nom, prenom";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sexe.getValeur());

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
    public int countAll() {
        String sql = "SELECT COUNT(*) as total FROM formateurs";

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

    // Compter par sexe
    public int countBySexe(Formateur.Sexe sexe) {
        String sql = "SELECT COUNT(*) as total FROM formateurs WHERE sexe = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sexe.getValeur());

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
                "profile_picture = ?, directeur_id = ?, updated_at = NOW() " +
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
            stmt.setString(11, formateur.getProfilePicture());
            stmt.setInt(12, formateur.getDirecteurId());
            stmt.setInt(13, formateur.getId());

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

    // Rechercher par nom ou prénom
    public List<Formateur> searchByName(String keyword) {
        List<Formateur> formateurs = new ArrayList<>();
        String sql = "SELECT * FROM formateurs WHERE nom LIKE ? OR prenom LIKE ? " +
                "OR CONCAT(prenom, ' ', nom) LIKE ? ORDER BY nom, prenom";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                formateurs.add(mapResultSetToFormateur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return formateurs;
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
        formateur.setProfilePicture(rs.getString("profile_picture"));
        formateur.setDirecteurId(rs.getInt("directeur_id"));

        return formateur;
    }
}