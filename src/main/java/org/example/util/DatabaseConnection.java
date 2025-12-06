package org.example.util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/gestion_scolaire_javafx";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // change si besoin

    private static Connection connection;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion MySQL réussie !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur connexion MySQL : " + e.getMessage());
        }
        return connection;
    }
}