package com.chrionline.client.ui.views;

import com.chrionline.client.ui.NavigationManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class HomeView extends VBox {
    public HomeView() {
        setSpacing(28);
        setPadding(new Insets(26));
        setStyle("-fx-background-color: linear-gradient(to bottom right, #fff7ed, #ffe0b8 55%, #cfece5);");

        Label logo = new Label("ChriOnline");
        logo.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #162521;");

        Button loginButton = ViewFactory.createSecondaryButton("Se connecter");
        loginButton.setOnAction(event -> NavigationManager.navigateTo(new LoginView()));
        Button registerButton = ViewFactory.createPrimaryButton("S'inscrire");
        registerButton.setOnAction(event -> NavigationManager.navigateTo(new RegisterView()));

        HBox topBar = ViewFactory.createTopBar(logo, loginButton, registerButton);

        VBox left = new VBox(16);
        left.setAlignment(Pos.CENTER_LEFT);
        left.setPrefWidth(420);

        Label badge = new Label("MODE • ACCESSIBLE • CONNECTEE");
        badge.setStyle("-fx-background-color: #1f6f5f; -fx-text-fill: white; -fx-font-size: 11px; "
                + "-fx-font-weight: bold; -fx-padding: 6 10 6 10; -fx-background-radius: 999;");

        Label title = new Label("Une boutique e-commerce claire, moderne et prete a commander.");
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 38px; -fx-font-weight: bold; -fx-text-fill: #15231f;");

        Label text = new Label("Parcourez les vetements, filtrez les categories, creez votre panier et finalisez votre achat avec une experience plus visuelle.");
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 15px; -fx-text-fill: #5f6368;");

        Button startButton = ViewFactory.createPrimaryButton("Commencer");
        startButton.setOnAction(event -> NavigationManager.navigateTo(new LoginView()));
        Button createAccountButton = ViewFactory.createSecondaryButton("Creer un compte");
        createAccountButton.setOnAction(event -> NavigationManager.navigateTo(new RegisterView()));
        HBox cta = new HBox(12, startButton, createAccountButton);

        HBox stats = new HBox(14,
                createMiniCard("Collections", "4 categories"),
                createMiniCard("Panier", "temps reel"),
                createMiniCard("Livraison", "standard / express"));

        left.getChildren().addAll(badge, title, text, cta, stats);

        StackPane hero = new StackPane();
        hero.setPrefSize(360, 430);
        hero.setMaxSize(360, 430);

        Rectangle panel = new Rectangle(360, 430);
        panel.setArcWidth(36);
        panel.setArcHeight(36);
        panel.setFill(Color.rgb(255, 255, 255, 0.45));

        Rectangle card1 = new Rectangle(220, 280);
        card1.setArcWidth(26);
        card1.setArcHeight(26);
        card1.setTranslateX(-36);
        card1.setTranslateY(10);
        card1.setFill(Color.web("#1f6f5f"));

        Rectangle card2 = new Rectangle(180, 220);
        card2.setArcWidth(26);
        card2.setArcHeight(26);
        card2.setTranslateX(70);
        card2.setTranslateY(-30);
        card2.setFill(Color.web("#d97706"));

        Circle accent = new Circle(48, Color.web("#fff1d6"));
        accent.setTranslateX(96);
        accent.setTranslateY(140);

        Label heroLabel = new Label("NEW\nDROP");
        heroLabel.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: white;");
        heroLabel.setTranslateX(-36);
        heroLabel.setTranslateY(12);

        hero.getChildren().addAll(panel, card1, card2, accent, heroLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox body = new HBox(20, left, spacer, hero);
        body.setAlignment(Pos.CENTER_LEFT);

        VBox featureRow = new VBox(14);
        Label featureTitle = new Label("Pourquoi cette interface");
        featureTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #162521;");
        featureRow.getChildren().add(featureTitle);
        featureRow.getChildren().add(new HBox(14,
                createFeatureCard("Catalogue visuel", "Cartes produit, image ou placeholder, prix mis en avant."),
                createFeatureCard("Parcours clair", "Login, panier, checkout et historique dans le meme style."),
                createFeatureCard("Gestion admin", "Produits, utilisateurs et commandes dans une vue propre.")));

        getChildren().addAll(topBar, body, featureRow);
    }

    private VBox createMiniCard(String title, String value) {
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 12px; -fx-text-fill: #5f6368;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #162521;");
        VBox box = new VBox(4, t, v);
        box.setPadding(new Insets(14));
        box.setStyle(ViewFactory.cardStyle());
        return box;
    }

    private VBox createFeatureCard(String title, String text) {
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #162521;");
        Label d = new Label(text);
        d.setWrapText(true);
        d.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f6368;");
        VBox box = new VBox(8, t, d);
        box.setPadding(new Insets(16));
        box.setPrefWidth(260);
        box.setStyle(ViewFactory.cardStyle());
        return box;
    }
}
