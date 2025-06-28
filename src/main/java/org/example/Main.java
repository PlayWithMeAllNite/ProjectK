package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.database.DatabaseManager;
import org.example.database.DataInitializer;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Инициализация подключения к БД
        DatabaseManager.getInstance();
        
        // Инициализация всех данных из базы данных
        try {
            DataInitializer.getInstance().initializeAllData();
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации данных: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Загрузка экрана авторизации
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        
        primaryStage.setTitle("Ювелирная мастерская - Авторизация");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 