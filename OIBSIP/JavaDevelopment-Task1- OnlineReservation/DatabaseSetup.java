

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

    private static final String URL = "jdbc:sqlite:reservation.db";

    /** Use this everywhere you need a DB connection. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /** Hash a password with SHA-256 (never store plain text). */
    public static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest(text.getBytes("UTF-8"))) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Call once from MainApp.main() before opening the login. */
    public static void init() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {

            s.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT UNIQUE NOT NULL, "
                    + "password_hash TEXT NOT NULL)");

            s.execute("CREATE TABLE IF NOT EXISTS trains ("
                    + "train_no INTEGER PRIMARY KEY, "
                    + "train_name TEXT NOT NULL)");

            s.execute("CREATE TABLE IF NOT EXISTS reservations ("
                    + "pnr TEXT PRIMARY KEY, "
                    + "passenger_name TEXT NOT NULL, "
                    + "train_no INTEGER NOT NULL, "
                    + "train_name TEXT NOT NULL, "
                    + "class_type TEXT NOT NULL, "
                    + "journey_date TEXT NOT NULL, "
                    + "source TEXT NOT NULL, "
                    + "destination TEXT NOT NULL)");

            // Default login: admin / admin123
            try (PreparedStatement p = c.prepareStatement(
                    "INSERT OR IGNORE INTO users(username, password_hash) VALUES(?, ?)")) {
                p.setString(1, "admin");
                p.setString(2, hash("admin123"));
                p.executeUpdate();
            }

            // Sample trains
            Object[][] trains = {
                    {12951, "Mumbai Rajdhani"},
                    {12301, "Howrah Rajdhani"},
                    {12627, "Karnataka Express"},
                    {12009, "Shatabdi Express"},
                    {12723, "Telangana Express"}
            };
            try (PreparedStatement p = c.prepareStatement(
                    "INSERT OR IGNORE INTO trains(train_no, train_name) VALUES(?, ?)")) {
                for (Object[] t : trains) {
                    p.setInt(1, (Integer) t[0]);
                    p.setString(2, (String) t[1]);
                    p.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database setup failed: " + e.getMessage(), e);
        }
    }

    /** Optional: run this file alone to test that the DB gets created. */
    public static void main(String[] args) {
        init();
        System.out.println("Database ready: reservation.db");
    }
}