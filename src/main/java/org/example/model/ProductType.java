package org.example.model;

import java.math.BigDecimal;

public class ProductType {
    private int typeId;
    private String name;
    private BigDecimal laborCost;
    
    public ProductType(int typeId, String name, BigDecimal laborCost) {
        this.typeId = typeId;
        this.name = name;
        this.laborCost = laborCost;
    }
    
    public ProductType(String name, BigDecimal laborCost) {
        this.name = name;
        this.laborCost = laborCost;
    }
    
    // Геттеры
    public int getTypeId() {
        return typeId;
    }
    
    public String getName() {
        return name;
    }
    
    public BigDecimal getLaborCost() {
        return laborCost;
    }
    
    // Сеттеры
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setLaborCost(BigDecimal laborCost) {
        this.laborCost = laborCost;
    }
    
    @Override
    public String toString() {
        return name + " (" + laborCost + " ₽)";
    }
} 