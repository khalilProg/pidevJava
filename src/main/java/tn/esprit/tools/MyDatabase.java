package tn.esprit.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/bloodlink?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection cnx;
    private static MyDatabase instance;

    // Private constructor (Singleton)
    private MyDatabase() {
        try {
            // Optional (forces driver loading)
            Class.forName("com.mysql.cj.jdbc.Driver");

            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connection established successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL Driver not found!");
        } catch (SQLException e) {
            System.out.println("❌ Database connection error: " + e.getMessage());
        }
    }

    // Singleton access
    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
