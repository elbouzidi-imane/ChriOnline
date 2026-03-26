package com.chrionline.client.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public final class NavigationManager {
    private static Scene scene;

    private NavigationManager() {
    }

    public static void init(Stage stage) {
        scene = new Scene(new StackPane(), 1280, 860);
        stage.setScene(scene);
    }

    public static void navigateTo(Parent view) {
        scene.setRoot(view);
    }
}
