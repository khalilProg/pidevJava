package tn.esprit.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private static DataSource instance;
    private Connection conn;
    private final String URL = "jdbc:mysql://localhost:3306/pidev";
    private final String USER = "root";
    private final String PASSWORD = "";

    // Private constructor to ensure it's a singleton
    private DataSource() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Central database connection established successfully!");
        } catch (SQLException e) {
            System.err.println("❌ CRITICAL: Database connection failed: " + e.getMessage());
        }
    }

    // Public method to get the single instance of this class
    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }

    public Connection getConn() {
        return conn;
    }
}