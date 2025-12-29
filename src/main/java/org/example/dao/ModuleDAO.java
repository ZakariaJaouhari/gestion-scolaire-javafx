package org.example.dao;

import org.example.model.Etudiant;
import org.example.model.Formateur;
import org.example.model.Groupe;
import org.example.model.Module;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class ModuleDAO {

    private Connection connection;

    public ModuleDAO() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    // Créer un module
    public Long create(Module module) {
        String sql = "INSERT INTO modules (nom, matricule, date_D, date_F, heures_P, coefficient, formateur_id, directeur_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, module.getNom());
            stmt.setString(2, module.getMatricule());
            stmt.setDate(3, Date.valueOf(module.getDateDebut()));
            stmt.setDate(4, Date.valueOf(module.getDateFin()));
            stmt.setString(5, module.getHeuresPratique());
            stmt.setInt(6, module.getCoefficient());
            stmt.setInt(7, module.getFormateurId());
            stmt.setInt(8, module.getDirecteurId());

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

    // Mettre à jour un étudiant
    public boolean update(Module module) {
        String sql = "UPDATE modules SET nom = ?, matricule = ?, date_D = ?, " +
                "date_F = ?, heures_P = ?, coefficient = ?, formateur_id = ?, " +
                "directeur_id = ?, updated_at = NOW() " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, module.getNom());
            stmt.setString(2, module.getMatricule());
            stmt.setDate(3, Date.valueOf(module.getDateDebut()));
            stmt.setDate(4, Date.valueOf(module.getDateFin()));
            stmt.setString(5, module.getHeuresPratique());
            stmt.setString(6, String.valueOf(module.getCoefficient()));
            stmt.setInt(7, module.getFormateurId());
            stmt.setInt(8, module.getDirecteurId());
            stmt.setInt(9, module.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Trouver les modules par groupe ID (via la table de relation)
    public List<Module> findByGroupeId(int groupeId) {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT m.*, f.nom as formateur_nom, f.prenom as formateur_prenom " +
                "FROM modules m " +
                "LEFT JOIN formateurs f ON m.formateur_id = f.id " +
                "INNER JOIN new_groupe_new_module gm ON m.id = gm.module_id " +
                "WHERE gm.groupe_id = ? " +
                "ORDER BY m.matricule";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupeId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Module module = mapResultSetToModule(rs);

                // Ajouter les infos du formateur
                Formateur formateur = new Formateur();
                formateur.setNom(rs.getString("formateur_nom"));
                formateur.setPrenom(rs.getString("formateur_prenom"));
                module.setFormateur(formateur);

                modules.add(module);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modules;
    }

    // Trouver les groupes par module ID (l'inverse)
    public List<Integer> findGroupeIdsByModuleId(int moduleId) {
        List<Integer> groupeIds = new ArrayList<>();
        String sql = "SELECT groupe_id FROM new_groupe_new_module WHERE module_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, moduleId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                groupeIds.add(rs.getInt("groupe_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupeIds;
    }

    // Ajouter une relation groupe-module
    public boolean addModuleToGroupe(int groupeId, int moduleId) {
        String sql = "INSERT INTO new_groupe_new_module (groupe_id, module_id) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupeId);
            stmt.setInt(2, moduleId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Supprimer une relation groupe-module
    public boolean removeModuleFromGroupe(int groupeId, int moduleId) {
        String sql = "DELETE FROM new_groupe_new_module WHERE groupe_id = ? AND module_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupeId);
            stmt.setInt(2, moduleId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Vérifier si une relation existe
    public boolean relationExists(int groupeId, int moduleId) {
        String sql = "SELECT COUNT(*) as count FROM new_groupe_new_module WHERE groupe_id = ? AND module_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupeId);
            stmt.setInt(2, moduleId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Trouver les modules disponibles (non associés à un groupe spécifique)
    public List<Module> findAvailableModulesForGroupe(int directeurId, int groupeId) {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT m.*, f.nom as formateur_nom, f.prenom as formateur_prenom " +
                "FROM modules m " +
                "LEFT JOIN formateurs f ON m.formateur_id = f.id " +
                "WHERE m.directeur_id = ? " +
                "AND m.id NOT IN ( " +
                "  SELECT module_id FROM new_groupe_new_module WHERE groupe_id = ? " +
                ") " +
                "ORDER BY m.matricule";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, directeurId);
            stmt.setInt(2, groupeId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Module module = mapResultSetToModule(rs);

                Formateur formateur = new Formateur();
                formateur.setNom(rs.getString("formateur_nom"));
                formateur.setPrenom(rs.getString("formateur_prenom"));
                module.setFormateur(formateur);

                modules.add(module);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modules;
    }

    // Méthode utilitaire pour mapper ResultSet à Module
    private Module mapResultSetToModule(ResultSet rs) throws SQLException {
        Module module = new Module();

        module.setId(rs.getInt("id"));
        module.setNom(rs.getString("nom"));
        module.setMatricule(rs.getString("matricule"));

        // ATTENTION : vos colonnes sont date_D et date_F, pas date_debut et date_fin
        Date dateDebut = rs.getDate("date_D");  // <-- Changé ici
        if (dateDebut != null) {
            module.setDateDebut(dateDebut.toLocalDate());
        }

        Date dateFin = rs.getDate("date_F");    // <-- Changé ici
        if (dateFin != null) {
            module.setDateFin(dateFin.toLocalDate());
        }

        module.setHeuresPratique(rs.getString("heures_P"));  // <-- Changé ici
        module.setCoefficient(rs.getInt("coefficient"));
        module.setFormateurId(rs.getInt("formateur_id"));
        module.setDirecteurId(rs.getInt("directeur_id"));

        return module;
    }


    // Dans ModuleDAO.java
    public List<Module> findByDirecteurId(int directeurId) {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT m.*, f.nom as formateur_nom, f.prenom as formateur_prenom " +
                "FROM modules m " +
                "LEFT JOIN formateurs f ON m.formateur_id = f.id " +
                "WHERE m.directeur_id = ? " +
                "ORDER BY m.matricule";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, directeurId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Module module = mapResultSetToModule(rs);

                // Ajouter les infos du formateur
                Formateur formateur = new Formateur();
                formateur.setNom(rs.getString("formateur_nom"));
                formateur.setPrenom(rs.getString("formateur_prenom"));
                module.setFormateur(formateur);

                modules.add(module);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modules;
    }

    public List<Module> findByFormateurId(int formateurId) {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT * FROM modules WHERE formateur_id = ? ORDER BY nom";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, formateurId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                modules.add(mapResultSetToModule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modules;
    }

    public List<Module> findByFormateurIdAndGroupeId(int formateurId, int groupeId) {
        List<Module> modules = new ArrayList<>();
        String sql = """
            SELECT DISTINCT m.* 
            FROM modules m
            INNER JOIN new_groupe_new_module gm ON m.id = gm.module_id
            WHERE m.formateur_id = ? 
            AND gm.groupe_id = ?
            ORDER BY m.nom
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, formateurId);
            stmt.setInt(2, groupeId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                modules.add(mapResultSetToModule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modules;
    }

    // Supprimer un module
    public boolean delete(int id) {
        String sql = "DELETE FROM modules WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Module> findAll() {
        List<Module> modules = new ArrayList<>();
        String sql = "SELECT * FROM modules ORDER BY nom";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                modules.add(mapResultSetToModule(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modules;
    }

    public Module findById(int id) {
        String sql = "SELECT * FROM modules WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Module module = new Module();
                module.setId(rs.getInt("id"));
                module.setNom(rs.getString("nom"));
                module.setMatricule(rs.getString("matricule"));

                // Dates
                Date dateD = rs.getDate("date_D");
                if (dateD != null) {
                    module.setDateDebut(dateD.toLocalDate());
                }

                Date dateF = rs.getDate("date_F");
                if (dateF != null) {
                    module.setDateFin(dateF.toLocalDate());
                }

                module.setHeuresPratique(rs.getString("heures_P"));

                // Coefficient (attention aux valeurs null)
                Object coefficientObj = rs.getObject("coefficient");
                if (coefficientObj != null) {
                    if (coefficientObj instanceof Number) {
                        module.setCoefficient(((Number) coefficientObj).intValue());
                    } else {
                        try {
                            module.setCoefficient(Integer.parseInt(coefficientObj.toString()));
                        } catch (NumberFormatException e) {
                            module.setCoefficient(1); // Valeur par défaut
                        }
                    }
                } else {
                    module.setCoefficient(1); // Valeur par défaut
                }

                module.setFormateurId(rs.getInt("formateur_id"));
                module.setDirecteurId(rs.getInt("directeur_id"));

                return module;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }




    public Map<String, Object> getStatistiquesModulesParGroupe(int directeurId) {
        Map<String, Object> stats = new HashMap<>();
        String sql = """
        SELECT 
            g.id as groupe_id,
            g.matricule as groupe_nom,
            COUNT(DISTINCT m.id) as nombre_modules,
            COUNT(DISTINCT f.id) as nombre_formateurs
        FROM groupes g
        LEFT JOIN new_groupe_new_module gm ON g.id = gm.groupe_id
        LEFT JOIN modules m ON gm.module_id = m.id
        LEFT JOIN formateurs f ON m.formateur_id = f.id
        WHERE g.directeur_id = ?
        GROUP BY g.id, g.matricule
        ORDER BY g.matricule
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, directeurId);
            ResultSet rs = stmt.executeQuery();

            List<Integer> modules = new ArrayList<>();
            List<Integer> formateurs = new ArrayList<>();

            while (rs.next()) {
                modules.add(rs.getInt("nombre_modules"));
                formateurs.add(rs.getInt("nombre_formateurs"));
            }

            stats.put("modules", modules);
            stats.put("formateurs", formateurs);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}
