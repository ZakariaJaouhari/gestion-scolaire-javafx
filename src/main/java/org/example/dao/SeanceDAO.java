package org.example.dao;

import org.example.model.Seance;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SeanceDAO {

    private Connection connection;

    public SeanceDAO() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    // Créer une séance
    public Long create(Seance seance) {
        String sql = "INSERT INTO seances (module_id, groupe_id, formateur_id, directeur_id, date, heure_debut, heure_fin, salle) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, seance.getModuleId());
            stmt.setInt(2, seance.getGroupeId());
            stmt.setInt(3, seance.getFormateurId());
            stmt.setInt(4, seance.getDirecteurId());
            stmt.setDate(5, Date.valueOf(seance.getDate()));
            stmt.setTime(6, Time.valueOf(seance.getHeureDebut()));
            stmt.setTime(7, Time.valueOf(seance.getHeureFin()));
            stmt.setString(8, seance.getSalle());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) return generatedKeys.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Séances pour un directeur
    public List<Seance> getSeancesByDirecteur(int directeurId) {
        List<Seance> seances = new ArrayList<>();
        String sql = "SELECT * FROM seances WHERE directeur_id = ? ORDER BY date, heure_debut";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, directeurId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(mapResultSetToSeance(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    // Séances par formateur
    public List<Seance> getSeancesByFormateur(int formateurId) {
        List<Seance> seances = new ArrayList<>();
        String sql = "SELECT * FROM seances WHERE formateur_id = ? ORDER BY date, heure_debut";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, formateurId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(mapResultSetToSeance(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    // Séances par groupe
    public List<Seance> getSeancesByGroupe(int groupeId) {
        List<Seance> seances = new ArrayList<>();
        String sql = "SELECT * FROM seances WHERE groupe_id = ? ORDER BY date, heure_debut";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(mapResultSetToSeance(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }


    // Ajouter ces méthodes à votre classe SeanceDAO existante

    public List<Seance> getSeancesByFormateurAndWeek(int formateurId, LocalDate weekStart) {
        List<Seance> seances = new ArrayList<>();
        LocalDate weekEnd = weekStart.plusDays(6);

        String sql = "SELECT * FROM seances WHERE formateur_id = ? " +
                "AND date >= ? AND date <= ? " +
                "ORDER BY date, heure_debut";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, formateurId);
            stmt.setDate(2, Date.valueOf(weekStart));
            stmt.setDate(3, Date.valueOf(weekEnd));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(mapResultSetToSeance(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    public List<Seance> getSeancesByGroupeAndWeek(int groupeId, LocalDate weekStart) {
        List<Seance> seances = new ArrayList<>();
        LocalDate weekEnd = weekStart.plusDays(6);

        String sql = "SELECT * FROM seances WHERE groupe_id = ? " +
                "AND date >= ? AND date <= ? " +
                "ORDER BY date, heure_debut";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupeId);
            stmt.setDate(2, Date.valueOf(weekStart));
            stmt.setDate(3, Date.valueOf(weekEnd));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(mapResultSetToSeance(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    public List<Seance> getSeancesBySalleAndWeek(String salle, LocalDate weekStart) {
        List<Seance> seances = new ArrayList<>();
        LocalDate weekEnd = weekStart.plusDays(6);

        String sql = "SELECT * FROM seances WHERE salle = ? " +
                "AND date >= ? AND date <= ? " +
                "ORDER BY date, heure_debut";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, salle);
            stmt.setDate(2, Date.valueOf(weekStart));
            stmt.setDate(3, Date.valueOf(weekEnd));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seances.add(mapResultSetToSeance(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seances;
    }

    public boolean checkScheduleConflict(int seanceId, int groupeId, int formateurId,
                                         LocalDate date, LocalTime startTime, LocalTime endTime, String salle) {
        String sql = "SELECT COUNT(*) FROM seances WHERE id != ? AND " +
                "date = ? AND " +
                "(heure_debut < ? AND heure_fin > ?) AND " +  // Chevauchement
                "(groupe_id = ? OR formateur_id = ? OR salle = ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, seanceId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, Time.valueOf(endTime));
            stmt.setTime(4, Time.valueOf(startTime));
            stmt.setInt(5, groupeId);
            stmt.setInt(6, formateurId);
            stmt.setString(7, salle);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Seance seance) {
        String sql = "UPDATE seances SET module_id = ?, groupe_id = ?, formateur_id = ?, " +
                "date = ?, heure_debut = ?, heure_fin = ?, salle = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, seance.getModuleId());
            stmt.setInt(2, seance.getGroupeId());
            stmt.setInt(3, seance.getFormateurId());
            stmt.setDate(4, Date.valueOf(seance.getDate()));
            stmt.setTime(5, Time.valueOf(seance.getHeureDebut()));
            stmt.setTime(6, Time.valueOf(seance.getHeureFin()));
            stmt.setString(7, seance.getSalle());
            stmt.setInt(8, seance.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int seanceId) {
        String sql = "DELETE FROM seances WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, seanceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private Seance mapResultSetToSeance(ResultSet rs) throws SQLException {
        Seance s = new Seance();
        s.setId(rs.getInt("id"));
        s.setModuleId(rs.getInt("module_id"));
        s.setGroupeId(rs.getInt("groupe_id"));
        s.setFormateurId(rs.getInt("formateur_id"));
        s.setDirecteurId(rs.getInt("directeur_id"));
        s.setDate(rs.getDate("date").toLocalDate());
        s.setHeureDebut(rs.getTime("heure_debut").toLocalTime());
        s.setHeureFin(rs.getTime("heure_fin").toLocalTime());
        s.setSalle(rs.getString("salle"));
        return s;
    }
}
