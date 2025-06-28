package org.example.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Users {
    private static Users instance;
    private List<User> users;
    
    private Users() {
        users = new ArrayList<>();
    }
    
    public static synchronized Users getInstance() {
        if (instance == null) {
            instance = new Users();
        }
        return instance;
    }
    
    public List<User> getUsers() {
        return users;
    }
    
    public void addUser(User user) {
        users.add(user);
    }
    
    public User getUserById(int userId) {
        for (User user : users) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }
    
    public User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
    
    public void loadFromDatabase(Connection connection) throws SQLException {
        users.clear();
        String query = "SELECT u.user_id, u.username, u.password_hash, u.role_id, r.role_name " +
                      "FROM users u JOIN roles r ON u.role_id = r.role_id ORDER BY u.user_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Role role = new Role(
                    rs.getInt("role_id"),
                    rs.getString("role_name")
                );
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    role
                );
                users.add(user);
            }
        }
    }
    
    public void clear() {
        users.clear();
    }
} 