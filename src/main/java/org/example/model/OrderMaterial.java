package org.example.model;

import java.math.BigDecimal;

public class OrderMaterial {
    private Order order;
    private Material material;
    private BigDecimal weightGrams;
    
    public OrderMaterial(Order order, Material material, BigDecimal weightGrams) {
        this.order = order;
        this.material = material;
        this.weightGrams = weightGrams;
    }
    
    public OrderMaterial(Material material, BigDecimal weightGrams) {
        this.material = material;
        this.weightGrams = weightGrams;
    }
    
    // Геттеры
    public Order getOrder() {
        return order;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public BigDecimal getWeightGrams() {
        return weightGrams;
    }
    
    public BigDecimal getTotalCost() {
        return material.getCostPerGram().multiply(weightGrams);
    }
    
    // Геттеры для отображения в таблице
    public String getMaterialName() {
        return material.getName();
    }
    
    public BigDecimal getWeight() {
        return weightGrams;
    }
    
    public BigDecimal getCostPerGram() {
        return material.getCostPerGram();
    }
    
    // Сеттеры
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public void setMaterial(Material material) {
        this.material = material;
    }
    
    public void setWeightGrams(BigDecimal weightGrams) {
        this.weightGrams = weightGrams;
    }
    
    @Override
    public String toString() {
        return material.getName() + " - " + weightGrams + "г (" + getTotalCost() + " ₽)";
    }
} 