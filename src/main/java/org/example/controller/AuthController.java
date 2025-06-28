package org.example.controller;

import org.example.database.DatabaseManager;
import org.example.model.User;
import org.example.model.Role;
import org.example.model.Roles;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

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
    
    public boolean authenticate(String username, String password) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "SELECT u.user_id, u.username, u.password_hash, u.role_id, r.role_name " +
                           "FROM users u JOIN roles r ON u.role_id = r.role_id " +
                           "WHERE u.username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");
                        String inputHash = hashPassword(password);
                        if (storedHash.equals(inputHash)) {
                            Role role = new Role(
                                rs.getInt("role_id"),
                                rs.getString("role_name")
                            );
                            currentUser = new User(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("password_hash"),
                                role
                            );
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public void logout() {
        currentUser = null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    public String getCurrentRole() {
        return currentUser != null ? currentUser.getRole().getRoleName() : null;
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
} 