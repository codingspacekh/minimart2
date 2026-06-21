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

        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                user_id TEXT PRIMARY KEY,
                name    TEXT NOT NULL,
                role    TEXT NOT NULL DEFAULT 'cashier'
            );
        """;


        String createSeq = "CREATE TABLE IF NOT EXISTS product_code_seq (last_num INTEGER NOT NULL DEFAULT 0);";
        String seedSeq   = "INSERT INTO product_code_seq (last_num) SELECT COUNT(*) FROM products WHERE NOT EXISTS (SELECT 1 FROM product_code_seq);";

        String createOrders = """
            CREATE TABLE IF NOT EXISTS orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cashier_id TEXT NOT NULL,
                total REAL NOT NULL DEFAULT 0.0,
                created_at TEXT NOT NULL DEFAULT (datetime('now'))
            );
        """;

        String createOrderItems = """
            CREATE TABLE IF NOT EXISTS order_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_code      TEXT    NOT NULL,
                product_name      TEXT    NOT NULL,
                amount    INTEGER NOT NULL,
                unit_price     REAL    NOT NULL,
                subtotal REAL NOT NULL,
                FOREIGN KEY (order_id) REFERENCES orders(id)
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createProducts);
            stmt.execute(createUsers);

            stmt.execute(createSeq);
            stmt.execute(seedSeq);
            stmt.execute(createOrders);
            stmt.execute(createOrderItems);

            System.out.println("Database tables ready.");
        }

        seedUsers(conn);
    }

    private static void seedUsers(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")){
            if (rs.next() && rs.getInt(1) > 0) return;
        }
        String[][] users = {
                {"ADM001", "Admin", "admin"},
                {"CSH001", "joe", "cashier"},
                {"CSH002", "jane", "cashier"}
        };
        String sql = "INSERT INTO users (user_id, name, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String[] u: users) {
                pstmt.setString(1, u[0]);
                pstmt.setString(2, u[1]);
                pstmt.setString(3, u[2]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("Users created.");
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
