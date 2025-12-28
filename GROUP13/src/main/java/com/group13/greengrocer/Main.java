package com.group13.greengrocer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;
    public static final int DEFAULT_WIDTH = 960;
    public static final int DEFAULT_HEIGHT = 540;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        stage.setTitle("Group13 GreenGrocer - Login");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setMinWidth(800);
        stage.setMinHeight(450);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
