package ru.gb.smykov.javafxchat.client;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class ChatController {
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField messageField;
    private final ChatClient client;
    private final SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

    public void clickExit() {
        System.exit(0);
    }

    public ChatController(ChatClient client) {
        this.client = new ChatClient(this);
        while (true) {
            try {
                client.openConnection();
                break;
            } catch (IOException e) {
                showNotification();
            }
        }
    }

    private void showNotification() {
        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Не могу подключиться к серверу.\n" +
                        "Проверьте, что сервер запущен и доступен!",
                new ButtonType("Попробовать снова", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        alert.setTitle("Ошибка подключения");
        Optional<ButtonType> answer = alert.showAndWait();
        Boolean isExit = answer.map(select -> select.getButtonData().isCancelButton()).orElse(false);
        if (isExit){
            System.exit(0);
        }

    }

    public void clickSendButton() {
        String message = messageField.getText();
        if (message.isBlank()) {
            return;
        }

        messageField.clear();

        client.sendMessage(message);
        messageField.clear();
        messageField.requestFocus();
    }

    public void addMessage(String message) {
        messageArea.appendText(getDate() + ": " + message + "\n");
    }

    private String getDate() {
        return formatForDateNow.format(new Date());
    }
}