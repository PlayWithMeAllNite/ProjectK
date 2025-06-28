package org.example.model;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductTypes {
    private static ProductTypes instance;
    private List<ProductType> productTypes;
    
    private ProductTypes() {
        productTypes = new ArrayList<>();
    }
    
    public static synchronized ProductTypes getInstance() {
        if (instance == null) {
            instance = new ProductTypes();
        }
        return instance;
    }
    
    public List<ProductType> getProductTypes() {
        return productTypes;
    }
    
    public void addProductType(ProductType productType) {
        productTypes.add(productType);
    }
    
    public ProductType getProductTypeById(int typeId) {
        for (ProductType productType : productTypes) {
            if (productType.getTypeId() == typeId) {
                return productType;
            }
        }
        return null;
    }
    
    public ProductType getProductTypeByName(String name) {
        for (ProductType productType : productTypes) {
            if (productType.getName().equals(name)) {
                return productType;
            }
        }
        return null;
    }
    
    public void loadFromDatabase(Connection connection) throws SQLException {
        productTypes.clear();
        String query = "SELECT type_id, name, labor_cost FROM product_types ORDER BY type_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                ProductType productType = new ProductType(
                    rs.getInt("type_id"),
                    rs.getString("name"),
                    rs.getBigDecimal("labor_cost")
                );
                productTypes.add(productType);
            }
        }
    }
    
    public void clear() {
        productTypes.clear();
    }
} 