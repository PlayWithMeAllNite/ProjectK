package org.example.controller;

import org.example.database.DatabaseManager;
import org.example.model.ProductType;
import org.example.model.ProductTypes;

import java.sql.*;
import java.util.List;

public class ProductTypesController {
    private static ProductTypesController instance;
    private ProductTypes productTypes;

    private ProductTypesController() {
        productTypes = ProductTypes.getInstance();
    }

    public static synchronized ProductTypesController getInstance() {
        if (instance == null) {
            instance = new ProductTypesController();
        }
        return instance;
    }

    public void loadProductTypes() {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            productTypes.loadFromDatabase(connection);
        } catch (SQLException e) {
            System.err.println("Error loading product types: " + e.getMessage());
        }
    }

    public boolean addProductType(ProductType productType) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "INSERT INTO product_types (name, labor_cost) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, productType.getName());
                stmt.setBigDecimal(2, productType.getLaborCost());
                int result = stmt.executeUpdate();
                if (result > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            productType.setTypeId(rs.getInt(1));
                            productTypes.addProductType(productType);
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding product type: " + e.getMessage());
        }
        return false;
    }

    public boolean updateProductType(ProductType productType) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "UPDATE product_types SET name = ?, labor_cost = ? WHERE type_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, productType.getName());
                stmt.setBigDecimal(2, productType.getLaborCost());
                stmt.setInt(3, productType.getTypeId());
                int result = stmt.executeUpdate();
                if (result > 0) {
                    loadProductTypes();
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating product type: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteProductType(int typeId) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "DELETE FROM product_types WHERE type_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, typeId);
                int result = stmt.executeUpdate();
                if (result > 0) {
                    productTypes.getProductTypes().removeIf(type -> type.getTypeId() == typeId);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting product type: " + e.getMessage());
        }
        return false;
    }

    public ProductType getProductTypeById(int typeId) {
        return productTypes.getProductTypeById(typeId);
    }

    public List<ProductType> getAllProductTypes() {
        return productTypes.getProductTypes();
    }
} 