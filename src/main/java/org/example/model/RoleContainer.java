package org.example.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleContainer {
    private static RoleContainer instance;
    private List<Role> roles;
    
    // Конструктор для Singleton
    private RoleContainer() {
        roles = new ArrayList<>();
    }
    
    // Singleton метод
    public static synchronized RoleContainer getInstance() {
        if (instance == null) {
            instance = new RoleContainer();
        }
        return instance;
    }
    
    // Методы для управления списком ролей
    public List<Role> getRoles() {
        return roles;
    }
    
    public void addRole(Role role) {
        roles.add(role);
    }
    
    public Role getRoleById(int roleId) {
        for (Role role : roles) {
            if (role.getRoleId() == roleId) {
                return role;
            }
        }
        return null;
    }
    
    public Role getRoleByName(String roleName) {
        for (Role role : roles) {
            if (role.getRoleName().equals(roleName)) {
                return role;
            }
        }
        return null;
    }
    
    public void loadFromDatabase(Connection connection) throws SQLException {
        roles.clear();
        String query = "SELECT role_id, role_name FROM roles ORDER BY role_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Role role = new Role(
                    rs.getInt("role_id"),
                    rs.getString("role_name")
                );
                roles.add(role);
            }
        }
    }
    
    public void clear() {
        roles.clear();
    }
} 