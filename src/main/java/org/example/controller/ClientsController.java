package org.example.controller;

import org.example.database.DatabaseManager;
import org.example.model.Client;
import org.example.model.Clients;

import java.sql.*;
import java.util.List;
import java.math.BigDecimal;

public class ClientsController {
    private static ClientsController instance;
    private Clients clients;
    
    private ClientsController() {
        clients = Clients.getInstance();
    }
    
    public static synchronized ClientsController getInstance() {
        if (instance == null) {
            instance = new ClientsController();
        }
        return instance;
    }
    
    public void loadClients() {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            clients.loadFromDatabase(connection);
        } catch (SQLException e) {
            System.err.println("Error loading clients: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean addClient(Client client) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "INSERT INTO clients (phone, full_name, email, total_purchases, discount) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, client.getPhone());
                stmt.setString(2, client.getFullName());
                stmt.setString(3, client.getEmail());
                stmt.setBigDecimal(4, client.getTotalPurchases());
                stmt.setInt(5, client.getDiscount());
                int result = stmt.executeUpdate();
                if (result > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            client.setClientId(rs.getInt(1));
                            clients.addClient(client);
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding client: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateClient(Client client) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "UPDATE clients SET phone = ?, full_name = ?, email = ?, total_purchases = ?, discount = ? WHERE client_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, client.getPhone());
                stmt.setString(2, client.getFullName());
                stmt.setString(3, client.getEmail());
                stmt.setBigDecimal(4, client.getTotalPurchases());
                stmt.setInt(5, client.getDiscount());
                stmt.setInt(6, client.getClientId());
                int result = stmt.executeUpdate();
                if (result > 0) {
                    loadClients();
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating client: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean deleteClient(int clientId) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "DELETE FROM clients WHERE client_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, clientId);
                int result = stmt.executeUpdate();
                if (result > 0) {
                    clients.getClients().removeIf(client -> client.getClientId() == clientId);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting client: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public Client getClientById(int clientId) {
        return clients.getClientById(clientId);
    }
    
    public List<Client> getAllClients() {
        return clients.getClients();
    }
    
    public Client getClientByPhone(String phone) {
        return clients.getClientByPhone(phone);
    }
    
    public void updateClientDiscount(int clientId) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            // Вычисляем общую сумму покупок
            String sumQuery = "SELECT COALESCE(SUM(price), 0) as total FROM orders " +
                             "WHERE client_id = ? AND status = 'COMPLETED'";
            
            BigDecimal totalPurchases = BigDecimal.ZERO;
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
            if (totalPurchases.compareTo(BigDecimal.valueOf(100000)) >= 0) {
                discount = 15;
            } else if (totalPurchases.compareTo(BigDecimal.valueOf(50000)) >= 0) {
                discount = 10;
            } else if (totalPurchases.compareTo(BigDecimal.valueOf(25000)) >= 0) {
                discount = 5;
            }
            
            // Обновляем скидку и общую сумму покупок
            String updateQuery = "UPDATE clients SET discount = ?, total_purchases = ? WHERE client_id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, discount);
                updateStmt.setBigDecimal(2, totalPurchases);
                updateStmt.setInt(3, clientId);
                updateStmt.executeUpdate();
            }
            
            // Обновляем данные в контейнере
            loadClients();
        } catch (SQLException e) {
            System.err.println("Error updating client discount: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Clients getClients() {
        return clients;
    }
} 