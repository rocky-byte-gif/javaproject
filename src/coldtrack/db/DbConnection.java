package coldtrack.db;

import java.sql.Connection;
import java.sql.DriverManager;

// [Practical 5] Packages: coldtrack.db
// [Practical 10] JDBC Connection configuration
public class DbConnection {

    // Database connection constants - edit user and password if needed
    public static final String URL = "jdbc:mysql://localhost:3306/coldtrack?useSSL=false&allowPublicKeyRetrieval=true";
    public static final String USER = "root";
    public static final String PASSWORD = "root";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // [Practical 10] Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("[DB] MySQL Connector/J driver not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[DB] Connection failed: " + e.getMessage());
        }
        return conn;
    }
}
