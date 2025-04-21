import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseConnection {
    private static final String URL = "jdbc:oracle:thin:@localhost:1522:xe";
    private static final String USER = "shivaani";
    private static final String PASS = "shivaani";
    private static Connection conn = null;

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                Class.forName("oracle.jdbc.OracleDriver");
                conn = DriverManager.getConnection(URL, USER, PASS);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Oracle JDBC Driver not found");
            }
        }
        return conn;
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String getCurrentUser() throws SQLException {
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT USER FROM DUAL")) {
            return rs.next() ? rs.getString(1) : "Unknown";
        }
    }
}