package ru.gb.smykov.javafxchat.server;

import java.io.Closeable;
import java.sql.SQLException;

public interface AuthService extends Closeable {
    String getNickByLoginAndPassword(String login, String password) throws SQLException;

    class UserData {
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
}
