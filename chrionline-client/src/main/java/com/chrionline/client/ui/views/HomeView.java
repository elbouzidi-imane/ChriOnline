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
        setPadding(new Insets(24));
        setStyle("-fx-background-color: linear-gradient(to bottom right, #fff8f1, #ffe5c7 58%, #d8efe7);");

        Label logo = new Label("ChriOnline");
        logo.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Button browseTopButton = ViewFactory.createSecondaryButton("Voir le catalogue");
        browseTopButton.setOnAction(event -> NavigationManager.navigateTo(new ProductListView()));
        Button loginButton = ViewFactory.createSecondaryButton("Se connecter");
        loginButton.setOnAction(event -> NavigationManager.navigateTo(new LoginView()));
        Button registerButton = ViewFactory.createPrimaryButton("S'inscrire");
        registerButton.setOnAction(event -> NavigationManager.navigateTo(new RegisterView()));

        HBox topBar = ViewFactory.createTopBar(logo, browseTopButton, loginButton, registerButton);

        VBox left = new VBox(16);
        left.setAlignment(Pos.CENTER_LEFT);
        left.setPrefWidth(450);

        Label badge = new Label("TEMU STYLE • ORANGE • VERT • BLANC");
        badge.setStyle("-fx-background-color: #1f6f5f; -fx-text-fill: white; -fx-font-size: 11px; "
                + "-fx-font-weight: bold; -fx-padding: 7 12 7 12; -fx-background-radius: 999;");

        Label title = new Label("Explorez les vetements, consultez les details et achetez avec une experience claire.");
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Label text = new Label("Un visiteur peut deja parcourir les produits disponibles, ouvrir les details, comparer les tailles et ensuite se connecter pour commander.");
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 15px; -fx-text-fill: #475569;");

        HBox cta = new HBox(12);
        Button browseButton = ViewFactory.createPrimaryButton("Decouvrir les produits");
        browseButton.setOnAction(event -> NavigationManager.navigateTo(new ProductListView()));
        Button createAccountButton = ViewFactory.createSecondaryButton("Creer un compte");
        createAccountButton.setOnAction(event -> NavigationManager.navigateTo(new RegisterView()));
        cta.getChildren().addAll(browseButton, createAccountButton);

        HBox stats = new HBox(14,
                createMiniCard("Catalogue", "Accessible sans connexion"),
                createMiniCard("Detail produit", "Image, prix, tailles"),
                createMiniCard("Commande", "Connexion requise"));

        left.getChildren().addAll(badge, title, text, cta, stats);

        StackPane hero = new StackPane();
        hero.setPrefSize(390, 450);
        hero.setMaxSize(390, 450);

        Rectangle panel = new Rectangle(390, 450);
        panel.setArcWidth(40);
        panel.setArcHeight(40);
        panel.setFill(Color.rgb(255, 255, 255, 0.52));

        Rectangle card1 = new Rectangle(245, 305);
        card1.setArcWidth(30);
        card1.setArcHeight(30);
        card1.setTranslateX(-42);
        card1.setTranslateY(16);
        card1.setFill(Color.web("#1f6f5f"));

        Rectangle card2 = new Rectangle(205, 255);
        card2.setArcWidth(30);
        card2.setArcHeight(30);
        card2.setTranslateX(78);
        card2.setTranslateY(-34);
        card2.setFill(Color.web("#eb8b1b"));

        Circle accent = new Circle(52, Color.web("#fff3de"));
        accent.setTranslateX(112);
        accent.setTranslateY(148);

        VBox floatingCard = new VBox(10);
        floatingCard.setPadding(new Insets(18));
        floatingCard.setMaxWidth(200);
        floatingCard.setStyle("-fx-background-color: rgba(255,255,255,0.94); -fx-background-radius: 24;");
        floatingCard.setTranslateX(-80);
        floatingCard.setTranslateY(120);
        Label floatingTitle = new Label("Nouveautes");
        floatingTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #12372e;");
        Label floatingText = new Label("Catalogue ouvert aux visiteurs avec filtres et detail produit.");
        floatingText.setWrapText(true);
        floatingText.setStyle("-fx-font-size: 12px; -fx-text-fill: #52606d;");
        floatingCard.getChildren().addAll(floatingTitle, floatingText);

        Label heroLabel = new Label("SHOP\nNOW");
        heroLabel.setStyle("-fx-font-size: 46px; -fx-font-weight: bold; -fx-text-fill: white;");
        heroLabel.setTranslateX(-34);
        heroLabel.setTranslateY(-8);

        hero.getChildren().addAll(panel, card1, card2, accent, heroLabel, floatingCard);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox body = new HBox(22, left, spacer, hero);
        body.setAlignment(Pos.CENTER_LEFT);

        Label featureTitle = new Label("Ce que le visiteur peut faire");
        featureTitle.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        HBox featureRow = new HBox(14,
                createFeatureCard("Consulter les produits", "La page catalogue est maintenant accessible sans connexion."),
                createFeatureCard("Voir les details", "Nom, prix, image, tailles et guide de taille sont visibles."),
                createFeatureCard("Passer a l'action", "Connexion ou inscription pour panier, commande et historique."));

        getChildren().addAll(topBar, body, featureTitle, featureRow);
    }

    private VBox createMiniCard(String title, String value) {
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        Label v = new Label(value);
        v.setWrapText(true);
        v.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #12372e;");
        VBox box = new VBox(5, t, v);
        box.setPadding(new Insets(15));
        box.setPrefWidth(150);
        box.setStyle(ViewFactory.cardStyle());
        return box;
    }

    private VBox createFeatureCard(String title, String text) {
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #12372e;");
        Label d = new Label(text);
        d.setWrapText(true);
        d.setStyle("-fx-font-size: 13px; -fx-text-fill: #52606d;");
        VBox box = new VBox(8, t, d);
        box.setPadding(new Insets(18));
        box.setPrefWidth(275);
        box.setStyle(ViewFactory.cardStyle());
        return box;
    }
}
