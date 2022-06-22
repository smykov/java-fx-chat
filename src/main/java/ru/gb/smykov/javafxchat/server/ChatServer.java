package ru.gb.smykov.javafxchat.server;

import ru.gb.smykov.javafxchat.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static ru.gb.smykov.javafxchat.Command.MESSAGE;

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

    public void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(MESSAGE, message);
        }
    }

    public void subscribe(ClientHandler client) {
        clients.put(client.getNick(), client);
    }

    public boolean isNickBusy(String nick) {
        return clients.get(nick) != null;
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client.getNick());
    }

    public void privateMessage(String receiverNick, String nick, String message) {
        for (ClientHandler client : clients.values()) {
            if (receiverNick.equals(client.getNick())) {
                client.sendMessage(MESSAGE, "private " + nick + ": " + message);
                break;
            }
        }
    }
}
