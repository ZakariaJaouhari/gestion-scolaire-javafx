package org.example.dao;

import org.example.model.Directeur;
import org.example.util.DatabaseConnection; // ou org.example.config.DatabaseConnection

import java.sql.*;
import java.util.Optional;

public class DirecteurDAO {

    private Connection connection;

    public DirecteurDAO() {
        // CORRECTION ICI : Appeler getInstance() puis getConnection()
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        this.connection = dbConnection.getConnection();
    }

    // Créer un directeur
    public int create(Directeur directeur) {
        String sql = "INSERT INTO directeurs (nom_directeur, nom_ecole, email, password, annee, academie, direction) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, directeur.getNomDirecteur());
            stmt.setString(2, directeur.getNomEcole());
            stmt.setString(3, directeur.getEmail());
            stmt.setString(4, directeur.getPassword());
            stmt.setString(5, directeur.getAnnee());
            stmt.setString(6, directeur.getAcademie());
            stmt.setString(7, directeur.getDirection());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Trouver par ID
    public Optional<Directeur> findById(int id) {
        String sql = "SELECT * FROM directeurs WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToDirecteur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public String getNomEcoleByDirecteurId(int directeurId) {
        String sql = "SELECT nom_ecole FROM directeurs WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, directeurId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nom_ecole");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "École non trouvée";
    }

    // Trouver par email
    public Optional<Directeur> findByEmail(String email) {
        String sql = "SELECT * FROM directeurs WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToDirecteur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Vérifier les identifiants
    public Optional<Directeur> authenticate(String email, String password) {
        String sql = "SELECT * FROM directeurs WHERE email = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToDirecteur(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Mettre à jour un directeur
    public boolean update(Directeur directeur) {
        String sql = "UPDATE directeurs SET nom_directeur = ?, nom_ecole = ?, email = ?, password = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, directeur.getNomDirecteur());
            stmt.setString(2, directeur.getNomEcole());
            stmt.setString(3, directeur.getEmail());
            stmt.setString(4, directeur.getPassword());
            stmt.setInt(5, directeur.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Supprimer un directeur
    public boolean delete(int id) {
        String sql = "DELETE FROM directeurs WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Vérifier si l'email existe déjà
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) as count FROM directeurs WHERE email = ?";

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

    // Méthode utilitaire pour mapper ResultSet à Directeur
    private Directeur mapResultSetToDirecteur(ResultSet rs) throws SQLException {
        Directeur directeur = new Directeur();
        directeur.setId(rs.getInt("id"));
        directeur.setNomDirecteur(rs.getString("nom_directeur"));
        directeur.setNomEcole(rs.getString("nom_ecole"));
        directeur.setAcademie(rs.getString("academie"));
        directeur.setDirection(rs.getString("direction"));
        directeur.setEmail(rs.getString("email"));
        directeur.setPassword(rs.getString("password"));
        return directeur;
    }
}