package com.chrionline.client.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

public final class UIUtils {
    private UIUtils() {
    }

    public static void showInfo(String message) {
        showStyledAlert(Alert.AlertType.INFORMATION, "Information", message, false);
    }

    public static void showSuccess(String message) {
        showStyledAlert(Alert.AlertType.INFORMATION, "Confirmation", message, false);
    }

    public static void showError(String message) {
        showStyledAlert(Alert.AlertType.ERROR, "Erreur", message, true);
    }

    public static boolean confirm(String title, String message) {
        Alert alert = createAlert(Alert.AlertType.CONFIRMATION, title, message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private static void showStyledAlert(Alert.AlertType type, String title, String message, boolean wait) {
        Alert alert = createAlert(type, title, message);
        if (wait) {
            alert.showAndWait();
        } else {
            alert.show();
        }
    }

    private static Alert createAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("ChriOnline");
        alert.setHeaderText(title);
        alert.setContentText(message);

        DialogPane pane = alert.getDialogPane();
        pane.setStyle("-fx-background-color: linear-gradient(to bottom, #fffaf5, #ffe7c7);"
                + "-fx-font-size: 13px;"
                + "-fx-background-radius: 18;"
                + "-fx-border-radius: 18;"
                + "-fx-border-color: rgba(255,255,255,0.6);");
        return alert;
    }
}
