package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
    private Label errorLabel;
    
    @FXML
    private Button loginButton;
    
    private AuthController authController;
    
    @FXML
    public void initialize() {
        authController = AuthController.getInstance();
        passwordField.setOnAction(event -> handleLogin());
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Пожалуйста, введите логин и пароль");
            return;
        }
        
        loginButton.setDisable(true);
        errorLabel.setText("");
        
        if (authController.authenticate(username, password)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
                Parent root = loader.load();
                MainController mainController = loader.getController();
                mainController.initialize();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Ювелирная мастерская - " + authController.getCurrentRole());
                stage.setMaximized(true);
            } catch (IOException e) {
                showError("Ошибка загрузки главного окна: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("Неверный логин или пароль");
            passwordField.clear();
            passwordField.requestFocus();
        }
        loginButton.setDisable(false);
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 