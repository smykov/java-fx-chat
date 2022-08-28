package ru.gb.smykov.javafxchat.server;

import ru.gb.smykov.javafxchat.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

import static ru.gb.smykov.javafxchat.Command.*;

public class ClientHandler {
    private final ChatServer server;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String nick;
    private final AuthService authService;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.socket = socket;
            this.server = server;
            this.authService = authService;
            this.in = new DataInputStream(this.socket.getInputStream());
            this.out = new DataOutputStream(this.socket.getOutputStream());
            new Thread(() -> {
                try {
                    authenticate();
                    readMessage();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void authenticate() {
        while (true) {
            try {
                final String message = in.readUTF();
                final Command command = Command.getCommand(message);
                if (command == Command.AUTH) {
                    final String[] params = command.parse(message);
                    String login = params[0];
                    String password = params[1];
                    final String nick = authService.getNickByLoginAndPassword(login, password);

                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage(ERROR, "Пользователь уже авторизован!");
                        }
                        sendMessage(AUTHOK, nick);
                        this.nick = nick;
                        server.subscribe(this);
                        server.broadcast(MESSAGE, "Пользователь " + nick + " зашел в чат");
                        break;
                    } else {
                        sendMessage(ERROR, "Неверные логин и пароль!");
                    }
                }
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() {
        sendMessage(END);
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
                server.unsubscribe(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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


    private void readMessage() {
        try {
            while (true) {
                String message = in.readUTF();
                Command command = Command.getCommand(message);
                String[] params = command.parse(message);

                if (Command.isCommand(params[0])) {
                    message = params[0];
                    command = getCommand(message);
                    params = command.parse(message);
                }
                if (command == MESSAGE) {
                    String clientMessage = params[0];
                    server.broadcast(MESSAGE, nick + ": " + clientMessage);
                    continue;
                }
                if (command == END) {
                    break;
                }
                if (command == PRIVATE_MESSAGE) {
                    final String receiverNick = params[0];
                    final String receiverMessage = params[1];
                    server.sendPrivateMessage(this, receiverNick, receiverMessage);
                }
                if (command == CHANGE_NICKNAME) {
                    String newNickname = params[0];
                    server.changeNickname(this, newNickname);
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
}
