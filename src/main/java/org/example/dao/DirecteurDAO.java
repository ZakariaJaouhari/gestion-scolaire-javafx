package org.example.dao;

import org.example.model.Directeur;
import org.example.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DirecteurDAO {

    // CREATE
    public boolean create(Directeur directeur) {
        String sql = "INSERT INTO directeurs (nom_ecole, nom_directeur, academie, direction, annee, email, password) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, directeur.getNomEcole());
            stmt.setString(2, directeur.getNomDirecteur());
            stmt.setString(3, directeur.getAcademie());
            stmt.setString(4, directeur.getDirection());
            stmt.setString(5, directeur.getAnnee());
            stmt.setString(6, directeur.getEmail());
            stmt.setString(7, directeur.getPassword());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        directeur.setId(rs.getInt(1));
                    }
                }
                System.out.println("✅ Directeur créé: " + directeur.getNomDirecteur());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur création directeur: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Ajouter dans DirecteurDAO.java
    public Directeur authenticate(String email, String password) {
        String sql = "SELECT * FROM directeurs WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDirecteur(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur d'authentification: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Ajouter aussi une méthode pour log des connexions
    public void logConnection(int directeurId, String ipAddress) {
        String sql = "INSERT INTO connexion_logs (directeur_id, ip_address, connexion_date) VALUES (?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, directeurId);
            stmt.setString(2, ipAddress);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur log connexion: " + e.getMessage());
        }
    }

    // READ - Tous les directeurs
    public List<Directeur> findAll() {
        List<Directeur> directeurs = new ArrayList<>();
        String sql = "SELECT * FROM directeurs ORDER BY nom_directeur";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                directeurs.add(mapResultSetToDirecteur(rs));
            }

            System.out.println("📋 " + directeurs.size() + " directeurs trouvés");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture directeurs: " + e.getMessage());
            e.printStackTrace();
        }
        return directeurs;
    }

    // READ - Par ID
    public Optional<Directeur> findById(int id) {
        String sql = "SELECT * FROM directeurs WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDirecteur(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche par ID: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // READ - Par email (pour login)
    public Optional<Directeur> findByEmail(String email) {
        String sql = "SELECT * FROM directeurs WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDirecteur(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche par email: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // UPDATE
    public boolean update(Directeur directeur) {
        String sql = "UPDATE directeurs SET nom_ecole = ?, nom_directeur = ?, academie = ?, " +
                "direction = ?, annee = ?, email = ?, password = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, directeur.getNomEcole());
            stmt.setString(2, directeur.getNomDirecteur());
            stmt.setString(3, directeur.getAcademie());
            stmt.setString(4, directeur.getDirection());
            stmt.setString(5, directeur.getAnnee());
            stmt.setString(6, directeur.getEmail());
            stmt.setString(7, directeur.getPassword());
            stmt.setInt(8, directeur.getId());

            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Directeur mis à jour: ID " + directeur.getId());
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour directeur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM directeurs WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                System.out.println("🗑️ Directeur supprimé: ID " + id);
            }
            return success;

        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression directeur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Vérifier si l'email existe déjà
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM directeurs WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Erreur vérification email: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // AJOUTER CETTE MÉTHODE MANQUANTE
    private Directeur mapResultSetToDirecteur(ResultSet rs) throws SQLException {
        Directeur directeur = new Directeur();
        directeur.setId(rs.getInt("id"));
        directeur.setNomEcole(rs.getString("nom_ecole"));
        directeur.setNomDirecteur(rs.getString("nom_directeur"));
        directeur.setAcademie(rs.getString("academie"));
        directeur.setDirection(rs.getString("direction"));
        directeur.setAnnee(rs.getString("annee"));
        directeur.setEmail(rs.getString("email"));
        directeur.setPassword(rs.getString("password"));

        return directeur;
    }

    // Méthode utilitaire pour compter les directeurs
    public int count() {
        String sql = "SELECT COUNT(*) FROM directeurs";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage directeurs: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}