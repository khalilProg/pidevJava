package tn.esprit.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private String url = "jdbc:mysql://localhost:3306/pidev"; // [cite: 133]
    private String user = "root"; // [cite: 134]
    private String pwd = ""; // [cite: 135]
    private Connection conn;
    private static MyDatabase instance;

    private MyDatabase() {
        try {
            conn = DriverManager.getConnection(url, user, pwd); // [cite: 127]
            System.out.println("Connection established once for the whole app!");
        } catch (SQLException e) {
            System.err.println(e.getMessage()); // [cite: 128-129]
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) instance = new MyDatabase();
        return instance;
    }

    public Connection getConn() { return conn; }
}