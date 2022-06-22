package ru.gb.smykov.javafxchat.client;

import javafx.application.Platform;
import ru.gb.smykov.javafxchat.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static ru.gb.smykov.javafxchat.Command.*;

public class ChatClient {
    private final ChatController controller;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean userAuth = false;

    public void setUserAuth(boolean userAuth) {
        this.userAuth = userAuth;
        controller.authBox.setVisible(!userAuth);
        controller.messageBox.setVisible(userAuth);
    }
    public boolean isUserAuth() {
        return userAuth;
    }

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket("127.0.0.1", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        Thread threadMain = new Thread(() -> {
            try {
                waitAuth();
                readMessages();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        });
        Thread threadTimerToClose = new Thread(() -> {
            try {
                Thread.sleep(120_000);
                if (!isUserAuth()){
                    closeConnection();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        threadMain.start();
        threadTimerToClose.start();
    }

    private void waitAuth() throws IOException {
        while (true) {
            final String message = in.readUTF();
            final Command command = getCommand(message);
            final String[] params = command.parse(message);
            if (command == AUTHOK) {
                final String nick = params[0];
                setUserAuth(true);
                controller.addMessage("Успешная авторизация под ником " + nick);
                break;
            }
            if (command == ERROR) {
                Platform.runLater(() -> controller.showError(params[0]));
                continue;
            }
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            String message = in.readUTF();
            final Command command = getCommand(message);
            if (command == END) {
                break;
            }
            final String[] params = command.parse(message);
            final String commandMessage = params[0];
            if (command == ERROR) {
                Platform.runLater(() -> controller.showError(commandMessage));
                continue;
            }
            if (command == MESSAGE) {
                controller.addMessage(commandMessage);
            }
            if (command == CLIENTS) {
                controller.updateClientsList(params);
            }
        }
    }

    private void closeConnection() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }
}
