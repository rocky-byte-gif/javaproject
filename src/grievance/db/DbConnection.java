package grievance.db;

import java.sql.Connection;
import java.sql.DriverManager;

// [Practical 5] Packages: grievance.db
// [Practical 10] JDBC Connection utility class
public class DbConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/grievance_db?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "root";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // [Practical 10] Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            System.out.println("[DB] MySQL Driver not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[DB] Connection error: " + e.getMessage());
        }
        return conn;
    }
}
