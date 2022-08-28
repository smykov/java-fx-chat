module com.example.javafxchat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports ru.gb.smykov.javafxchat.client;
    opens ru.gb.smykov.javafxchat.client to javafx.fxml;
}