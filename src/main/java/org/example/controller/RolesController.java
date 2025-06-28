package org.example.controller;

import org.example.database.DatabaseManager;
import org.example.database.DataInitializer;
import org.example.model.Role;
import org.example.model.RoleContainer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер для управления ролями
 * Обеспечивает CRUD операции, работу с БД и управление интерфейсом
 */
public class RolesController {
    private static RolesController instance;
    private RoleContainer roles;
    private TableView<Role> tableView;
    
    private RolesController() {
        roles = RoleContainer.getInstance();
    }
    
    public static synchronized RolesController getInstance() {
        if (instance == null) {
            instance = new RolesController();
        }
        return instance;
    }
    
    /**
     * Устанавливает TableView для отображения данных
     */
    public void setTableView(TableView<Role> tableView) {
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
            ObservableList<Role> roleList = FXCollections.observableArrayList(roles.getRoles());
            tableView.setItems(roleList);
        }
    }
    
    /**
     * Загружает все роли из базы данных
     */
    public void loadAllRoles() {
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            roles.loadFromDatabase(connection);
            refreshTableView();
        } catch (SQLException e) {
            showError("Ошибка загрузки ролей", e.getMessage());
        }
    }
    
    /**
     * Добавляет новую роль
     */
    public void addRole() {
        try {
            // Здесь можно открыть диалог для добавления роли
            // Пока просто показываем сообщение
            showInfo("Добавление роли", "Функция добавления роли будет реализована позже");
        } catch (Exception e) {
            showError("Ошибка добавления роли", e.getMessage());
        }
    }
    
    /**
     * Редактирует выбранную роль
     */
    public void editRole() {
        Role selectedRole = tableView.getSelectionModel().getSelectedItem();
        if (selectedRole == null) {
            showWarning("Предупреждение", "Выберите роль для редактирования");
            return;
        }
        
        try {
            // Здесь можно открыть диалог для редактирования роли
            showInfo("Редактирование роли", "Редактирование роли: " + selectedRole.getRoleName());
        } catch (Exception e) {
            showError("Ошибка редактирования роли", e.getMessage());
        }
    }
    
    /**
     * Удаляет выбранную роль
     */
    public void deleteRole() {
        Role selectedRole = tableView.getSelectionModel().getSelectedItem();
        if (selectedRole == null) {
            showWarning("Предупреждение", "Выберите роль для удаления");
            return;
        }
        
        // Проверяем, не используется ли роль пользователями
        if (isRoleUsedByUsers(selectedRole.getRoleId())) {
            showError("Ошибка", "Нельзя удалить роль, которая используется пользователями");
            return;
        }
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление роли");
        alert.setContentText("Вы уверены, что хотите удалить роль '" + selectedRole.getRoleName() + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                String sql = "DELETE FROM roles WHERE role_id = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setInt(1, selectedRole.getRoleId());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    roles.getRoles().remove(selectedRole);
                    refreshTableView();
                    showInfo("Успех", "Роль успешно удалена");
                } else {
                    showError("Ошибка", "Не удалось удалить роль");
                }
            } catch (SQLException e) {
                showError("Ошибка удаления роли", e.getMessage());
            }
        }
    }
    
    /**
     * Проверяет, используется ли роль пользователями
     */
    private boolean isRoleUsedByUsers(int roleId) {
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT COUNT(*) FROM users WHERE role_id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, roleId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            showError("Ошибка проверки роли", e.getMessage());
        }
        return false;
    }
    
    /**
     * Обновляет данные ролей
     */
    public void refreshRoles() {
        loadAllRoles();
    }
    
    /**
     * Получает роль по ID
     */
    public Role getRoleById(int roleId) {
        return roles.getRoles().stream()
                .filter(role -> role.getRoleId() == roleId)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Получает роль по названию
     */
    public Role getRoleByName(String roleName) {
        return roles.getRoles().stream()
                .filter(role -> role.getRoleName().equals(roleName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Получает все роли
     */
    public List<Role> getAllRoles() {
        return roles.getRoles();
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