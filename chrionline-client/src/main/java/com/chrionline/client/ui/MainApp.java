package com.chrionline.client.ui;
import com.chrionline.client.network.TCPClient;
import com.chrionline.client.network.UDPListener;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        TCPClient.getInstance().connect();
        new Thread(new UDPListener()).start();
        VBox root = new VBox(new Label("ChriOnline — En construction"));
        stage.setScene(new Scene(root, 900, 600));
        stage.setTitle("ChriOnline");
        stage.show();
    }
    @Override
    public void stop() throws Exception { TCPClient.getInstance().disconnect(); }
    public static void main(String[] args) { launch(args); }
}
