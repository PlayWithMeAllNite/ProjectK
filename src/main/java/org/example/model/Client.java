package org.example.model;

import java.math.BigDecimal;

public class Client {
    private int clientId;
    private String phone;
    private String fullName;
    private String email;
    private BigDecimal totalPurchases;
    private int discount;
    
    public Client(int clientId, String phone, String fullName, String email, 
                  BigDecimal totalPurchases, int discount) {
        this.clientId = clientId;
        this.phone = phone;
        this.fullName = fullName;
        this.email = email;
        this.totalPurchases = totalPurchases != null ? totalPurchases : BigDecimal.ZERO;
        this.discount = discount;
    }
    
    public Client(String phone, String fullName, String email) {
        this.phone = phone;
        this.fullName = fullName;
        this.email = email;
        this.totalPurchases = BigDecimal.ZERO;
        this.discount = 0;
    }
    
    // Геттеры
    public int getClientId() {
        return clientId;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public BigDecimal getTotalPurchases() {
        return totalPurchases;
    }
    
    public int getDiscount() {
        return discount;
    }
    
    // Сеттеры
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setTotalPurchases(BigDecimal totalPurchases) {
        this.totalPurchases = totalPurchases != null ? totalPurchases : BigDecimal.ZERO;
    }
    
    public void setDiscount(int discount) {
        this.discount = discount;
    }
    
    @Override
    public String toString() {
        return fullName + " (" + phone + ")";
    }
} 