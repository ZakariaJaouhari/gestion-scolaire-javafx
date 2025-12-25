package org.example.dao;

import org.example.model.Note;
import org.example.model.Etudiant;
import org.example.model.Module;
import org.example.model.Formateur;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NoteDAO {

    private Connection connection;

    public NoteDAO() {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        this.connection = dbConnection.getConnection();
    }

    // ========================= CREATE =========================

    /**
     * Créer une nouvelle note
     */
    public Long create(Note note) {
        String sql = "INSERT INTO notes (etudiant_id, module_id, formateur_id, note, " +
                "appreciation, observation, annee_scolaire, semestre, type_evaluation, " +
                "created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, note.getEtudiantId());
            stmt.setInt(2, note.getModuleId());
            stmt.setInt(3, note.getFormateurId());
            stmt.setDouble(4, note.getNote());
            stmt.setString(5, note.getAppreciation());
            stmt.setString(6, note.getObservation());
            stmt.setInt(7, note.getAnneeScolaire());
            stmt.setString(8, note.getSemestre());
            stmt.setString(9, note.getTypeEvaluation());

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

    /**
     * Créer plusieurs notes en une transaction
     */
    public boolean createAll(List<Note> notes) {
        String sql = "INSERT INTO notes (etudiant_id, module_id, formateur_id, note, " +
                "appreciation, observation, annee_scolaire, semestre, type_evaluation, " +
                "created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (Note note : notes) {
                    stmt.setInt(1, note.getEtudiantId());
                    stmt.setInt(2, note.getModuleId());
                    stmt.setInt(3, note.getFormateurId());
                    stmt.setDouble(4, note.getNote());
                    stmt.setString(5, note.getAppreciation());
                    stmt.setString(6, note.getObservation());
                    stmt.setInt(7, note.getAnneeScolaire());
                    stmt.setString(8, note.getSemestre());
                    stmt.setString(9, note.getTypeEvaluation());

                    stmt.addBatch();
                }

                int[] results = stmt.executeBatch();
                connection.commit();

                // Vérifier que toutes les insertions ont réussi
                for (int result : results) {
                    if (result == PreparedStatement.EXECUTE_FAILED) {
                        return false;
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // ========================= READ =========================

    /**
     * Trouver une note par son ID
     */
    public Optional<Note> findById(int id) {
        String sql = """
            SELECT n.*, 
                   e.id as e_id, e.nom as e_nom, e.prenom as e_prenom, e.cin as e_cin,
                   m.id as m_id, m.nom as m_nom, m.coefficient as m_coefficient,
                   f.id as f_id, f.nom as f_nom, f.prenom as f_prenom
            FROM notes n
            LEFT JOIN etudiants e ON n.etudiant_id = e.id
            LEFT JOIN modules m ON n.module_id = m.id
            LEFT JOIN formateurs f ON n.formateur_id = f.id
            WHERE n.id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Trouver toutes les notes d'un étudiant pour un semestre
     */
    public List<Note> findByEtudiantAndSemestre(int etudiantId, int anneeScolaire, String semestre) {
        List<Note> notes = new ArrayList<>();
        String sql = """
            SELECT n.*, 
                   m.id as m_id, m.nom as m_nom, m.coefficient as m_coefficient,
                   f.id as f_id, f.nom as f_nom, f.prenom as f_prenom
            FROM notes n
            LEFT JOIN modules m ON n.module_id = m.id
            LEFT JOIN formateurs f ON n.formateur_id = f.id
            WHERE n.etudiant_id = ? AND n.annee_scolaire = ? AND n.semestre = ?
            ORDER BY m.nom
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            stmt.setInt(2, anneeScolaire);
            stmt.setString(3, semestre);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    /**
     * Trouver toutes les notes d'un étudiant
     */
    public List<Note> findByEtudiant(int etudiantId) {
        List<Note> notes = new ArrayList<>();
        String sql = """
            SELECT n.*, m.id as m_id, m.nom as m_nom, m.coefficient as m_coefficient
            FROM notes n
            LEFT JOIN modules m ON n.module_id = m.id
            WHERE n.etudiant_id = ?
            ORDER BY n.annee_scolaire DESC, n.semestre, m.nom
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    /**
     * Trouver les notes d'un formateur pour un module donné
     */
    public List<Note> findByFormateurAndModule(int formateurId, int moduleId, int anneeScolaire, String semestre) {
        List<Note> notes = new ArrayList<>();
        String sql = """
            SELECT n.*, 
                   e.id as e_id, e.nom as e_nom, e.prenom as e_prenom, e.cin as e_cin,
                   g.id as g_id, g.matricule as g_matricule
            FROM notes n
            JOIN etudiants e ON n.etudiant_id = e.id
            LEFT JOIN groupes g ON e.groupe_id = g.id
            WHERE n.formateur_id = ? AND n.module_id = ? 
            AND n.annee_scolaire = ? AND n.semestre = ?
            ORDER BY e.nom, e.prenom
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, formateurId);
            stmt.setInt(2, moduleId);
            stmt.setInt(3, anneeScolaire);
            stmt.setString(4, semestre);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    /**
     * Trouver les notes d'un groupe pour un module donné
     */
    public List<Note> findByGroupeAndModule(int groupeId, int moduleId, int anneeScolaire, String semestre) {
        List<Note> notes = new ArrayList<>();
        String sql = """
            SELECT n.*, 
                   e.id as e_id, e.nom as e_nom, e.prenom as e_prenom,
                   f.id as f_id, f.nom as f_nom, f.prenom as f_prenom
            FROM notes n
            JOIN etudiants e ON n.etudiant_id = e.id
            LEFT JOIN formateurs f ON n.formateur_id = f.id
            WHERE e.groupe_id = ? AND n.module_id = ? 
            AND n.annee_scolaire = ? AND n.semestre = ?
            ORDER BY e.nom, e.prenom
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupeId);
            stmt.setInt(2, moduleId);
            stmt.setInt(3, anneeScolaire);
            stmt.setString(4, semestre);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    /**
     * Trouver toutes les notes d'un module
     */
    public List<Note> findByModule(int moduleId, int anneeScolaire, String semestre) {
        List<Note> notes = new ArrayList<>();
        String sql = """
            SELECT n.*, e.id as e_id, e.nom as e_nom, e.prenom as e_prenom
            FROM notes n
            JOIN etudiants e ON n.etudiant_id = e.id
            WHERE n.module_id = ? AND n.annee_scolaire = ? AND n.semestre = ?
            ORDER BY e.nom, e.prenom
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, moduleId);
            stmt.setInt(2, anneeScolaire);
            stmt.setString(3, semestre);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }




    /**
     * Vérifier si une note existe déjà pour un étudiant, module et type d'évaluation
     */
    public boolean existsNoteForEtudiantModule(int etudiantId, int moduleId, int anneeScolaire,
                                               String semestre, String typeEvaluation) {
        String sql = """
            SELECT COUNT(*) as count 
            FROM notes 
            WHERE etudiant_id = ? AND module_id = ? 
            AND annee_scolaire = ? AND semestre = ? AND type_evaluation = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            stmt.setInt(2, moduleId);
            stmt.setInt(3, anneeScolaire);
            stmt.setString(4, semestre);
            stmt.setString(5, typeEvaluation);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Trouver la note existante pour un étudiant, module et type d'évaluation
     */
    public Optional<Note> findExistingNote(int etudiantId, int moduleId, int anneeScolaire,
                                           String semestre, String typeEvaluation) {
        String sql = """
            SELECT * FROM notes 
            WHERE etudiant_id = ? AND module_id = ? 
            AND annee_scolaire = ? AND semestre = ? AND type_evaluation = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            stmt.setInt(2, moduleId);
            stmt.setInt(3, anneeScolaire);
            stmt.setString(4, semestre);
            stmt.setString(5, typeEvaluation);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Trouver toutes les notes
     */
    public List<Note> findAll() {
        List<Note> notes = new ArrayList<>();
        String sql = """
            SELECT n.*, 
                   e.id as e_id, e.nom as e_nom, e.prenom as e_prenom,
                   m.id as m_id, m.nom as m_nom,
                   f.id as f_id, f.nom as f_nom, f.prenom as f_prenom
            FROM notes n
            JOIN etudiants e ON n.etudiant_id = e.id
            JOIN modules m ON n.module_id = m.id
            LEFT JOIN formateurs f ON n.formateur_id = f.id
            ORDER BY n.annee_scolaire DESC, n.semestre, e.nom
            """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    // ========================= UPDATE =========================

    /**
     * Mettre à jour une note
     */
    public boolean update(Note note) {
        String sql = """
            UPDATE notes 
            SET note = ?, appreciation = ?, observation = ?, 
                updated_at = NOW() 
            WHERE id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setDouble(1, note.getNote());
            stmt.setString(2, note.getAppreciation());
            stmt.setString(3, note.getObservation());
            stmt.setInt(5, note.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Mettre à jour plusieurs notes
     */
    public boolean updateAll(List<Note> notes) {
        String sql = """
            UPDATE notes 
            SET note = ?, appreciation = ?, observation = ?, 
                 updated_at = NOW() 
            WHERE id = ?
            """;

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (Note note : notes) {
                    stmt.setDouble(1, note.getNote());
                    stmt.setString(2, note.getAppreciation());
                    stmt.setString(3, note.getObservation());
                    stmt.setInt(5, note.getId());

                    stmt.addBatch();
                }

                int[] results = stmt.executeBatch();
                connection.commit();

                // Vérifier que toutes les mises à jour ont réussi
                for (int result : results) {
                    if (result == PreparedStatement.EXECUTE_FAILED) {
                        return false;
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // ========================= DELETE =========================

    /**
     * Supprimer une note par son ID
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM notes WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprimer toutes les notes d'un étudiant pour un module
     */
    public boolean deleteByEtudiantAndModule(int etudiantId, int moduleId, int anneeScolaire, String semestre) {
        String sql = """
            DELETE FROM notes 
            WHERE etudiant_id = ? AND module_id = ? 
            AND annee_scolaire = ? AND semestre = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            stmt.setInt(2, moduleId);
            stmt.setInt(3, anneeScolaire);
            stmt.setString(4, semestre);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprimer toutes les notes d'un module
     */
    public boolean deleteByModule(int moduleId, int anneeScolaire, String semestre) {
        String sql = "DELETE FROM notes WHERE module_id = ? AND annee_scolaire = ? AND semestre = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, moduleId);
            stmt.setInt(2, anneeScolaire);
            stmt.setString(3, semestre);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ========================= STATISTIQUES =========================

    /**
     * Compter toutes les notes
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) as total FROM notes";

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




    // ========================= UTILS =========================

    /**
     * Méthode utilitaire pour mapper ResultSet à Note
     */
    private Note mapResultSetToNote(ResultSet rs) throws SQLException {
        Note note = new Note();

        note.setId(rs.getInt("id"));
        note.setEtudiantId(rs.getInt("etudiant_id"));
        note.setModuleId(rs.getInt("module_id"));
        note.setFormateurId(rs.getInt("formateur_id"));
        note.setNote(rs.getDouble("note"));
        note.setAppreciation(rs.getString("appreciation"));
        note.setObservation(rs.getString("observation"));
        note.setAnneeScolaire(rs.getInt("annee_scolaire"));
        note.setSemestre(rs.getString("semestre"));
        note.setTypeEvaluation(rs.getString("type_evaluation"));



        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            note.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            note.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        // 🔐 Mapper l'étudiant UNIQUEMENT si jointure présente
        try {
            rs.findColumn("e_id");

            Etudiant etudiant = new Etudiant();
            etudiant.setId(rs.getInt("e_id"));
            etudiant.setNom(rs.getString("e_nom"));
            etudiant.setPrenom(rs.getString("e_prenom"));
            etudiant.setCin(rs.getString("e_cin"));

            note.setEtudiant(etudiant);

        } catch (SQLException ignored) {
            // Aucun JOIN étudiant → normal pour certaines requêtes
            note.setEtudiant(null);
        }

        // 🔐 Mapper le module UNIQUEMENT si jointure présente
        try {
            rs.findColumn("m_id");

            Module module = new Module();
            module.setId(rs.getInt("m_id"));
            module.setNom(rs.getString("m_nom"));
            module.setCoefficient(rs.getInt("m_coefficient"));

            note.setModule(module);

        } catch (SQLException ignored) {
            note.setModule(null);
        }

        // 🔐 Mapper le formateur UNIQUEMENT si jointure présente
        try {
            rs.findColumn("f_id");

            Formateur formateur = new Formateur();
            formateur.setId(rs.getInt("f_id"));
            formateur.setNom(rs.getString("f_nom"));
            formateur.setPrenom(rs.getString("f_prenom"));

            note.setFormateur(formateur);

        } catch (SQLException ignored) {
            note.setFormateur(null);
        }

        return note;
    }




    /**
     * Trouver toutes les notes pour une année et un semestre
     */
    public List<Note> findAllByAnneeSemestre(int anneeScolaire, String semestre) {
        List<Note> notes = new ArrayList<>();
        String sql = """
        SELECT n.*, 
               e.id as e_id, e.nom as e_nom, e.prenom as e_prenom, e.cin as e_cin,
               g.id as g_id, g.matricule as g_matricule,
               m.id as m_id, m.nom as m_nom,
               f.id as f_id, f.nom as f_nom, f.prenom as f_prenom
        FROM notes n
        JOIN etudiants e ON n.etudiant_id = e.id
        LEFT JOIN groupes g ON e.groupe_id = g.id
        LEFT JOIN modules m ON n.module_id = m.id
        LEFT JOIN formateurs f ON n.formateur_id = f.id
        WHERE n.annee_scolaire = ? AND n.semestre = ?
        ORDER BY g.matricule, e.nom, m.nom
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, anneeScolaire);
            stmt.setString(2, semestre);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    /**
     * Trouver les notes d'un groupe
     */
    public List<Note> findByGroupe(int groupeId, int anneeScolaire, String semestre) {
        List<Note> notes = new ArrayList<>();
        String sql = """
        SELECT n.*, 
               e.id as e_id, e.nom as e_nom, e.prenom as e_prenom,
               m.id as m_id, m.nom as m_nom,
               f.id as f_id, f.nom as f_nom, f.prenom as f_prenom
        FROM notes n
        JOIN etudiants e ON n.etudiant_id = e.id
        LEFT JOIN modules m ON n.module_id = m.id
        LEFT JOIN formateurs f ON n.formateur_id = f.id
        WHERE e.groupe_id = ? AND n.annee_scolaire = ? AND n.semestre = ?
        ORDER BY e.nom, m.nom
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupeId);
            stmt.setInt(2, anneeScolaire);
            stmt.setString(3, semestre);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    /**
     * Trouver les notes par formateur
     */
    public List<Note> findByFormateur(int formateurId, int anneeScolaire, String semestre) {
        List<Note> notes = new ArrayList<>();
        String sql = """
        SELECT n.*, 
               e.id as e_id, e.nom as e_nom, e.prenom as e_prenom,
               g.id as g_id, g.matricule as g_matricule,
               m.id as m_id, m.nom as m_nom
        FROM notes n
        JOIN etudiants e ON n.etudiant_id = e.id
        LEFT JOIN groupes g ON e.groupe_id = g.id
        LEFT JOIN modules m ON n.module_id = m.id
        WHERE n.formateur_id = ? AND n.annee_scolaire = ? AND n.semestre = ?
        ORDER BY g.matricule, e.nom, m.nom
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, formateurId);
            stmt.setInt(2, anneeScolaire);
            stmt.setString(3, semestre);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    /**
     * Trouver les notes par groupe et formateur
     */
    public List<Note> findByGroupeFormateur(int groupeId, int formateurId, int anneeScolaire, String semestre) {
        List<Note> notes = new ArrayList<>();
        String sql = """
        SELECT n.*, 
               e.id as e_id, e.nom as e_nom, e.prenom as e_prenom,
               m.id as m_id, m.nom as m_nom
        FROM notes n
        JOIN etudiants e ON n.etudiant_id = e.id
        LEFT JOIN modules m ON n.module_id = m.id
        WHERE e.groupe_id = ? AND n.formateur_id = ? 
        AND n.annee_scolaire = ? AND n.semestre = ?
        ORDER BY e.nom, m.nom
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupeId);
            stmt.setInt(2, formateurId);
            stmt.setInt(3, anneeScolaire);
            stmt.setString(4, semestre);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    /**
     * Trouver les notes par module et formateur
     */
    public List<Note> findByModuleFormateur(int moduleId, int formateurId, int anneeScolaire, String semestre) {
        List<Note> notes = new ArrayList<>();
        String sql = """
        SELECT n.*, 
               e.id as e_id, e.nom as e_nom, e.prenom as e_prenom,
               g.id as g_id, g.matricule as g_matricule
        FROM notes n
        JOIN etudiants e ON n.etudiant_id = e.id
        LEFT JOIN groupes g ON e.groupe_id = g.id
        WHERE n.module_id = ? AND n.formateur_id = ? 
        AND n.annee_scolaire = ? AND n.semestre = ?
        ORDER BY g.matricule, e.nom
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, moduleId);
            stmt.setInt(2, formateurId);
            stmt.setInt(3, anneeScolaire);
            stmt.setString(4, semestre);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    /**
     * Trouver les notes par groupe, module et formateur
     */
    public List<Note> findByGroupeModuleFormateur(int groupeId, int moduleId, int formateurId,
                                                  int anneeScolaire, String semestre) {
        List<Note> notes = new ArrayList<>();
        String sql = """
        SELECT n.*, 
               e.id as e_id, e.nom as e_nom, e.prenom as e_prenom
        FROM notes n
        JOIN etudiants e ON n.etudiant_id = e.id
        WHERE e.groupe_id = ? AND n.module_id = ? AND n.formateur_id = ? 
        AND n.annee_scolaire = ? AND n.semestre = ?
        ORDER BY e.nom
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupeId);
            stmt.setInt(2, moduleId);
            stmt.setInt(3, formateurId);
            stmt.setInt(4, anneeScolaire);
            stmt.setString(5, semestre);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(mapResultSetToNote(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }




    /**
     * Calculer la note de module (40% contrôle + 60% examen)
     */
    public Double calculerNoteModule(int etudiantId, int moduleId, int anneeScolaire, String semestre) {
        String sql = """
        SELECT 
            MAX(CASE WHEN type_evaluation = 'Contrôle continu' THEN note END) as note_controle,
            MAX(CASE WHEN type_evaluation = 'Examen' THEN note END) as note_examen
        FROM notes
        WHERE etudiant_id = ? 
        AND module_id = ? 
        AND annee_scolaire = ? 
        AND semestre = ?
        AND type_evaluation IN ('Contrôle continu', 'Examen')
        GROUP BY etudiant_id, module_id
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            stmt.setInt(2, moduleId);
            stmt.setInt(3, anneeScolaire);
            stmt.setString(4, semestre);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Double noteControle = rs.getObject("note_controle", Double.class);
                Double noteExamen = rs.getObject("note_examen", Double.class);

                // Si une des notes est manquante, retourner null
                if (noteControle == null || noteExamen == null) {
                    return null;
                }

                // Calcul : 40% contrôle + 60% examen
                return (noteControle * 0.4) + (noteExamen * 0.6);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer toutes les notes calculées d'un étudiant pour un semestre
     */
    public List<ModuleNote> getNotesCalculesEtudiant(int etudiantId, int anneeScolaire, String semestre) {
        List<ModuleNote> resultats = new ArrayList<>();

        String sql = """
        SELECT 
            m.id as module_id,
            m.nom as module_nom,
            m.coefficient as coefficient,
            MAX(CASE WHEN n.type_evaluation = 'Contrôle continu' THEN n.note END) as note_controle,
            MAX(CASE WHEN n.type_evaluation = 'Examen' THEN n.note END) as note_examen
        FROM modules m
        LEFT JOIN notes n ON m.id = n.module_id 
            AND n.etudiant_id = ? 
            AND n.annee_scolaire = ?
            AND n.semestre = ?
            AND n.type_evaluation IN ('Contrôle continu', 'Examen')
        GROUP BY m.id, m.nom, m.coefficient
        ORDER BY m.nom
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            stmt.setInt(2, anneeScolaire);
            stmt.setString(3, semestre);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ModuleNote moduleNote = new ModuleNote();
                moduleNote.setModuleId(rs.getInt("module_id"));
                moduleNote.setModuleNom(rs.getString("module_nom"));
                moduleNote.setCoefficient(rs.getInt("coefficient"));
                moduleNote.setNoteControle(rs.getObject("note_controle", Double.class));
                moduleNote.setNoteExamen(rs.getObject("note_examen", Double.class));

                // Calculer la note du module si les deux notes existent
                if (moduleNote.getNoteControle() != null && moduleNote.getNoteExamen() != null) {
                    double noteModule = (moduleNote.getNoteControle() * 0.4) + (moduleNote.getNoteExamen() * 0.6);
                    moduleNote.setNoteModule(noteModule);
                    moduleNote.setNoteCie(noteModule * moduleNote.getCoefficient());
                }

                resultats.add(moduleNote);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultats;
    }

    /**
     * Trouver les notes calculées d'un étudiant pour un semestre AVEC FILTRE PAR GROUPE
     */
    public BulletinResultat calculerBulletinEtudiant(int etudiantId, int anneeScolaire, String semestre) {
        System.out.println("DEBUG: calculerBulletinEtudiant appelé avec:");
        System.out.println("  etudiantId: " + etudiantId);
        System.out.println("  anneeScolaire: " + anneeScolaire);
        System.out.println("  semestre: " + semestre);

        // Récupérer d'abord le groupe de l'étudiant
        String groupeSql = "SELECT groupe_id FROM etudiant WHERE id = ?";
        int groupeId = 0;

        try (PreparedStatement stmt = connection.prepareStatement(groupeSql)) {
            stmt.setInt(1, etudiantId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                groupeId = rs.getInt("groupe_id");
                System.out.println("DEBUG: Groupe ID trouvé: " + groupeId);
            }
        } catch (SQLException e) {
            System.err.println("ERREUR lors de la récupération du groupe:");
            e.printStackTrace();
        }

        // Si l'étudiant n'a pas de groupe, retourner un résultat vide
        if (groupeId == 0) {
            System.out.println("DEBUG: Aucun groupe trouvé pour l'étudiant");
            BulletinResultat resultat = new BulletinResultat();
            resultat.setModuleNotes(new ArrayList<>());
            resultat.setMoyenneGenerale(0);
            resultat.setDecision("Non évalué");
            return resultat;
        }

        // Récupérer les modules du groupe (via table new_groupe_new_module)
        String sql = """
    SELECT 
        m.id as module_id,
        m.nom as module_nom,
        m.coefficient as coefficient,
        MAX(CASE WHEN n.type_evaluation = 'Contrôle continu' THEN n.note END) as note_controle,
        MAX(CASE WHEN n.type_evaluation = 'Examen' THEN n.note END) as note_examen
    FROM modules m
    INNER JOIN new_groupe_new_module gm ON m.id = gm.module_id
    LEFT JOIN notes n ON m.id = n.module_id 
        AND n.etudiant_id = ? 
        AND n.annee_scolaire = ?
        AND n.semestre = ?
        AND n.type_evaluation IN ('Contrôle continu', 'Examen')
    WHERE gm.groupe_id = ?
    GROUP BY m.id, m.nom, m.coefficient
    ORDER BY m.nom
    """;

        List<ModuleNote> resultats = new ArrayList<>();
        double sommeNotesCie = 0;
        int sommeCoefficients = 0;
        int modulesComplets = 0;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // CORRECTION ICI: Définir tous les paramètres
            stmt.setInt(1, etudiantId);
            stmt.setInt(2, anneeScolaire);
            stmt.setString(3, semestre);
            stmt.setInt(4, groupeId);

            System.out.println("DEBUG: Requête SQL avec paramètres:");
            System.out.println("  Paramètre 1 (etudiantId): " + etudiantId);
            System.out.println("  Paramètre 2 (anneeScolaire): " + anneeScolaire);
            System.out.println("  Paramètre 3 (semestre): " + semestre);
            System.out.println("  Paramètre 4 (groupeId): " + groupeId);

            ResultSet rs = stmt.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                ModuleNote moduleNote = new ModuleNote();
                moduleNote.setModuleId(rs.getInt("module_id"));
                moduleNote.setModuleNom(rs.getString("module_nom"));
                moduleNote.setCoefficient(rs.getInt("coefficient"));
                moduleNote.setNoteControle(rs.getObject("note_controle", Double.class));
                moduleNote.setNoteExamen(rs.getObject("note_examen", Double.class));

                System.out.println("DEBUG: Module trouvé #" + rowCount + ":");
                System.out.println("  Module: " + moduleNote.getModuleNom());
                System.out.println("  Coefficient: " + moduleNote.getCoefficient());
                System.out.println("  Note contrôle: " + moduleNote.getNoteControle());
                System.out.println("  Note examen: " + moduleNote.getNoteExamen());

                // Calculer la note du module si les deux notes existent
                if (moduleNote.getNoteControle() != null && moduleNote.getNoteExamen() != null) {
                    double noteModule = (moduleNote.getNoteControle() * 0.4) + (moduleNote.getNoteExamen() * 0.6);
                    moduleNote.setNoteModule(noteModule);
                    moduleNote.setNoteCie(noteModule * moduleNote.getCoefficient());

                    sommeNotesCie += moduleNote.getNoteCie();
                    sommeCoefficients += moduleNote.getCoefficient();
                    modulesComplets++;
                }

                resultats.add(moduleNote);
            }
            System.out.println("DEBUG: Total modules trouvés: " + rowCount);
        } catch (SQLException e) {
            System.err.println("ERREUR lors de l'exécution de la requête principale:");
            e.printStackTrace();
        }

        // Calculer la moyenne
        BulletinResultat bulletin = new BulletinResultat();
        bulletin.setModuleNotes(resultats);

        if (sommeCoefficients > 0 && modulesComplets > 0) {
            double moyenneGenerale = sommeNotesCie / sommeCoefficients;
            bulletin.setMoyenneGenerale(moyenneGenerale);
            bulletin.setDecision(moyenneGenerale >= 10 ? "Admis" : "Non Admis");
            System.out.println("DEBUG: Moyenne calculée: " + moyenneGenerale);
            System.out.println("DEBUG: Décision: " + bulletin.getDecision());
        } else {
            bulletin.setMoyenneGenerale(0);
            bulletin.setDecision("Non évalué");
            System.out.println("DEBUG: Pas assez de notes pour calculer la moyenne");
            System.out.println("DEBUG: Modules complets: " + modulesComplets + "/" + resultats.size());
        }

        bulletin.setNombreModules(resultats.size());
        bulletin.setNombreModulesComplets(modulesComplets);

        System.out.println("DEBUG: Bulletin généré avec " + resultats.size() + " modules");

        return bulletin;
    }

    /**
     * Vérifier si un étudiant a des notes pour un semestre donné
     */
    public boolean hasNotesForSemestre(int etudiantId, int anneeScolaire, String semestre) {
        String sql = """
        SELECT COUNT(*) as count FROM notes 
        WHERE etudiant_id = ? AND annee_scolaire = ? AND semestre = ?
        AND type_evaluation IN ('Contrôle continu', 'Examen')
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, etudiantId);
            stmt.setInt(2, anneeScolaire);
            stmt.setString(3, semestre);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // Classe pour représenter la note d'un module
    public static class ModuleNote {
        private int moduleId;
        private String moduleNom;
        private int coefficient;
        private Double noteControle;
        private Double noteExamen;
        private Double noteModule;
        private Double noteCie;

        // Getters et Setters
        public int getModuleId() { return moduleId; }
        public void setModuleId(int moduleId) { this.moduleId = moduleId; }

        public String getModuleNom() { return moduleNom; }
        public void setModuleNom(String moduleNom) { this.moduleNom = moduleNom; }

        public int getCoefficient() { return coefficient; }
        public void setCoefficient(int coefficient) { this.coefficient = coefficient; }

        public Double getNoteControle() { return noteControle; }
        public void setNoteControle(Double noteControle) { this.noteControle = noteControle; }

        public Double getNoteExamen() { return noteExamen; }
        public void setNoteExamen(Double noteExamen) { this.noteExamen = noteExamen; }

        public Double getNoteModule() { return noteModule; }
        public void setNoteModule(Double noteModule) { this.noteModule = noteModule; }

        public Double getNoteCie() { return noteCie; }
        public void setNoteCie(Double noteCie) { this.noteCie = noteCie; }
    }

    // Classe pour représenter le bulletin complet
    public static class BulletinResultat {
        private List<ModuleNote> moduleNotes;
        private double moyenneGenerale;
        private String decision;
        private int nombreModules;
        private int nombreModulesComplets;

        // Getters et Setters
        public List<ModuleNote> getModuleNotes() { return moduleNotes; }
        public void setModuleNotes(List<ModuleNote> moduleNotes) { this.moduleNotes = moduleNotes; }

        public double getMoyenneGenerale() { return moyenneGenerale; }
        public void setMoyenneGenerale(double moyenneGenerale) { this.moyenneGenerale = moyenneGenerale; }

        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }

        public int getNombreModules() { return nombreModules; }
        public void setNombreModules(int nombreModules) { this.nombreModules = nombreModules; }

        public int getNombreModulesComplets() { return nombreModulesComplets; }
        public void setNombreModulesComplets(int nombreModulesComplets) { this.nombreModulesComplets = nombreModulesComplets; }
    }
}