package ru.gb.smykov.javafxchat.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class inMemoryAuthService implements AuthService {
    private static class UserData {
        private final String nick;
        private final String login;
        private final String password;

        public UserData(String nick, String login, String password) {
            this.nick = nick;
            this.login = login;
            this.password = password;
        }

        public String getNick() {
            return nick;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }

    private final List<UserData> users;

    public inMemoryAuthService() {
        users = new ArrayList<UserData>();
        for (int i = 1; i <= 5; i++) {
            users.add(new UserData("nick" + i, "login" + i, "pass" + i));
        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (UserData user : users) {
            if (login.equals(user.getLogin()) && password.equals(user.getPassword())){
                return user.getNick();
            }
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        System.out.println("Сервис аутентификации остановлен!");
    }
}
