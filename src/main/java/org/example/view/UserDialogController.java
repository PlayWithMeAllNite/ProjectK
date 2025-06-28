package org.example.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.Role;
import org.example.model.User;
import org.example.controller.RolesController;
import org.example.util.PasswordUtils;

public class UserDialogController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private Button okButton;
    
    private Stage dialogStage;
    private User user;
    private boolean okClicked = false;
    private RolesController rolesController;
    
    @FXML
    private void initialize() {
        rolesController = RolesController.getInstance();
        loadRoles();
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setUser(User user) {
        this.user = user;
        
        if (user != null) {
            // Редактирование существующего пользователя
            usernameField.setText(user.getUsername());
            passwordField.clear(); // Очищаем поле пароля для редактирования
            roleComboBox.setValue(user.getRole());
        } else {
            // Добавление нового пользователя
            usernameField.clear();
            passwordField.clear();
            roleComboBox.setValue(null);
        }
    }
    
    private void loadRoles() {
        ObservableList<Role> roles = FXCollections.observableArrayList(rolesController.getRoles());
        roleComboBox.setItems(roles);
    }
    
    public boolean isOkClicked() {
        return okClicked;
    }
    
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            if (user == null) {
                // Создание нового пользователя с хешированием
                user = new User(usernameField.getText(), PasswordUtils.hashPassword(passwordField.getText()), roleComboBox.getValue());
            } else {
                // Редактирование существующего пользователя
                user.setUsername(usernameField.getText());
                user.setRole(roleComboBox.getValue());
                String newPassword = passwordField.getText().trim();
                if (!newPassword.isEmpty()) {
                    user.setPasswordHash(PasswordUtils.hashPassword(newPassword));
                }
            }
            okClicked = true;
            dialogStage.close();
        }
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private boolean isInputValid() {
        String errorMessage = "";
        
        if (usernameField.getText() == null || usernameField.getText().trim().length() == 0) {
            errorMessage += "Логин не может быть пустым!\n";
        }
        
        // Для нового пользователя пароль обязателен
        if (user == null && (passwordField.getText() == null || passwordField.getText().trim().length() == 0)) {
            errorMessage += "Пароль не может быть пустым для нового пользователя!\n";
        }
        
        if (roleComboBox.getValue() == null) {
            errorMessage += "Необходимо выбрать роль!\n";
        }
        
        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка валидации");
            alert.setHeaderText("Пожалуйста, исправьте ошибки:");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
    
    public User getUser() {
        return user;
    }
} 