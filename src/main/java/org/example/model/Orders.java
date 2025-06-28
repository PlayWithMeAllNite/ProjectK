package org.example.model;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Orders {
    private static Orders instance;
    private List<Order> orders;
    
    private Orders() {
        orders = new ArrayList<>();
    }
    
    public static synchronized Orders getInstance() {
        if (instance == null) {
            instance = new Orders();
        }
        return instance;
    }
    
    public List<Order> getOrders() {
        return orders;
    }
    
    public void addOrder(Order order) {
        orders.add(order);
    }
    
    public Order getOrderById(int orderId) {
        for (Order order : orders) {
            if (order.getOrderId() == orderId) {
                return order;
            }
        }
        return null;
    }
    
    public List<Order> getOrdersByClient(int clientId) {
        List<Order> clientOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getClient().getClientId() == clientId) {
                clientOrders.add(order);
            }
        }
        return clientOrders;
    }
    
    public void loadFromDatabase(Connection connection) throws SQLException {
        orders.clear();
        String query = "SELECT o.order_id, o.client_id, o.order_date, o.status, o.product_type_id, " +
                      "o.total_weight, o.price, " +
                      "c.phone, c.full_name, c.email, c.total_purchases, c.discount, " +
                      "pt.name as type_name, pt.labor_cost " +
                      "FROM orders o " +
                      "JOIN clients c ON o.client_id = c.client_id " +
                      "JOIN product_types pt ON o.product_type_id = pt.type_id " +
                      "ORDER BY o.order_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Client client = new Client(
                    rs.getInt("client_id"),
                    rs.getString("phone"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getBigDecimal("total_purchases"),
                    rs.getInt("discount")
                );
                
                ProductType productType = new ProductType(
                    rs.getInt("product_type_id"),
                    rs.getString("type_name"),
                    rs.getBigDecimal("labor_cost")
                );
                
                Order order = new Order(
                    rs.getInt("order_id"),
                    client,
                    rs.getDate("order_date").toLocalDate(),
                    Order.Status.valueOf(rs.getString("status")),
                    productType,
                    rs.getBigDecimal("total_weight"),
                    rs.getBigDecimal("price")
                );
                
                orders.add(order);
            }
        }
        
        // Загружаем материалы для каждого заказа
        loadOrderMaterials(connection);
    }
    
    private void loadOrderMaterials(Connection connection) throws SQLException {
        String query = "SELECT om.order_id, om.material_id, om.weight, " +
                      "m.name, m.cost_per_gram " +
                      "FROM order_materials om " +
                      "JOIN materials m ON om.material_id = m.material_id " +
                      "ORDER BY om.order_id, om.material_id";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                Order order = getOrderById(orderId);
                if (order != null) {
                    Material material = new Material(
                        rs.getInt("material_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("cost_per_gram")
                    );
                    OrderMaterial orderMaterial = new OrderMaterial(
                        order,
                        material,
                        rs.getBigDecimal("weight")
                    );
                    order.addMaterial(orderMaterial);
                }
            }
        }
    }
    
    public void clear() {
        orders.clear();
    }
} 