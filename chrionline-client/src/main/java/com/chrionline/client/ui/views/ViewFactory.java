package com.chrionline.client.ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class ViewFactory {
    private ViewFactory() {
    }

    public static HBox createTopBar(Node left, Node... rightItems) {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(0, 0, 12, 0));
        bar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        if (left != null) {
            bar.getChildren().add(left);
        }
        bar.getChildren().add(spacer);
        for (Node item : rightItems) {
            bar.getChildren().add(item);
        }
        return bar;
    }

    public static VBox createPageShell(String title, String subtitle) {
        VBox shell = new VBox(18);
        shell.setPadding(new Insets(24));
        shell.setStyle("-fx-background-color: linear-gradient(to bottom, #fff8ef, #ffe8ce);");
        shell.getChildren().add(createHeaderBlock(title, subtitle));
        return shell;
    }

    public static VBox createHeaderBlock(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #14261f;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5f6368;");

        return new VBox(6, titleLabel, subtitleLabel);
    }

    public static Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 14; -fx-padding: 11 18 11 18;");
        button.setWrapText(false);
        button.setTextOverrun(OverrunStyle.CLIP);
        button.setMinWidth(110);
        return button;
    }

    public static Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: white; -fx-text-fill: #162521; -fx-font-weight: bold; "
                + "-fx-background-radius: 14; -fx-padding: 11 18 11 18;");
        button.setWrapText(false);
        button.setTextOverrun(OverrunStyle.CLIP);
        button.setMinWidth(110);
        return button;
    }

    public static String cardStyle() {
        return "-fx-background-color: rgba(255,255,255,0.92); -fx-background-radius: 24; "
                + "-fx-border-color: rgba(255,255,255,0.55); -fx-border-radius: 24;";
    }
}
