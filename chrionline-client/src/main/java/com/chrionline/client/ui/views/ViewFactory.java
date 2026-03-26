package com.chrionline.client.ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class ViewFactory {
    private ViewFactory() {
    }

    public static HBox createTopBar(Node left, Node... rightItems) {
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(0, 0, 16, 0));
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
        VBox shell = new VBox(24);
        shell.setPadding(new Insets(28));
        shell.setStyle(pageBackground());
        shell.getChildren().add(createHeaderBlock(title, subtitle));
        return shell;
    }

    public static VBox createHeaderBlock(String title, String subtitle) {
        Label eyebrow = new Label("CHRIONLINE");
        eyebrow.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 2px; "
                + "-fx-text-fill: #9b5d20; -fx-background-color: rgba(255,244,229,0.9); "
                + "-fx-padding: 7 12 7 12; -fx-background-radius: 999;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #14261f;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5f6368;");

        return new VBox(8, eyebrow, titleLabel, subtitleLabel);
    }

    public static Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: linear-gradient(to right, #cc6b1d, #f29b38); "
                + "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; "
                + "-fx-background-radius: 16; -fx-padding: 12 20 12 20; "
                + "-fx-effect: dropshadow(gaussian, rgba(204,107,29,0.28), 18, 0.18, 0, 6);");
        button.setWrapText(false);
        button.setTextOverrun(OverrunStyle.CLIP);
        button.setMinWidth(118);
        return button;
    }

    public static Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: rgba(255,255,255,0.84); -fx-text-fill: #18342d; "
                + "-fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 16; "
                + "-fx-border-color: rgba(24,52,45,0.10); -fx-border-radius: 16; "
                + "-fx-padding: 12 20 12 20;");
        button.setWrapText(false);
        button.setTextOverrun(OverrunStyle.CLIP);
        button.setMinWidth(118);
        return button;
    }

    public static String cardStyle() {
        return "-fx-background-color: rgba(255,251,246,0.92); -fx-background-radius: 28; "
                + "-fx-border-color: rgba(255,255,255,0.82); -fx-border-radius: 28; "
                + "-fx-effect: dropshadow(gaussian, rgba(29,46,39,0.10), 26, 0.18, 0, 8);";
    }

    public static String elevatedCardStyle() {
        return "-fx-background-color: linear-gradient(to bottom right, rgba(255,251,246,0.98), rgba(247,238,227,0.94)); "
                + "-fx-background-radius: 32; -fx-border-color: rgba(255,255,255,0.92); "
                + "-fx-border-radius: 32; -fx-effect: dropshadow(gaussian, rgba(18,55,46,0.14), 30, 0.2, 0, 10);";
    }

    public static String glassPanelStyle() {
        return "-fx-background-color: rgba(255,255,255,0.18); -fx-background-radius: 30; "
                + "-fx-border-color: rgba(255,255,255,0.24); -fx-border-radius: 30;";
    }

    public static String inputStyle() {
        return "-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 16; "
                + "-fx-border-color: rgba(25,45,40,0.08); -fx-border-radius: 16; "
                + "-fx-padding: 13 14 13 14; -fx-font-size: 13px;";
    }

    public static String pageBackground() {
        return "-fx-background-color: linear-gradient(to bottom, #f8efe4, #f1ddc6 42%, #e4efe9 100%);";
    }
}
