package org.example.controller;

import org.example.database.DatabaseManager;
import org.example.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class OrdersController {
    private static OrdersController instance;
    private Orders orders;

    private OrdersController() {
        orders = Orders.getInstance();
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
            e.printStackTrace();
        }
    }

    public boolean addOrder(Order order) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "INSERT INTO orders (client_id, order_date, status, product_type_id, total_weight, price) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, order.getClient().getClientId());
                stmt.setDate(2, Date.valueOf(order.getOrderDate()));
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
                            addOrderMaterials(order, connection);
                            
                            // Если заказ сразу завершен, обновляем данные клиента
                            if (order.getStatus() == Order.Status.COMPLETED) {
                                updateClientPurchases(order.getClient().getClientId(), connection);
                            }
                            
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding order: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateOrder(Order order) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            // Получаем старый статус заказа
            Order.Status oldStatus = getOrderStatus(order.getOrderId(), connection);
            
            String query = "UPDATE orders SET client_id = ?, order_date = ?, status = ?, product_type_id = ?, total_weight = ?, price = ? WHERE order_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, order.getClient().getClientId());
                stmt.setDate(2, Date.valueOf(order.getOrderDate()));
                stmt.setString(3, order.getStatus().name());
                stmt.setInt(4, order.getProductType().getTypeId());
                stmt.setBigDecimal(5, order.getTotalWeight());
                stmt.setBigDecimal(6, order.getPrice());
                stmt.setInt(7, order.getOrderId());
                int result = stmt.executeUpdate();
                if (result > 0) {
                    // Обновляем материалы заказа
                    updateOrderMaterials(order, connection);
                    
                    // Проверяем изменение статуса
                    if (oldStatus != Order.Status.COMPLETED && order.getStatus() == Order.Status.COMPLETED) {
                        // Заказ стал завершенным - обновляем данные клиента
                        updateClientPurchases(order.getClient().getClientId(), connection);
                    } else if (oldStatus == Order.Status.COMPLETED && order.getStatus() != Order.Status.COMPLETED) {
                        // Заказ больше не завершен - пересчитываем данные клиента
                        updateClientPurchases(order.getClient().getClientId(), connection);
                    }
                    
                    loadOrders();
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating order: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteOrder(int orderId) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            // Получаем информацию о заказе перед удалением
            Order orderToDelete = getOrderById(orderId);
            
            String query = "DELETE FROM orders WHERE order_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, orderId);
                int result = stmt.executeUpdate();
                if (result > 0) {
                    orders.getOrders().removeIf(order -> order.getOrderId() == orderId);
                    
                    // Если удаляемый заказ был завершен, пересчитываем данные клиента
                    if (orderToDelete != null && orderToDelete.getStatus() == Order.Status.COMPLETED) {
                        updateClientPurchases(orderToDelete.getClient().getClientId(), connection);
                    }
                    
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Order getOrderById(int orderId) {
        return orders.getOrderById(orderId);
    }

    public List<Order> getAllOrders() {
        return orders.getOrders();
    }

    private void addOrderMaterials(Order order, Connection connection) throws SQLException {
        String query = "INSERT INTO order_materials (order_id, material_id, weight) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (OrderMaterial om : order.getMaterials()) {
                stmt.setInt(1, order.getOrderId());
                stmt.setInt(2, om.getMaterial().getMaterialId());
                stmt.setBigDecimal(3, om.getWeightGrams());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void updateOrderMaterials(Order order, Connection connection) throws SQLException {
        // Удаляем старые материалы
        String deleteQuery = "DELETE FROM order_materials WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            stmt.setInt(1, order.getOrderId());
            stmt.executeUpdate();
        }
        // Добавляем новые
        addOrderMaterials(order, connection);
    }
    
    private Order.Status getOrderStatus(int orderId, Connection connection) throws SQLException {
        String query = "SELECT status FROM orders WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Order.Status.valueOf(rs.getString("status"));
                }
            }
        }
        return null;
    }
    
    private void updateClientPurchases(int clientId, Connection connection) throws SQLException {
        // Вычисляем общую сумму завершенных покупок клиента
        String sumQuery = "SELECT COALESCE(SUM(price), 0) as total FROM orders " +
                         "WHERE client_id = ? AND status = 'COMPLETED'";
        
        java.math.BigDecimal totalPurchases = java.math.BigDecimal.ZERO;
        try (PreparedStatement sumStmt = connection.prepareStatement(sumQuery)) {
            sumStmt.setInt(1, clientId);
            try (ResultSet rs = sumStmt.executeQuery()) {
                if (rs.next()) {
                    totalPurchases = rs.getBigDecimal("total");
                }
            }
        }
        
        // Определяем скидку на основе суммы покупок
        int discount = 0;
        if (totalPurchases.compareTo(java.math.BigDecimal.valueOf(100000)) >= 0) {
            discount = 15;
        } else if (totalPurchases.compareTo(java.math.BigDecimal.valueOf(50000)) >= 0) {
            discount = 10;
        } else if (totalPurchases.compareTo(java.math.BigDecimal.valueOf(25000)) >= 0) {
            discount = 5;
        }
        
        // Обновляем данные клиента
        String updateQuery = "UPDATE clients SET total_purchases = ?, discount = ? WHERE client_id = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
            updateStmt.setBigDecimal(1, totalPurchases);
            updateStmt.setInt(2, discount);
            updateStmt.setInt(3, clientId);
            updateStmt.executeUpdate();
        }
        
        // Обновляем данные в контейнере клиентов
        ClientsController.getInstance().loadClients();
    }
} 