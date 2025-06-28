package org.example.database;

import org.example.controller.*;
import org.example.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Класс для централизованной инициализации всех моделей из базы данных
 */
public class DataInitializer {
    private static DataInitializer instance;
    
    private DataInitializer() {}
    
    public static synchronized DataInitializer getInstance() {
        if (instance == null) {
            instance = new DataInitializer();
        }
        return instance;
    }
    
    /**
     * Инициализирует все модели из базы данных
     * @throws SQLException если произошла ошибка при загрузке данных
     */
    public void initializeAllData() throws SQLException {
        Connection connection = DatabaseManager.getInstance().getConnection();
        
        try {
            // Загружаем роли и пользователей
            initializeRolesAndUsers(connection);
            
            // Загружаем основные данные
            initializeClients(connection);
            initializeMaterials(connection);
            initializeProductTypes(connection);
            initializeOrders(connection);
            
            // Обновляем общие суммы покупок клиентов на основе их заказов
            updateClientsTotalPurchases();
            
        } catch (SQLException e) {
            System.err.println("Ошибка при инициализации данных: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Инициализирует роли и пользователей
     */
    public void initializeRolesAndUsers(Connection connection) throws SQLException {
        // Загружаем роли
        RoleContainer roleContainer = RoleContainer.getInstance();
        roleContainer.loadFromDatabase(connection);
        
        // Загружаем пользователей
        UserContainer userContainer = UserContainer.getInstance();
        userContainer.loadFromDatabase(connection);
    }
    
    /**
     * Инициализирует клиентов
     */
    public void initializeClients(Connection connection) throws SQLException {
        ClientsController clientsController = ClientsController.getInstance();
        clientsController.loadClients();
    }
    
    /**
     * Инициализирует материалы
     */
    public void initializeMaterials(Connection connection) throws SQLException {
        MaterialsController materialsController = MaterialsController.getInstance();
        materialsController.loadMaterials();
    }
    
    /**
     * Инициализирует типы изделий
     */
    public void initializeProductTypes(Connection connection) throws SQLException {
        ProductTypesController productTypesController = ProductTypesController.getInstance();
        productTypesController.loadProductTypes();
    }
    
    /**
     * Инициализирует заказы
     */
    public void initializeOrders(Connection connection) throws SQLException {
        OrdersController ordersController = OrdersController.getInstance();
        ordersController.loadOrders();
    }
    
    /**
     * Обновляет все данные из базы данных
     */
    public void refreshAllData() {
        try {
            initializeAllData();
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении данных: " + e.getMessage());
        }
    }
    
    /**
     * Очищает все данные из памяти
     */
    public void clearAllData() {
        RoleContainer.getInstance().clear();
        UserContainer.getInstance().clear();
        ClientContainer.getInstance().clear();
        MaterialContainer.getInstance().clear();
        ProductTypeContainer.getInstance().clear();
        OrderContainer.getInstance().clear();
    }
    
    /**
     * Обновляет общие суммы покупок всех клиентов
     */
    private void updateClientsTotalPurchases() {
        try {
            ClientsController clientsController = ClientsController.getInstance();
            clientsController.updateAllClientsTotalPurchases();
        } catch (Exception e) {
            System.err.println("Ошибка при обновлении общих сумм покупок клиентов: " + e.getMessage());
        }
    }
} 