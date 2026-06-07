package database;

import java.sql.*;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:happymart.db";
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("Connected to SQLite database.");
                initializeDatabase(connection);
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found: " + e.getMessage());
            }
        }
        return connection;
    }

    private static void initializeDatabase(Connection conn) throws SQLException {
        String createProducts = """
            CREATE TABLE IF NOT EXISTS products (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                code      TEXT    NOT NULL UNIQUE,
                name      TEXT    NOT NULL,
                amount    INTEGER NOT NULL DEFAULT 0,
                price     REAL    NOT NULL DEFAULT 0.0,
                thumbnail BLOB
            );
        """;

        String createSeq = "CREATE TABLE IF NOT EXISTS product_code_seq (last_num INTEGER NOT NULL DEFAULT 0);";
        String seedSeq   = "INSERT INTO product_code_seq (last_num) SELECT COUNT(*) FROM products WHERE NOT EXISTS (SELECT 1 FROM product_code_seq);";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createProducts);
//            stmt.execute(createUsers);
            stmt.execute(createSeq);
            stmt.execute(seedSeq);
            System.out.println("Database tables ready.");
        }

    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }


}
