package database;

import model.OrderItem;

import java.sql.*;
import java.util.List;

public class OrderDAO {

    private final ProductDAO productDAO = new ProductDAO();

    // ─── CREATE ORDER (with items, decreasing stock) ──────────────────────────
    public int createOrder(String cashierId, double total, List<OrderItem> items) {
        String insertOrder = "INSERT INTO orders (cashier_id, total) VALUES (?, ?)";
        String insertItem  = "INSERT INTO order_items (order_id, product_code, product_name, amount, unit_price, subtotal) " +
                              "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int orderId;
                try (PreparedStatement ps = conn.prepareStatement(insertOrder)) {
                    ps.setString(1, cashierId);
                    ps.setDouble(2, total);
                    ps.executeUpdate();
                }
                try (Statement stmt = conn.createStatement();
                     ResultSet keys = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (!keys.next()) throw new SQLException("Failed to obtain order id.");
                    orderId = keys.getInt(1);
                }

                try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                    for (OrderItem item : items) {
                        if (!productDAO.decreaseStock(item.getProductCode(), item.getAmount(), conn)) {
                            throw new SQLException("Not enough stock for product " + item.getProductCode());
                        }
                        ps.setInt(1, orderId);
                        ps.setString(2, item.getProductCode());
                        ps.setString(3, item.getProductName());
                        ps.setInt(4, item.getAmount());
                        ps.setDouble(5, item.getUnitPrice());
                        ps.setDouble(6, item.getSubtotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                return orderId;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Create order error: " + e.getMessage());
                return -1;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Create order error: " + e.getMessage());
            return -1;
        }
    }
}
