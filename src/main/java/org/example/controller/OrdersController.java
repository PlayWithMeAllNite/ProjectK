package org.example.controller;

import org.example.database.DatabaseManager;
import org.example.model.Order;
import org.example.model.OrderContainer;
import org.example.model.OrderMaterial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class OrdersController {
    private static OrdersController instance;
    private OrderContainer orders;
    
    private OrdersController() {
        orders = OrderContainer.getInstance();
    }
    
    public static synchronized OrdersController getInstance() {
        if (instance == null) {
            instance = new OrdersController();
        }
        return instance;
    }
    
    public void loadOrders() {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            orders.loadFromDatabase(connection);
        } catch (SQLException e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }
    }
    
    public boolean addOrder(Order order) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "INSERT INTO orders (client_id, order_date, status, product_type_id, total_weight, price) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, order.getClient().getClientId());
                stmt.setDate(2, java.sql.Date.valueOf(order.getOrderDate()));
                stmt.setString(3, order.getStatus().name());
                stmt.setInt(4, order.getProductType().getTypeId());
                stmt.setBigDecimal(5, order.getTotalWeight());
                stmt.setBigDecimal(6, order.getPrice());
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            order.setOrderId(rs.getInt(1));
                            orders.addOrder(order);
                            
                            // Добавляем материалы заказа
                            for (var material : order.getMaterials()) {
                                addOrderMaterial(order.getOrderId(), material);
                            }
                            
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding order: " + e.getMessage());
        }
        return false;
    }
    
    private boolean addOrderMaterial(int orderId, OrderMaterial orderMaterial) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "INSERT INTO order_materials (order_id, material_id, weight) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, orderMaterial.getMaterial().getMaterialId());
                stmt.setBigDecimal(3, orderMaterial.getWeightGrams());
                
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error adding order material: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateOrder(Order order) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "UPDATE orders SET client_id = ?, order_date = ?, status = ?, product_type_id = ?, total_weight = ?, price = ? WHERE order_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, order.getClient().getClientId());
                stmt.setDate(2, java.sql.Date.valueOf(order.getOrderDate()));
                stmt.setString(3, order.getStatus().name());
                stmt.setInt(4, order.getProductType().getTypeId());
                stmt.setBigDecimal(5, order.getTotalWeight());
                stmt.setBigDecimal(6, order.getPrice());
                stmt.setInt(7, order.getOrderId());
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    // Обновляем заказ в списке
                    List<Order> orderList = orders.getOrders();
                    for (int i = 0; i < orderList.size(); i++) {
                        if (orderList.get(i).getOrderId() == order.getOrderId()) {
                            orderList.set(i, order);
                            break;
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating order: " + e.getMessage());
        }
        return false;
    }
    
    public boolean deleteOrder(int orderId) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            // Сначала удаляем материалы заказа
            String deleteMaterialsQuery = "DELETE FROM order_materials WHERE order_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteMaterialsQuery)) {
                stmt.setInt(1, orderId);
                stmt.executeUpdate();
            }
            
            // Затем удаляем сам заказ
            String query = "DELETE FROM orders WHERE order_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, orderId);
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    // Удаляем заказ из списка
                    List<Order> orderList = orders.getOrders();
                    orderList.removeIf(order -> order.getOrderId() == orderId);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
        }
        return false;
    }
    
    public List<Order> getOrders() {
        return orders.getOrders();
    }
    
    public Order getOrderById(int orderId) {
        return orders.getOrderById(orderId);
    }
    
    public List<Order> getOrdersByClient(int clientId) {
        return orders.getOrdersByClient(clientId);
    }
} 