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
    public void addUser() {
        try {
            // Здесь можно открыть диалог для добавления пользователя
            // Пока просто показываем сообщение
            showInfo("Добавление пользователя", "Функция добавления пользователя будет реализована позже");
        } catch (Exception e) {
            showError("Ошибка добавления пользователя", e.getMessage());
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
     * Удаляет выбранного пользователя
     */
    public void deleteUser() {
        User selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showWarning("Предупреждение", "Выберите пользователя для удаления");
            return;
        }
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление пользователя");
        alert.setContentText("Вы уверены, что хотите удалить пользователя '" + selectedUser.getUsername() + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                String sql = "DELETE FROM users WHERE user_id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, selectedUser.getUserId());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    users.getUsers().remove(selectedUser);
                    refreshTableView();
                    showInfo("Успех", "Пользователь успешно удален");
                } else {
                    showError("Ошибка", "Не удалось удалить пользователя");
                }
            } catch (SQLException e) {
                showError("Ошибка удаления пользователя", e.getMessage());
            }
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