package org.example.controller;

import org.example.database.DatabaseManager;
import org.example.database.DataInitializer;
import org.example.model.Material;
import org.example.model.MaterialContainer;
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
 * Контроллер для управления материалами
 * Обеспечивает CRUD операции, работу с БД и управление интерфейсом
 */
public class MaterialsController {
    private static MaterialsController instance;
    private MaterialContainer materials;
    private TableView<Material> materialsTable;

    private MaterialsController() {
        materials = MaterialContainer.getInstance();
    }

    public static synchronized MaterialsController getInstance() {
        if (instance == null) {
            instance = new MaterialsController();
        }
        return instance;
    }

    // ==================== МЕТОДЫ РАБОТЫ С БАЗОЙ ДАННЫХ ====================

    /**
     * Загружает материалы из базы данных
     */
    public void loadMaterials() {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            materials.loadFromDatabase(connection);
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки материалов: " + e.getMessage());
            showError("Ошибка загрузки данных", "Не удалось загрузить материалы из базы данных");
        }
    }

    /**
     * Добавляет новый материал в базу данных
     */
    public boolean addMaterial(Material material) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "INSERT INTO materials (name, cost_per_gram) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, material.getName());
                stmt.setBigDecimal(2, material.getCostPerGram());
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            material.setMaterialId(rs.getInt(1));
                            materials.addMaterial(material);
                            updateTableView();
                            showSuccess("Материал добавлен", "Материал " + material.getName() + " успешно добавлен");
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления материала: " + e.getMessage());
            showError("Ошибка добавления", "Не удалось добавить материал: " + e.getMessage());
        }
        return false;
    }

    public boolean updateMaterial(Material material) {
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "UPDATE materials SET name = ?, cost_per_gram = ? WHERE material_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, material.getName());
                stmt.setBigDecimal(2, material.getCostPerGram());
                stmt.setInt(3, material.getMaterialId());
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    // Обновляем материал в списке
                    List<Material> materialList = materials.getMaterials();
                    for (int i = 0; i < materialList.size(); i++) {
                        if (materialList.get(i).getMaterialId() == material.getMaterialId()) {
                            materialList.set(i, material);
                            break;
                        }
                    }
                    updateTableView();
                    showSuccess("Материал обновлен", "Данные материала " + material.getName() + " успешно обновлены");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка обновления материала: " + e.getMessage());
            showError("Ошибка обновления", "Не удалось обновить материал: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteMaterial(int materialId) {
        Material material = getMaterialById(materialId);
        if (material == null) {
            showError("Ошибка удаления", "Материал не найден");
            return false;
        }
        
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            String query = "DELETE FROM materials WHERE material_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, materialId);
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    // Удаляем материал из списка
                    List<Material> materialList = materials.getMaterials();
                    materialList.removeIf(m -> m.getMaterialId() == materialId);
                    updateTableView();
                    showSuccess("Материал удален", "Материал " + material.getName() + " успешно удален");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка удаления материала: " + e.getMessage());
            showError("Ошибка удаления", "Не удалось удалить материал: " + e.getMessage());
        }
        return false;
    }

    // ==================== МЕТОДЫ УПРАВЛЕНИЯ ДАННЫМИ ====================

    public List<Material> getMaterials() {
        return materials.getMaterials();
    }

    public Material getMaterialById(int materialId) {
        return materials.getMaterialById(materialId);
    }

    public Material getMaterialByName(String name) {
        return materials.getMaterialByName(name);
    }

    // ==================== МЕТОДЫ УПРАВЛЕНИЯ ИНТЕРФЕЙСОМ ====================

    /**
     * Устанавливает таблицу для отображения материалов
     */
    public void setTableView(TableView<Material> table) {
        this.materialsTable = table;
        updateTableView();
    }

    /**
     * Обновляет данные в таблице
     */
    public void updateTableView() {
        if (materialsTable != null) {
            ObservableList<Material> observableList = FXCollections.observableArrayList(materials.getMaterials());
            materialsTable.setItems(observableList);
        }
    }

    /**
     * Получает выбранный материал из таблицы
     */
    public Material getSelectedMaterial() {
        if (materialsTable != null) {
            return materialsTable.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    /**
     * Проверяет, выбран ли материал в таблице
     */
    public boolean isMaterialSelected() {
        return getSelectedMaterial() != null;
    }

    /**
     * Очищает выбор в таблице
     */
    public void clearSelection() {
        if (materialsTable != null) {
            materialsTable.getSelectionModel().clearSelection();
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