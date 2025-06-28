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
        DatabaseManager.getInstance();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Ювелирная мастерская - Вход в систему");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
} 