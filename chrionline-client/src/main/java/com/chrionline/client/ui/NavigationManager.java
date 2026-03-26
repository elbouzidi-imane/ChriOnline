package com.chrionline.client.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class NavigationManager {
    private static Scene scene;
    private static StackPane root;
    private static StackPane contentLayer;
    private static VBox notificationLayer;

    private NavigationManager() {
    }

    public static void init(Stage stage) {
        contentLayer = new StackPane();

        notificationLayer = new VBox(12);
        notificationLayer.setMouseTransparent(true);
        notificationLayer.setPadding(new Insets(24));
        notificationLayer.setAlignment(Pos.TOP_RIGHT);
        StackPane.setAlignment(notificationLayer, Pos.TOP_RIGHT);

        root = new StackPane(contentLayer, notificationLayer);
        scene = new Scene(root, 1280, 860);
        stage.setScene(scene);
    }

    public static void navigateTo(Parent view) {
        contentLayer.getChildren().setAll(view);
    }

    public static boolean isReady() {
        return notificationLayer != null;
    }

    public static void showNotification(String title, String message, boolean error) {
        if (notificationLayer == null) {
            return;
        }
        Platform.runLater(() -> {
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #17342d;");

            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(320);
            messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057;");

            VBox toast = new VBox(6, titleLabel, messageLabel);
            toast.setMaxWidth(340);
            toast.setPadding(new Insets(14, 16, 14, 16));
            toast.setStyle((error
                    ? "-fx-background-color: linear-gradient(to bottom, #fff2ef, #ffd8cf); -fx-border-color: rgba(154,52,18,0.18);"
                    : "-fx-background-color: linear-gradient(to bottom, #fffaf4, #f6ead7); -fx-border-color: rgba(23,52,45,0.10);")
                    + "-fx-background-radius: 18; -fx-border-radius: 18; "
                    + "-fx-effect: dropshadow(gaussian, rgba(18,55,46,0.18), 22, 0.18, 0, 8);");

            toast.setOpacity(0);
            notificationLayer.getChildren().add(0, toast);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), toast);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            PauseTransition wait = new PauseTransition(Duration.seconds(error ? 5 : 4));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(240), toast);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> notificationLayer.getChildren().remove(toast));

            fadeIn.play();
            wait.setOnFinished(event -> fadeOut.play());
            wait.play();
        });
    }
}
