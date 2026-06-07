package database;

import model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // ─── CREATE ────────────────────────────────────────────────────────────────
    public boolean insertProduct(Product product) {
        String sql = "INSERT INTO products (code, name, amount, price, thumbnail) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getCode());
            pstmt.setString(2, product.getName());
            pstmt.setInt(3, product.getAmount());
            pstmt.setDouble(4, product.getPrice());
            pstmt.setBytes(5, product.getThumbnail());
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Insert error: " + e.getMessage());
            return false;
        }
    }

    // ─── READ ALL ──────────────────────────────────────────────────────────────
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, code, name, amount, price, thumbnail FROM products ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getInt("amount"),
                    rs.getDouble("price"),
                    rs.getBytes("thumbnail")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Fetch all error: " + e.getMessage());
        }
        return products;
    }

    // ─── READ ONE ──────────────────────────────────────────────────────────────
    public Product getProductById(int id) {
        String sql = "SELECT id, code, name, amount, price, thumbnail FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Product(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getInt("amount"),
                    rs.getDouble("price"),
                    rs.getBytes("thumbnail")
                );
            }
        } catch (SQLException e) {
            System.err.println("Fetch by ID error: " + e.getMessage());
        }
        return null;
    }

    // ─── UPDATE ────────────────────────────────────────────────────────────────
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET code=?, name=?, amount=?, price=?, thumbnail=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getCode());
            pstmt.setString(2, product.getName());
            pstmt.setInt(3, product.getAmount());
            pstmt.setDouble(4, product.getPrice());
            pstmt.setBytes(5, product.getThumbnail());
            pstmt.setInt(6, product.getId());
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Update error: " + e.getMessage());
            return false;
        }
    }

    // ─── DELETE ────────────────────────────────────────────────────────────────
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Delete error: " + e.getMessage());
            return false;
        }
    }

    // ─── AUTO-GENERATE CODE ────────────────────────────────────────────────────
//    public String generateNextCode() {
//        String sql = "SELECT COUNT(*) AS cnt FROM products";
//        try (Connection conn = DatabaseConnection.getConnection();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(sql)) {
//
//            if (rs.next()) {
//                int count = rs.getInt("cnt") + 1;
//                return String.format("PRD%04d", count);
//            }
//        } catch (SQLException e) {
//            System.err.println("Code gen error: " + e.getMessage());
//        }
//        return "PRD0001";
//    }

    public String generateNextCode() {
        String update = "UPDATE product_code_seq SET last_num = last_num + 1";
        String select = "SELECT last_num FROM product_code_seq";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // wrap in transaction
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(update);
                ResultSet rs = stmt.executeQuery(select);
                if (rs.next()) {
                    int num = rs.getInt("last_num");
                    conn.commit();
                    return String.format("PRD%04d", num);
                }
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Code gen error: " + e.getMessage());
        }
        return "PRD0001";
    }
}
