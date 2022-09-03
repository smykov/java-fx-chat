package ru.gb.smykov.javafxchat.server;

import ru.gb.smykov.javafxchat.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.gb.smykov.javafxchat.Command.*;

public class ChatServer {
    private final Map<String, ClientHandler> clients;

    public ChatServer() {
        this.clients = new HashMap<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new inDatabaseAuthService()
        ) {
            while (true) {
                System.out.println("Ожидаю подключения...");
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Клиент подключен!");
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(Command command, String message) {
        for (ClientHandler client : this.clients.values()) {
            client.sendMessage(command, message);
        }
    }

    public void subscribe(ClientHandler client) {
        clients.put(client.getNick(), client);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClientList();
    }

    private void broadcastClientList() {
        String nicks = clients.values().stream()
                .map(ClientHandler::getNick)
                .collect(Collectors.joining(" "));
        broadcast(CLIENTS, nicks);
    }

    public boolean isNickBusy(String nick) {
        return clients.get(nick) != null;
    }

    public void sendPrivateMessage(ClientHandler from, String nickTo, String message) {
        final ClientHandler clientTo = clients.get(nickTo);
        if (clientTo == null) {
            from.sendMessage(ERROR, "Пользователь '" + nickTo + "' не авторизован!");
            return;
        }
        clientTo.sendMessage(MESSAGE, "private from " + clientTo.getNick() + ": " + message);
        from.sendMessage(MESSAGE, "private to " + clientTo.getNick() + ": " + message);
    }

    public void changeNickname(ClientHandler currentUser, String newNickname) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/ru/gb/smykov/javafxchat/server/database.db");
            Statement statement = connection.createStatement();
            String updateQuery = String.format("update users set nickname = '%s' where nickname = '%s'", newNickname, currentUser.getNick());
            if (statement.executeUpdate(updateQuery) != 0) {
                currentUser.sendMessage(MESSAGE, "Ник изменен на " + newNickname);
                currentUser.setNick(newNickname);
                broadcastClientList();
            } else {
                currentUser.sendMessage(MESSAGE, "Ошибка изменения ника");
            }
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
