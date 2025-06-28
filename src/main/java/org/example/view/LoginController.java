package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.controller.AuthController;

import java.io.IOException;

public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Введите логин и пароль");
            return;
        }
        
        AuthController authController = AuthController.getInstance();
        if (authController.login(username, password)) {
            try {
                // Загружаем главное окно
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
                Parent root = loader.load();
                
                MainController mainController = loader.getController();
                mainController.initialize();
                
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setTitle("Ювелирная мастерская - " + authController.getCurrentUser().getUsername());
                stage.setScene(new Scene(root));
                stage.setMaximized(true);
                stage.show();
                
            } catch (IOException e) {
                messageLabel.setText("Ошибка загрузки главного окна: " + e.getMessage());
            }
        } else {
            messageLabel.setText("Неверный логин или пароль");
            passwordField.clear();
        }
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
} 