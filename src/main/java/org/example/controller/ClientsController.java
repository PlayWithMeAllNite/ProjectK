package org.example.controller;

import org.example.database.DatabaseManager;
import org.example.database.DataInitializer;
import org.example.model.Client;
import org.example.model.ClientContainer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.math.BigDecimal;

/**
 * Контроллер для управления клиентами
 * Обеспечивает CRUD операции, работу с БД и управление интерфейсом
 */
public class ClientsController {
    private static ClientsController instance;
    private ClientContainer clients;
    private TableView<Client> clientsTable;
    
    private ClientsController() {
        clients = ClientContainer.getInstance();
    }
    
    public static synchronized ClientsController getInstance() {
        if (instance == null) {
            instance = new ClientsController();
        }
        return instance;
    }
    
    // ==================== МЕТОДЫ РАБОТЫ С БАЗОЙ ДАННЫХ ====================
    
    /**
     * Загружает всех клиентов из базы данных
     */
    public void loadClients() {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            clients.loadFromDatabase(connection);
            System.out.println("Клиенты загружены из БД: " + clients.getClients().size() + " записей");
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки клиентов: " + e.getMessage());
            showError("Ошибка загрузки данных", "Не удалось загрузить клиентов из базы данных");
        }
    }
    
    /**
     * Обновляет данные клиентов через DataInitializer
     */
    public void refreshClients() {
        try {
            DataInitializer.getInstance().initializeClients(DatabaseManager.getInstance().getConnection());
            updateTableView();
        } catch (SQLException e) {
            System.err.println("Ошибка обновления клиентов: " + e.getMessage());
            showError("Ошибка обновления", "Не удалось обновить данные клиентов");
        }
    }
    
    /**
     * Добавляет нового клиента в базу данных
     */
    public boolean addClient(Client client) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "INSERT INTO clients (phone, full_name, email, total_purchases, discount) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
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
                            updateTableView();
                            showSuccess("Клиент добавлен", "Клиент " + client.getFullName() + " успешно добавлен");
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления клиента: " + e.getMessage());
            showError("Ошибка добавления", "Не удалось добавить клиента: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Обновляет данные клиента в базе данных
     */
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
                    // Обновляем клиента в списке
                    List<Client> clientList = clients.getClients();
                    for (int i = 0; i < clientList.size(); i++) {
                        if (clientList.get(i).getClientId() == client.getClientId()) {
                            clientList.set(i, client);
                            break;
                        }
                    }
                    updateTableView();
                    showSuccess("Клиент обновлен", "Данные клиента " + client.getFullName() + " успешно обновлены");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка обновления клиента: " + e.getMessage());
            showError("Ошибка обновления", "Не удалось обновить клиента: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Удаляет клиента из базы данных
     */
    public boolean deleteClient(int clientId) {
        Client client = getClientById(clientId);
        if (client == null) {
            showError("Ошибка удаления", "Клиент не найден");
            return false;
        }

        // Проверяем, есть ли у клиента заказы
        OrdersController ordersController = OrdersController.getInstance();
        List<org.example.model.Order> clientOrders = ordersController.getOrdersByClient(clientId);
        if (!clientOrders.isEmpty()) {
            showError("Ошибка удаления", "Нельзя удалить клиента, у которого есть заказы!");
            return false;
        }

        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "DELETE FROM clients WHERE client_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, clientId);

                int result = stmt.executeUpdate();
                if (result > 0) {
                    // Удаляем клиента из списка
                    List<Client> clientList = clients.getClients();
                    clientList.removeIf(c -> c.getClientId() == clientId);
                    updateTableView();
                    showSuccess("Клиент удален", "Клиент " + client.getFullName() + " успешно удален");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка удаления клиента: " + e.getMessage());
            showError("Ошибка удаления", "Не удалось удалить клиента: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Обновляет скидку клиента на основе суммы покупок
     */
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
            int discount = calculateDiscount(totalPurchases);
            
            // Обновляем скидку и общую сумму покупок
            String updateQuery = "UPDATE clients SET discount = ?, total_purchases = ? WHERE client_id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, discount);
                updateStmt.setBigDecimal(2, totalPurchases);
                updateStmt.setInt(3, clientId);
                updateStmt.executeUpdate();
            }
            
            // Обновляем данные в контейнере и интерфейсе
            loadClients();
            updateTableView();
            
            Client client = getClientById(clientId);
            if (client != null) {
                showSuccess("Скидка обновлена", 
                    "Скидка клиента " + client.getFullName() + " обновлена до " + discount + "%");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка обновления скидки клиента: " + e.getMessage());
            showError("Ошибка обновления скидки", "Не удалось обновить скидку клиента");
        }
    }
    
    // ==================== МЕТОДЫ УПРАВЛЕНИЯ ДАННЫМИ ====================
    
    public List<Client> getClients() {
        return clients.getClients();
    }
    
    public Client getClientById(int clientId) {
        return clients.getClientById(clientId);
    }
    
    public Client getClientByPhone(String phone) {
        return clients.getClientByPhone(phone);
    }
    
    /**
     * Вычисляет скидку на основе суммы покупок
     */
    private int calculateDiscount(BigDecimal totalPurchases) {
        if (totalPurchases.compareTo(BigDecimal.valueOf(100000)) >= 0) {
            return 15;
        } else if (totalPurchases.compareTo(BigDecimal.valueOf(50000)) >= 0) {
            return 10;
        } else if (totalPurchases.compareTo(BigDecimal.valueOf(25000)) >= 0) {
            return 5;
        }
        return 0;
    }
    
    // ==================== МЕТОДЫ УПРАВЛЕНИЯ ИНТЕРФЕЙСОМ ====================
    
    /**
     * Устанавливает таблицу для отображения клиентов
     */
    public void setTableView(TableView<Client> table) {
        this.clientsTable = table;
        updateTableView();
    }
    
    /**
     * Обновляет данные в таблице
     */
    public void updateTableView() {
        if (clientsTable != null) {
            ObservableList<Client> observableList = FXCollections.observableArrayList(clients.getClients());
            clientsTable.setItems(observableList);
        }
    }
    
    /**
     * Получает выбранного клиента из таблицы
     */
    public Client getSelectedClient() {
        if (clientsTable != null) {
            return clientsTable.getSelectionModel().getSelectedItem();
        }
        return null;
    }
    
    /**
     * Проверяет, выбран ли клиент в таблице
     */
    public boolean isClientSelected() {
        return getSelectedClient() != null;
    }
    
    /**
     * Очищает выбор в таблице
     */
    public void clearSelection() {
        if (clientsTable != null) {
            clientsTable.getSelectionModel().clearSelection();
        }
    }
    
    // ==================== МЕТОДЫ УВЕДОМЛЕНИЙ ====================
    
    /**
     * Показывает информационное сообщение
     */
    private void showSuccess(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Показывает сообщение об ошибке
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Показывает предупреждение
     */
    public void showWarning(String title, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 