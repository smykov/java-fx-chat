package ru.gb.smykov.javafxchat.client;

import javafx.application.Platform;
import ru.gb.smykov.javafxchat.Command;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static ru.gb.smykov.javafxchat.Command.*;

public class ChatClient {
    private final ChatController controller;
    private Socket socket;
    private Path fileHistory;
    private OutputStream fout;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean userAuth = false;
    private String nickName;

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
                fout = Files.newOutputStream(fileHistory);
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
                if (!isUserAuth()) {
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
                nickName = params[0];
                setUserAuth(true);
                initFileHistory();
                String chatHistory = getChatHistory();
                controller.addMessage(chatHistory);
                controller.addMessage("Успешная авторизация под ником " + nickName);
                break;
            }
            if (command == ERROR) {
                Platform.runLater(() -> controller.showError(params[0]));
                continue;
            }
        }
    }

    private String getChatHistory() {
        String[] textArr;
        try {
            Path parent = fileHistory.getParent();
            if (!Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(fileHistory)) {
                Files.createFile(fileHistory);
            }
            textArr = Files.readString(fileHistory).split("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int indexStart = Math.max(textArr.length - 100, 0);
        int indexEnd = textArr.length;
        return String.join("\n", Arrays.copyOfRange(textArr, indexStart, indexEnd));
    }

    private void initFileHistory() {
        fileHistory = Path.of("history", "history_" + this.nickName + ".txt");
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

    public void closeConnection() {
        if (fout != null) {
            try {
                fout.write(controller.getMessageAreaText().getBytes(StandardCharsets.UTF_8));
                fout.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
