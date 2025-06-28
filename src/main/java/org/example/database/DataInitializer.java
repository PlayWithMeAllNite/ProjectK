package org.example.database;

import org.example.controller.*;
import org.example.model.*;

import java.sql.Connection;
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
            
            System.out.println("Все данные успешно загружены из базы данных");
            
        } catch (SQLException e) {
            System.err.println("Ошибка при инициализации данных: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Инициализирует роли и пользователей
     */
    public void initializeRolesAndUsers(Connection connection) throws SQLException {
        System.out.println("Загрузка ролей и пользователей...");
        
        // Загружаем роли
        RoleContainer roleContainer = RoleContainer.getInstance();
        roleContainer.loadFromDatabase(connection);
        
        // Загружаем пользователей
        UserContainer userContainer = UserContainer.getInstance();
        userContainer.loadFromDatabase(connection);
        
        System.out.println("Загружено ролей: " + roleContainer.getRoles().size());
        System.out.println("Загружено пользователей: " + userContainer.getUsers().size());
    }
    
    /**
     * Инициализирует клиентов
     */
    public void initializeClients(Connection connection) throws SQLException {
        System.out.println("Загрузка клиентов...");
        
        ClientsController clientsController = ClientsController.getInstance();
        clientsController.loadClients();
        
        System.out.println("Загружено клиентов: " + clientsController.getClients().size());
    }
    
    /**
     * Инициализирует материалы
     */
    public void initializeMaterials(Connection connection) throws SQLException {
        System.out.println("Загрузка материалов...");
        
        MaterialsController materialsController = MaterialsController.getInstance();
        materialsController.loadMaterials();
        
        System.out.println("Загружено материалов: " + materialsController.getMaterials().size());
    }
    
    /**
     * Инициализирует типы изделий
     */
    public void initializeProductTypes(Connection connection) throws SQLException {
        System.out.println("Загрузка типов изделий...");
        
        ProductTypesController productTypesController = ProductTypesController.getInstance();
        productTypesController.loadProductTypes();
        
        System.out.println("Загружено типов изделий: " + productTypesController.getProductTypes().size());
    }
    
    /**
     * Инициализирует заказы
     */
    public void initializeOrders(Connection connection) throws SQLException {
        System.out.println("Загрузка заказов...");
        
        OrdersController ordersController = OrdersController.getInstance();
        ordersController.loadOrders();
        
        System.out.println("Загружено заказов: " + ordersController.getOrders().size());
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
        System.out.println("Очистка всех данных из памяти...");
        
        RoleContainer.getInstance().clear();
        UserContainer.getInstance().clear();
        ClientContainer.getInstance().clear();
        MaterialContainer.getInstance().clear();
        ProductTypeContainer.getInstance().clear();
        OrderContainer.getInstance().clear();
        
        System.out.println("Все данные очищены");
    }
} 