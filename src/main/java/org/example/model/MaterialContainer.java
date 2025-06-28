package org.example.model;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialContainer {
    private static MaterialContainer instance;
    private List<Material> materials;
    
    // Конструктор для Singleton
    private MaterialContainer() {
        materials = new ArrayList<>();
    }
    
    // Singleton метод
    public static synchronized MaterialContainer getInstance() {
        if (instance == null) {
            instance = new MaterialContainer();
        }
        return instance;
    }
    
    // Методы для управления списком материалов
    public List<Material> getMaterials() {
        return materials;
    }
    
    public void addMaterial(Material material) {
        materials.add(material);
    }
    
    public Material getMaterialById(int materialId) {
        for (Material material : materials) {
            if (material.getMaterialId() == materialId) {
                return material;
            }
        }
        return null;
    }
    
    public Material getMaterialByName(String name) {
        for (Material material : materials) {
            if (material.getName().equals(name)) {
                return material;
            }
        }
        return null;
    }
    
    public void loadFromDatabase(Connection connection) throws SQLException {
        materials.clear();
        String query = "SELECT material_id, name, cost_per_gram FROM materials ORDER BY material_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Material material = new Material(
                    rs.getInt("material_id"),
                    rs.getString("name"),
                    rs.getBigDecimal("cost_per_gram")
                );
                materials.add(material);
            }
        }
    }
    
    public void clear() {
        materials.clear();
    }
} 