package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/gestion_scolaire_javafx";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private DatabaseConnection() {
        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Établir la connexion
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Connexion à la base de données établie");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé");
            e.printStackTrace();
            throw new RuntimeException("Driver MySQL non trouvé", e);
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données");
            e.printStackTrace();
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}