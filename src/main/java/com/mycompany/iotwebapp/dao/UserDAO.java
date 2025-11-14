package com.mycompany.iotwebapp.dao;

import com.mycompany.iotwebapp.model.User;
import java.sql.*;
import java.time.LocalDateTime;

public class UserDAO {
    
    /**
     * Authenticate user with username and password
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT user_id, username, password, full_name, email, role, is_active, created_at, last_login " +
                     "FROM Users WHERE username = ? AND is_active = 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                // Simple password comparison (in production, use hashed passwords)
                if (password.equals(storedPassword)) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setIsActive(rs.getBoolean("is_active"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        user.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    
                    Timestamp lastLogin = rs.getTimestamp("last_login");
                    if (lastLogin != null) {
                        user.setLastLogin(lastLogin.toLocalDateTime());
                    }
                    
                    // Update last login time
                    updateLastLogin(user.getUserId());
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Update last login timestamp
     */
    private void updateLastLogin(Integer userId) {
        String sql = "UPDATE Users SET last_login = GETUTCDATE() WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Find user by username
     */
    public User findByUsername(String username) {
        String sql = "SELECT user_id, username, full_name, email, role, is_active, created_at, last_login " +
                     "FROM Users WHERE username = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setIsActive(rs.getBoolean("is_active"));
                
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    user.setCreatedAt(createdAt.toLocalDateTime());
                }
                
                Timestamp lastLogin = rs.getTimestamp("last_login");
                if (lastLogin != null) {
                    user.setLastLogin(lastLogin.toLocalDateTime());
                }
                
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
