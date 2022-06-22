package ru.gb.smykov.javafxchat.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.gb.smykov.javafxchat.Command;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import static ru.gb.smykov.javafxchat.Command.MESSAGE;

public class ChatController {
    @FXML
    private HBox authBox;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passField;
    @FXML
    private VBox messageBox;
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField messageField;
    private final ChatClient client;

    public void clickExit() {
        System.exit(0);
    }

    public ChatController() {
        this.client = new ChatClient(this);
        while (true) {
            try {
                client.openConnection();
                break;
            } catch (IOException e) {
                e.printStackTrace();
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
        if (isExit) {
            System.exit(0);
        }

    }

    public void clickSendButton() {
        String message = messageField.getText();
        if (message.isBlank()) {
            return;
        }

        client.sendMessage(MESSAGE, message);
        messageField.clear();
        messageField.requestFocus();
    }

    public void addMessage(String message) {
        messageArea.appendText(message + "\n");
    }

    public void setAuth(boolean success) {
        authBox.setVisible(!success);
        messageBox.setVisible(success);
    }

    public void signinBtnClick() {
        client.sendMessage(Command.AUTH, loginField.getText(), passField.getText());
    }

    public void showError(String errorMessage) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage,
                new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle("Error!");
        alert.showAndWait();
    }
}