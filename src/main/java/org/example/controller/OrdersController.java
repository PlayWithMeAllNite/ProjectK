package org.example.controller;

import org.example.database.DatabaseManager;
import org.example.model.Order;
import org.example.model.OrderContainer;
import org.example.model.OrderMaterial;
import org.example.model.Client;
import org.example.model.ClientContainer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.math.BigDecimal;

public class OrdersController {
    private static OrdersController instance;
    private OrderContainer orders;
    private TableView<Order> tableView;
    
    private OrdersController() {
        orders = OrderContainer.getInstance();
    }
    
    public static synchronized OrdersController getInstance() {
        if (instance == null) {
            instance = new OrdersController();
        }
        return instance;
    }
    
    /**
     * Устанавливает TableView для отображения данных
     */
    public void setTableView(TableView<Order> tableView) {
        this.tableView = tableView;
        refreshTableView();
    }
    
    /**
     * Обновляет отображение таблицы
     */
    public void updateTableView() {
        refreshTableView();
    }
    
    /**
     * Обновляет отображение таблицы
     */
    private void refreshTableView() {
        if (tableView != null) {
            ObservableList<Order> orderList = FXCollections.observableArrayList(orders.getOrders());
            tableView.setItems(orderList);
        }
    }
    
    public void loadOrders() {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            orders.loadFromDatabase(connection);
            refreshTableView();
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
                            
                            // Если заказ создается со статусом COMPLETED, обновляем общую сумму клиента
                            if (order.getStatus() == Order.Status.COMPLETED) {
                                updateClientTotalPurchases(order.getClient().getClientId(), BigDecimal.ZERO);
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
            // Получаем старый статус заказа для сравнения
            Order oldOrder = getOrderById(order.getOrderId());
            Order.Status oldStatus = oldOrder != null ? oldOrder.getStatus() : null;
            
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
                    
                    // Если статус заказа изменился, пересчитываем общую сумму клиента
                    if (oldStatus != order.getStatus()) {
                        updateClientTotalPurchases(order.getClient().getClientId(), BigDecimal.ZERO);
                    }
                    
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating order: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Обновляет общую сумму покупок клиента
     */
    private void updateClientTotalPurchases(int clientId, BigDecimal amountToAdd) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            // Пересчитываем общую сумму всех завершенных заказов клиента
            String sumQuery = "SELECT COALESCE(SUM(price), 0) as total FROM orders " +
                             "WHERE client_id = ? AND status = 'COMPLETED'";
            BigDecimal totalPurchases = BigDecimal.ZERO;
            
            try (PreparedStatement sumStmt = connection.prepareStatement(sumQuery)) {
                sumStmt.setInt(1, clientId);
                try (ResultSet rs = sumStmt.executeQuery()) {
                    if (rs.next()) {
                        totalPurchases = rs.getBigDecimal("total");
                        if (totalPurchases == null) {
                            totalPurchases = BigDecimal.ZERO;
                        }
                    }
                }
            }
            
            // Определяем новую скидку на основе суммы покупок
            int newDiscount = calculateDiscount(totalPurchases);
            
            // Обновляем сумму покупок и скидку клиента
            String updateQuery = "UPDATE clients SET total_purchases = ?, discount = ? WHERE client_id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setBigDecimal(1, totalPurchases);
                updateStmt.setInt(2, newDiscount);
                updateStmt.setInt(3, clientId);
                updateStmt.executeUpdate();
            }
            
            // Обновляем данные клиента в контейнере
            ClientContainer clientContainer = ClientContainer.getInstance();
            Client client = clientContainer.getClientById(clientId);
            if (client != null) {
                client.setTotalPurchases(totalPurchases);
                client.setDiscount(newDiscount);
            }
            
            System.out.println("Обновлена общая сумма клиента ID " + clientId + ": " + totalPurchases + " (скидка: " + newDiscount + "%)");
            
        } catch (SQLException e) {
            System.err.println("Error updating client total purchases: " + e.getMessage());
        }
    }
    
    /**
     * Вычисляет скидку клиента на основе суммы покупок
     */
    private int calculateDiscount(BigDecimal totalPurchases) {
        if (totalPurchases.compareTo(BigDecimal.valueOf(100000)) >= 0) {
            return 15; // 15% скидка при покупках от 100,000
        } else if (totalPurchases.compareTo(BigDecimal.valueOf(50000)) >= 0) {
            return 10; // 10% скидка при покупках от 50,000
        } else if (totalPurchases.compareTo(BigDecimal.valueOf(25000)) >= 0) {
            return 5;  // 5% скидка при покупках от 25,000
        } else {
            return 0;  // Без скидки
        }
    }
    
    public boolean deleteOrder(int orderId) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            // Получаем заказ перед удалением для проверки статуса
            Order orderToDelete = getOrderById(orderId);
            
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
                    // Если удаляемый заказ был завершен, пересчитываем общую сумму клиента
                    if (orderToDelete != null && orderToDelete.getStatus() == Order.Status.COMPLETED) {
                        updateClientTotalPurchases(orderToDelete.getClient().getClientId(), BigDecimal.ZERO);
                    }
                    
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