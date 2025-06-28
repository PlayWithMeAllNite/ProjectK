package org.example.controller;

import org.example.database.DatabaseManager;
import org.example.model.User;
import org.example.model.Role;
import org.example.view.MainController;
import org.example.util.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthController {
    private static AuthController instance;
    private User currentUser;
    
    private AuthController() {}
    
    public static synchronized AuthController getInstance() {
        if (instance == null) {
            instance = new AuthController();
        }
        return instance;
    }
    
    public boolean login(String username, String password) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "SELECT u.user_id, u.username, u.password_hash, u.role_id, r.role_name " +
                          "FROM users u JOIN roles r ON u.role_id = r.role_id " +
                          "WHERE u.username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");
                        if (PasswordUtils.verifyPassword(password, storedHash)) {
                            Role role = new Role(
                                rs.getInt("role_id"),
                                rs.getString("role_name")
                            );
                            currentUser = new User(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                storedHash,
                                role
                            );
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Применяет права доступа после успешного входа
     */
    public void applyPermissionsAfterLogin() {
        if (currentUser != null && MainController.getInstance() != null) {
            MainController.getInstance().applyPermissions();
        }
    }
    
    public void logout() {
        currentUser = null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean hasRole(String roleName) {
        return currentUser != null && currentUser.getRole().getRoleName().equals(roleName);
    }
    
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    public boolean isManager() {
        return hasRole("MANAGER");
    }
    
    public boolean isMaster() {
        return hasRole("MASTER");
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    public boolean registerUser(String username, String password, String roleName) {
        if (!isAdmin()) {
            return false;
        }
        
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            // Проверяем, существует ли пользователь
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Пользователь уже существует
                    }
                }
            }
            
            // Получаем role_id
            String roleQuery = "SELECT role_id FROM roles WHERE role_name = ?";
            int roleId = 0;
            try (PreparedStatement roleStmt = connection.prepareStatement(roleQuery)) {
                roleStmt.setString(1, roleName);
                try (ResultSet rs = roleStmt.executeQuery()) {
                    if (rs.next()) {
                        roleId = rs.getInt("role_id");
                    } else {
                        return false; // Роль не найдена
                    }
                }
            }
            
            // Добавляем пользователя
            String insertQuery = "INSERT INTO users (username, password_hash, role_id) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, hashPassword(password));
                insertStmt.setInt(3, roleId);
                
                int result = insertStmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }
} 