package org.example.controller;

import org.example.database.DatabaseManager;
import org.example.model.Material;
import org.example.model.Materials;

import java.sql.*;
import java.util.List;

public class MaterialsController {
    private static MaterialsController instance;
    private Materials materials;

    private MaterialsController() {
        materials = Materials.getInstance();
    }

    public static synchronized MaterialsController getInstance() {
        if (instance == null) {
            instance = new MaterialsController();
        }
        return instance;
    }

    public void loadMaterials() {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            materials.loadFromDatabase(connection);
        } catch (SQLException e) {
            System.err.println("Error loading materials: " + e.getMessage());
        }
    }

    public boolean addMaterial(Material material) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "INSERT INTO materials (name, cost_per_gram) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, material.getName());
                stmt.setBigDecimal(2, material.getCostPerGram());
                int result = stmt.executeUpdate();
                if (result > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            material.setMaterialId(rs.getInt(1));
                            materials.addMaterial(material);
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding material: " + e.getMessage());
        }
        return false;
    }

    public boolean updateMaterial(Material material) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "UPDATE materials SET name = ?, cost_per_gram = ? WHERE material_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, material.getName());
                stmt.setBigDecimal(2, material.getCostPerGram());
                stmt.setInt(3, material.getMaterialId());
                int result = stmt.executeUpdate();
                if (result > 0) {
                    loadMaterials();
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating material: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteMaterial(int materialId) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "DELETE FROM materials WHERE material_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, materialId);
                int result = stmt.executeUpdate();
                if (result > 0) {
                    materials.getMaterials().removeIf(material -> material.getMaterialId() == materialId);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting material: " + e.getMessage());
        }
        return false;
    }

    public Material getMaterialById(int materialId) {
        return materials.getMaterialById(materialId);
    }

    public List<Material> getAllMaterials() {
        return materials.getMaterials();
    }
} 