package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/ocean_view_resort";
    private static final String USER = "root"; // Update this with your actual MySQL username
    private static final String PASSWORD = ""; // Update this with your actual MySQL password

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
        return conn;
    }
}
