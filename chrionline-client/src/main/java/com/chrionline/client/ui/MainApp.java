package com.chrionline.client.ui;

import com.chrionline.client.network.TCPClient;
import com.chrionline.client.network.UDPListener;
import com.chrionline.client.ui.views.HomeView;
import com.chrionline.client.util.UIUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class MainApp extends Application {
    private UDPListener udpListener;

    @Override
    public void start(Stage stage) throws Exception {
        try {
            TCPClient.getInstance().connect();
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
            Platform.exit();
            return;
        }

        udpListener = new UDPListener();
        Thread udpThread = new Thread(udpListener, "chrionline-udp-listener");
        udpThread.setDaemon(true);
        udpThread.start();

        NavigationManager.init(stage);
        NavigationManager.navigateTo(new HomeView());

        stage.setTitle("ChriOnline");
        stage.setWidth(1280);
        stage.setHeight(860);
        stage.setMinWidth(1180);
        stage.setMinHeight(760);
        stage.setResizable(true);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if (udpListener != null) {
            udpListener.stopListening();
        }
        TCPClient.getInstance().disconnect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
