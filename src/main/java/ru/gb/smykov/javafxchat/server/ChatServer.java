package ru.gb.smykov.javafxchat.server;

import ru.gb.smykov.javafxchat.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static ru.gb.smykov.javafxchat.Command.MESSAGE;

public class ChatServer {
    private final List<ClientHandler> clients;

    public ChatServer() {
        this.clients = new ArrayList<>();
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
        for (ClientHandler client : clients) {
            client.sendMessage(MESSAGE, message);
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (nick.equals(client.getNick())) {
                return true;
            }
        }
        return false;
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public void privateMessage(String receiverNick, String nick, String message) {
        for (ClientHandler client : clients) {
            if (receiverNick.equals(client.getNick())) {
                client.sendMessage(MESSAGE, "private " + nick + ": " + message);
                break;
            }
        }
    }
}
