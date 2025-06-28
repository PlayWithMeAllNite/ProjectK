package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.database.DatabaseManager;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Инициализация подключения к БД
        DatabaseManager.getInstance();
        
        // Загрузка главного окна
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();
        
        primaryStage.setTitle("Ювелирная мастерская");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 