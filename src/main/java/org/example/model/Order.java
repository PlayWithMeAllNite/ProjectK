package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order {
    public enum Status {
        IN_PROCESS("В процессе"),
        READY("Готов"),
        COMPLETED("Завершён"),
        CANCELLED("Отменён");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    private int orderId;
    private Client client;
    private LocalDate orderDate;
    private Status status;
    private ProductType productType;
    private BigDecimal totalWeight;
    private BigDecimal price;
    private List<OrderMaterial> materials;
    
    public Order(int orderId, Client client, LocalDate orderDate, Status status, 
                 ProductType productType, BigDecimal totalWeight, BigDecimal price) {
        this.orderId = orderId;
        this.client = client;
        this.orderDate = orderDate;
        this.status = status;
        this.productType = productType;
        this.totalWeight = totalWeight;
        this.price = price;
        this.materials = new ArrayList<>();
    }
    
    public Order(Client client, ProductType productType) {
        this.client = client;
        this.orderDate = LocalDate.now();
        this.status = Status.IN_PROCESS;
        this.productType = productType;
        this.totalWeight = BigDecimal.ZERO;
        this.price = BigDecimal.ZERO;
        this.materials = new ArrayList<>();
    }
    
    // Геттеры
    public int getOrderId() {
        return orderId;
    }
    
    public Client getClient() {
        return client;
    }
    
    public LocalDate getOrderDate() {
        return orderDate;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public ProductType getProductType() {
        return productType;
    }
    
    public BigDecimal getTotalWeight() {
        return totalWeight;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public List<OrderMaterial> getMaterials() {
        return materials;
    }
    
    public BigDecimal getTotalWithDiscount() {
        if (client != null && client.getDiscount() > 0) {
            return price.multiply(BigDecimal.valueOf(1 - client.getDiscount() / 100.0));
        }
        return price;
    }
    
    // Сеттеры
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public void setClient(Client client) {
        this.client = client;
    }
    
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public void setProductType(ProductType productType) {
        this.productType = productType;
    }
    
    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public void setMaterials(List<OrderMaterial> materials) {
        this.materials = materials;
    }
    
    public void addMaterial(OrderMaterial material) {
        this.materials.add(material);
    }
    
    public void removeMaterial(OrderMaterial material) {
        this.materials.remove(material);
    }
    
    @Override
    public String toString() {
        return "Заказ №" + orderId + " - " + client.getFullName() + " (" + status + ")";
    }
} 