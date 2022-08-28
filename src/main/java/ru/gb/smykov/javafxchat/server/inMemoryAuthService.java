package ru.gb.smykov.javafxchat.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class inMemoryAuthService implements AuthService {
    private final List<UserData> users;

    public inMemoryAuthService() {
        users = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            users.add(new UserData("nick" + i, "login" + i, "pass" + i));
        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (UserData user : users) {
            if (login.equals(user.getLogin()) && password.equals(user.getPassword())) {
                return user.getNick();
            }
        }
        return null;
    }

    @Override
    public void close() {
        System.out.println("Сервис аутентификации остановлен!");
    }
}
