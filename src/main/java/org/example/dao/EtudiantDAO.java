package org.example.dao;

import org.example.model.Etudiant;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EtudiantDAO {

    private Connection connection;

    public EtudiantDAO() {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        this.connection = dbConnection.getConnection();
    }

    // Créer un étudiant
    public Long create(Etudiant etudiant) {
        String sql = "INSERT INTO etudiant (nom, prenom, date_naissance, CIN, sexe, " +
                "groupe_id, email, password, profile_picture, directeur_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, etudiant.getNom());
            stmt.setString(2, etudiant.getPrenom());
            stmt.setDate(3, Date.valueOf(etudiant.getDateNaissance()));
            stmt.setString(4, etudiant.getCin());
            stmt.setString(5, etudiant.getSexe().getValeur());
            stmt.setInt(6, etudiant.getGroupeId());
            stmt.setString(7, etudiant.getEmail());
            stmt.setString(8, etudiant.getPassword());
            stmt.setString(9, etudiant.getProfilePicture());
            stmt.setInt(10, etudiant.getDirecteurId());

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
    public Optional<Etudiant> findById(int id) {
        String sql = "SELECT * FROM etudiant WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEtudiant(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Trouver par CIN
    public Optional<Etudiant> findByCin(String cin) {
        String sql = "SELECT * FROM etudiant WHERE CIN = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cin);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEtudiant(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Trouver par email
    public Optional<Etudiant> findByEmail(String email) {
        String sql = "SELECT * FROM etudiant WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEtudiant(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Authentifier un étudiant
    public Optional<Etudiant> authenticate(String email, String password) {
        String sql = "SELECT * FROM etudiant WHERE email = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEtudiant(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Trouver tous les étudiants
    public List<Etudiant> findAll() {
        List<Etudiant> etudiants = new ArrayList<>();
        String sql = "SELECT * FROM etudiant ORDER BY nom, prenom";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                etudiants.add(mapResultSetToEtudiant(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return etudiants;
    }

    // Trouver par directeur
    public List<Etudiant> findByDirecteurId(int directeurId) {
        List<Etudiant> etudiants = new ArrayList<>();
        String sql = "SELECT * FROM etudiant WHERE directeur_id = ? ORDER BY nom, prenom";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, directeurId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                etudiants.add(mapResultSetToEtudiant(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return etudiants;
    }

    // Trouver par groupe
    public List<Etudiant> findByGroupeId(int groupeId) {
        List<Etudiant> etudiants = new ArrayList<>();
        String sql = "SELECT * FROM etudiant WHERE groupe_id = ? ORDER BY nom, prenom";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupeId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                etudiants.add(mapResultSetToEtudiant(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return etudiants;
    }

    // Trouver par sexe
    public List<Etudiant> findBySexe(Etudiant.Sexe sexe) {
        List<Etudiant> etudiants = new ArrayList<>();
        String sql = "SELECT * FROM etudiant WHERE sexe = ? ORDER BY nom, prenom";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sexe.getValeur());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                etudiants.add(mapResultSetToEtudiant(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return etudiants;
    }

    // Rechercher par nom ou prénom
    public List<Etudiant> searchByName(String keyword) {
        List<Etudiant> etudiants = new ArrayList<>();
        String sql = "SELECT * FROM etudiant WHERE nom LIKE ? OR prenom LIKE ? " +
                "OR CONCAT(prenom, ' ', nom) LIKE ? ORDER BY nom, prenom";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchTerm = "%" + keyword + "%";
            stmt.setString(1, searchTerm);
            stmt.setString(2, searchTerm);
            stmt.setString(3, searchTerm);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                etudiants.add(mapResultSetToEtudiant(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return etudiants;
    }

    // Compter tous les étudiants
    public int countAll() {
        String sql = "SELECT COUNT(*) as total FROM etudiant";

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
        String sql = "SELECT COUNT(*) as total FROM etudiant WHERE directeur_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, directeurId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Compter par groupe
    public int countByGroupeId(int groupeId) {
        String sql = "SELECT COUNT(*) as total FROM etudiant WHERE groupe_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupeId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Compter par sexe
    public int countBySexe(Etudiant.Sexe sexe) {
        String sql = "SELECT COUNT(*) as total FROM etudiant WHERE sexe = ?";

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

    // Mettre à jour un étudiant
    public boolean update(Etudiant etudiant) {
        String sql = "UPDATE etudiant SET nom = ?, prenom = ?, date_naissance = ?, " +
                "CIN = ?, sexe = ?, groupe_id = ?, email = ?, password = ?, " +
                "profile_picture = ?, directeur_id = ?, updated_at = NOW() " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, etudiant.getNom());
            stmt.setString(2, etudiant.getPrenom());
            stmt.setDate(3, Date.valueOf(etudiant.getDateNaissance()));
            stmt.setString(4, etudiant.getCin());
            stmt.setString(5, etudiant.getSexe().getValeur());
            stmt.setInt(6, etudiant.getGroupeId());
            stmt.setString(7, etudiant.getEmail());
            stmt.setString(8, etudiant.getPassword());
            stmt.setString(9, etudiant.getProfilePicture());
            stmt.setInt(10, etudiant.getDirecteurId());
            stmt.setInt(11, etudiant.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Supprimer un étudiant
    public boolean delete(int id) {
        String sql = "DELETE FROM etudiant WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Vérifier si CIN existe déjà
    public boolean cinExists(String cin) {
        String sql = "SELECT COUNT(*) as count FROM etudiant WHERE CIN = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cin);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Vérifier si email existe déjà
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) as count FROM etudiant WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Obtenir les statistiques par sexe pour un directeur
    public int[] getSexeStatsByDirecteur(int directeurId) {
        int[] stats = new int[2]; // [hommes, femmes]
        String sql = "SELECT sexe, COUNT(*) as count FROM etudiant WHERE directeur_id = ? GROUP BY sexe";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, directeurId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String sexe = rs.getString("sexe");
                int count = rs.getInt("count");

                if ("Homme".equals(sexe)) {
                    stats[0] = count;
                } else if ("Femme".equals(sexe)) {
                    stats[1] = count;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    // Méthode utilitaire pour mapper ResultSet à Etudiant
    private Etudiant mapResultSetToEtudiant(ResultSet rs) throws SQLException {
        Etudiant etudiant = new Etudiant();
        etudiant.setId(rs.getInt("id"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            etudiant.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            etudiant.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        etudiant.setNom(rs.getString("nom"));
        etudiant.setPrenom(rs.getString("prenom"));

        Date dateNaissance = rs.getDate("date_naissance");
        if (dateNaissance != null) {
            etudiant.setDateNaissance(dateNaissance.toLocalDate());
        }

        etudiant.setCin(rs.getString("CIN"));
        etudiant.setSexe(Etudiant.Sexe.fromString(rs.getString("sexe")));
        etudiant.setGroupeId(rs.getInt("groupe_id"));
        etudiant.setEmail(rs.getString("email"));
        etudiant.setPassword(rs.getString("password"));
        etudiant.setProfilePicture(rs.getString("profile_picture"));
        etudiant.setDirecteurId(rs.getInt("directeur_id"));

        return etudiant;
    }
}