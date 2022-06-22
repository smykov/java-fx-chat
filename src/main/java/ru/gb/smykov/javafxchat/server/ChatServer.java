package ru.gb.smykov.javafxchat.server;

import ru.gb.smykov.javafxchat.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
             AuthService authService = new inMemoryAuthService()
        ) {
            while (true) {
                System.out.println("Ожидаю подключения...");
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Клиент подключен!");
            }
        } catch (IOException e) {
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
        clientTo.sendMessage(MESSAGE, "private from" + clientTo.getNick() + ": " + message);
        from.sendMessage(MESSAGE, "private to" + clientTo.getNick() + ": " + message);
    }
}
