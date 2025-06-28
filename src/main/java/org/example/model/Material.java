package org.example.model;

import java.math.BigDecimal;

public class Material {
    private int materialId;
    private String name;
    private BigDecimal costPerGram;
    
    public Material(int materialId, String name, BigDecimal costPerGram) {
        this.materialId = materialId;
        this.name = name;
        this.costPerGram = costPerGram;
    }
    
    public Material(String name, BigDecimal costPerGram) {
        this.name = name;
        this.costPerGram = costPerGram;
    }
    
    // Геттеры
    public int getMaterialId() {
        return materialId;
    }
    
    public String getName() {
        return name;
    }
    
    public BigDecimal getCostPerGram() {
        return costPerGram;
    }
    
    // Сеттеры
    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setCostPerGram(BigDecimal costPerGram) {
        this.costPerGram = costPerGram;
    }
    
    @Override
    public String toString() {
        return name + " (" + costPerGram + " ₽/г)";
    }
} 