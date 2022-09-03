package ru.gb.smykov.javafxchat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatClientApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatClientApp.class.getResource("client-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 360);
        stage.setTitle("Chat javaFX!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}