package com.chrionline.client.ui.views;

import com.chrionline.client.session.AppSession;
import com.chrionline.client.ui.NavigationManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

final class AdminViewSupport {
    private AdminViewSupport() {
    }

    static boolean ensureAdmin() {
        if (!AppSession.isLoggedIn() || !AppSession.getCurrentUser().isAdmin()) {
            NavigationManager.navigateTo(new LoginView());
            return false;
        }
        return true;
    }

    static ScrollPane createAdminPage(String title, String subtitle, String activeSection, VBox body) {
        VBox page = new VBox(18);
        page.setPadding(new Insets(22));
        page.setStyle("-fx-background-color: linear-gradient(to bottom, #fff8ef, #ffe6ca 66%, #edf8f3);");

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #fff8ef; -fx-background-color: #fff8ef;");

        Label badge = new Label("ADMIN");
        badge.setStyle("-fx-background-color: #1f6f5f; -fx-text-fill: white; -fx-font-size: 11px; "
                + "-fx-font-weight: bold; -fx-padding: 6 10 6 10; -fx-background-radius: 999;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5b5f63;");

        VBox heading = new VBox(8, badge, titleLabel, subtitleLabel);

        HBox nav = createAdminNav(activeSection);
        page.getChildren().addAll(heading, nav, body);
        return scrollPane;
    }

    static HBox createAdminNav(String activeSection) {
        Button dashboardButton = createNavButton("Dashboard", "dashboard".equals(activeSection));
        dashboardButton.setOnAction(event -> NavigationManager.navigateTo(new AdminView()));

        Button productsButton = createNavButton("Produits", "products".equals(activeSection));
        productsButton.setOnAction(event -> NavigationManager.navigateTo(new AdminProductsView()));

        Button usersButton = createNavButton("Utilisateurs", "users".equals(activeSection));
        usersButton.setOnAction(event -> NavigationManager.navigateTo(new AdminUsersView()));

        Button ordersButton = createNavButton("Commandes", "orders".equals(activeSection));
        ordersButton.setOnAction(event -> NavigationManager.navigateTo(new AdminOrdersView()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label emailLabel = new Label(AppSession.getCurrentUser().getEmail());
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

        Button homeButton = ViewFactory.createSecondaryButton("Accueil");
        homeButton.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));

        Button logoutButton = ViewFactory.createPrimaryButton("Deconnexion");
        logoutButton.setOnAction(event -> {
            AppSession.clear();
            NavigationManager.navigateTo(new LoginView());
        });

        HBox nav = new HBox(10, dashboardButton, productsButton, usersButton, ordersButton, spacer, emailLabel, homeButton, logoutButton);
        nav.setAlignment(Pos.CENTER_LEFT);
        return nav;
    }

    static VBox createStatCard(String title, Label valueLabel, String accentColor, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        valueLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b9096;");

        VBox card = new VBox(6, titleLabel, valueLabel, subtitleLabel);
        card.setPadding(new Insets(16));
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.94); -fx-background-radius: 22;");
        return card;
    }

    private static Button createNavButton(String text, boolean active) {
        Button button = new Button(text);
        button.setStyle(active
                ? "-fx-background-color: #1f6f5f; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 16; -fx-padding: 11 18 11 18;"
                : "-fx-background-color: rgba(255,255,255,0.94); -fx-text-fill: #12372e; -fx-font-weight: bold; "
                + "-fx-background-radius: 16; -fx-padding: 11 18 11 18;");
        return button;
    }
}
