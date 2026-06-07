package database;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public User findUser(String userId, String name) {
        String sql = "SELECT user_id, name, role FROM users WHERE user_id = ? AND name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("role")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("User not found! " + e.getMessage());
        }

        return null;
    }
}
