package org.example.model;

public class Role {
    private int roleId;
    private String roleName;
    
    // Конструкторы
    public Role(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }
    
    public Role(String roleName) {
        this.roleName = roleName;
    }
    
    // Геттеры
    public int getRoleId() {
        return roleId;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    // Сеттеры
    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    @Override
    public String toString() {
        return roleName;
    }
} 