package org.example.controller;

import org.example.database.DatabaseManager;
import org.example.database.DataInitializer;
import org.example.model.User;
import org.example.model.UserContainer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер для управления пользователями
 * Обеспечивает CRUD операции, работу с БД и управление интерфейсом
 */
public class UsersController {
    private static UsersController instance;
    private UserContainer users;
    private TableView<User> tableView;
    
    private UsersController() {
        users = UserContainer.getInstance();
    }
    
    public static synchronized UsersController getInstance() {
        if (instance == null) {
            instance = new UsersController();
        }
        return instance;
    }
    
    /**
     * Устанавливает TableView для отображения данных
     */
    public void setTableView(TableView<User> tableView) {
        this.tableView = tableView;
        setupTableColumns();
        refreshTableView();
    }
    
    /**
     * Настраивает колонки таблицы
     */
    private void setupTableColumns() {
        if (tableView == null) return;
        
        // Настройка колонок будет выполнена в MainController
        // Здесь мы только обновляем данные
    }
    
    /**
     * Обновляет отображение таблицы
     */
    private void refreshTableView() {
        if (tableView != null) {
            ObservableList<User> userList = FXCollections.observableArrayList(users.getUsers());
            tableView.setItems(userList);
        }
    }
    
    /**
     * Загружает всех пользователей из базы данных
     */
    public void loadAllUsers() {
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            users.loadFromDatabase(connection);
            refreshTableView();
        } catch (SQLException e) {
            showError("Ошибка загрузки пользователей", e.getMessage());
        }
    }
    
    /**
     * Добавляет нового пользователя
     */
    public boolean addUser(User user) {
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            String sql = "INSERT INTO users (username, password_hash, role_id) VALUES (?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setInt(3, user.getRole().getRoleId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // Получаем ID нового пользователя
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
                users.getUsers().add(user);
                refreshTableView();
                return true;
            }
            return false;
        } catch (SQLException e) {
            showError("Ошибка добавления пользователя", e.getMessage());
            return false;
        }
    }
    
    /**
     * Редактирует выбранного пользователя
     */
    public void editUser() {
        User selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Предупреждение", "Выберите пользователя для редактирования");
            return;
        }
        
        try {
            // Здесь можно открыть диалог для редактирования пользователя
            showInfo("Редактирование пользователя", "Редактирование пользователя: " + selectedUser.getUsername());
        } catch (Exception e) {
            showError("Ошибка редактирования пользователя", e.getMessage());
        }
    }
    
    /**
     * Обновляет существующего пользователя
     */
    public boolean updateUser(User user) {
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            
            // Проверяем, изменился ли пароль
            User originalUser = getUserById(user.getUserId());
            String sql;
            PreparedStatement stmt;
            
            if (originalUser != null && user.getPasswordHash().equals(originalUser.getPasswordHash())) {
                // Пароль не изменился, обновляем только логин и роль
                sql = "UPDATE users SET username = ?, role_id = ? WHERE user_id = ?";
                stmt = connection.prepareStatement(sql);
                stmt.setString(1, user.getUsername());
                stmt.setInt(2, user.getRole().getRoleId());
                stmt.setInt(3, user.getUserId());
            } else {
                // Пароль изменился, обновляем все поля
                sql = "UPDATE users SET username = ?, password_hash = ?, role_id = ? WHERE user_id = ?";
                stmt = connection.prepareStatement(sql);
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPasswordHash());
                stmt.setInt(3, user.getRole().getRoleId());
                stmt.setInt(4, user.getUserId());
            }
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // Обновляем пользователя в списке
                for (int i = 0; i < users.getUsers().size(); i++) {
                    if (users.getUsers().get(i).getUserId() == user.getUserId()) {
                        users.getUsers().set(i, user);
                        break;
                    }
                }
                refreshTableView();
                return true;
            }
            return false;
        } catch (SQLException e) {
            showError("Ошибка обновления пользователя", e.getMessage());
            return false;
        }
    }
    
    /**
     * Удаляет пользователя по ID
     */
    public boolean deleteUser(int userId) {
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            String sql = "DELETE FROM users WHERE user_id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                users.getUsers().removeIf(user -> user.getUserId() == userId);
                refreshTableView();
                return true;
            }
            return false;
        } catch (SQLException e) {
            showError("Ошибка удаления пользователя", e.getMessage());
            return false;
        }
    }
    
    /**
     * Обновляет данные пользователей
     */
    public void refreshUsers() {
        loadAllUsers();
    }
    
    /**
     * Получает пользователя по ID
     */
    public User getUserById(int userId) {
        return users.getUsers().stream()
                .filter(user -> user.getUserId() == userId)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Получает пользователя по логину
     */
    public User getUserByUsername(String username) {
        return users.getUsers().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Получает всех пользователей
     */
    public List<User> getAllUsers() {
        return users.getUsers();
    }
    
    // Вспомогательные методы для отображения диалогов
    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 