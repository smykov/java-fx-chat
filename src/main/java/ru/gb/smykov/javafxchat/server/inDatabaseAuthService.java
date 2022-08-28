package ru.gb.smykov.javafxchat.server;

import java.io.IOException;
import java.sql.*;

public class inDatabaseAuthService implements AuthService {
    private final Connection connection;

    public inDatabaseAuthService() throws SQLException {
        connection = getConnection();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:src/main/resources/ru/gb/smykov/javafxchat/server/database.db");
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) throws SQLException {
        Statement statement = createStatement();
        String sql = String.format("select nickname from users where login = '%s' and password = '%s'", login, password);
        ResultSet resultSet = statement.executeQuery(sql);

        return resultSet.getString("nickname");
    }

    private Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    @Override
    public void close() {
        System.out.println("Сервис аутентификации остановлен!");
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
