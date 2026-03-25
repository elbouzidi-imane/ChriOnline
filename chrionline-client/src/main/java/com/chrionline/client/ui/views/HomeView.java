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
        setSpacing(26);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: linear-gradient(to bottom, #fff8ef, #ffe2bd 52%, #eef7f3);");

        Label logo = new Label("ChriOnline");
        logo.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Button categoriesButton = ViewFactory.createSecondaryButton("Catalogue");
        categoriesButton.setOnAction(event -> NavigationManager.navigateTo(new ProductListView()));
        Button loginButton = ViewFactory.createSecondaryButton("Connexion");
        loginButton.setOnAction(event -> NavigationManager.navigateTo(new LoginView()));
        Button registerButton = ViewFactory.createPrimaryButton("Inscription");
        registerButton.setOnAction(event -> NavigationManager.navigateTo(new RegisterView()));

        HBox topBar = ViewFactory.createTopBar(logo, categoriesButton, loginButton, registerButton);

        VBox left = new VBox(18);
        left.setAlignment(Pos.CENTER_LEFT);
        left.setPrefWidth(470);

        Label capsule = new Label("MODE FEMME • HOMME • ENFANT • SPORT");
        capsule.setStyle("-fx-background-color: #12372e; -fx-text-fill: white; -fx-font-size: 11px; "
                + "-fx-font-weight: bold; -fx-padding: 8 14 8 14; -fx-background-radius: 999;");

        Label title = new Label("Un shopping plus clair, plus coloré et plus rapide.");
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 46px; -fx-font-weight: bold; -fx-text-fill: #16332b;");

        Label subtitle = new Label("Parcourez le catalogue sans connexion, ouvrez les fiches produit, comparez les tailles, puis connectez-vous pour ajouter au panier, commander et suivre vos achats.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #55616a;");

        HBox ctaRow = new HBox(12);
        Button shopNowButton = ViewFactory.createPrimaryButton("Voir les produits");
        shopNowButton.setOnAction(event -> NavigationManager.navigateTo(new ProductListView()));
        Button accountButton = ViewFactory.createSecondaryButton("Creer mon compte");
        accountButton.setOnAction(event -> NavigationManager.navigateTo(new RegisterView()));
        ctaRow.getChildren().addAll(shopNowButton, accountButton);

        HBox metrics = new HBox(14,
                createMetricCard("Catalogue ouvert", "Accessible avant connexion"),
                createMetricCard("Guide de taille", "Visible dans la fiche produit"),
                createMetricCard("Commande", "Panier, checkout et historique"));

        left.getChildren().addAll(capsule, title, subtitle, ctaRow, metrics);

        StackPane right = buildHeroPanel();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox heroRow = new HBox(24, left, spacer, right);
        heroRow.setAlignment(Pos.CENTER_LEFT);

        HBox strips = new HBox(14,
                createFeatureStrip("Livraison", "Standard, express et point relais", "#fff2df"),
                createFeatureStrip("Paiement", "Carte bancaire, PayPal ou fictif", "#eef8f4"),
                createFeatureStrip("Historique", "Photos, taille, couleur et detail commande", "#fff7ef"));

        Label focusTitle = new Label("Pourquoi commencer ici");
        focusTitle.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #16332b;");

        HBox cards = new HBox(14,
                createFeatureCard("Explorer sans compte", "Le visiteur peut filtrer les categories et consulter les details produit avant de se connecter."),
                createFeatureCard("Acheter avec plus de confiance", "Le panier, le checkout et l'historique affichent les bonnes informations de taille, photo et prix."),
                createFeatureCard("Gerer facilement", "Une fois admin, vous accedez a un espace separe pour produits, utilisateurs et commandes."));

        getChildren().addAll(topBar, heroRow, strips, focusTitle, cards);
    }

    private StackPane buildHeroPanel() {
        StackPane hero = new StackPane();
        hero.setPrefSize(420, 470);
        hero.setMaxSize(420, 470);

        Rectangle base = new Rectangle(420, 470);
        base.setArcWidth(42);
        base.setArcHeight(42);
        base.setFill(Color.rgb(255, 255, 255, 0.55));

        Rectangle orangeCard = new Rectangle(240, 300);
        orangeCard.setArcWidth(32);
        orangeCard.setArcHeight(32);
        orangeCard.setTranslateX(70);
        orangeCard.setTranslateY(-40);
        orangeCard.setFill(Color.web("#eb8b1b"));

        Rectangle greenCard = new Rectangle(260, 320);
        greenCard.setArcWidth(32);
        greenCard.setArcHeight(32);
        greenCard.setTranslateX(-56);
        greenCard.setTranslateY(24);
        greenCard.setFill(Color.web("#196b5a"));

        Circle bubbleTop = new Circle(48, Color.web("#fff1dc"));
        bubbleTop.setTranslateX(-132);
        bubbleTop.setTranslateY(-142);

        Circle bubbleBottom = new Circle(42, Color.web("#dff4eb"));
        bubbleBottom.setTranslateX(138);
        bubbleBottom.setTranslateY(152);

        Label heroWord = new Label("STYLE\nSTORE");
        heroWord.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white;");
        heroWord.setTranslateX(-36);
        heroWord.setTranslateY(-18);

        VBox promoCard = new VBox(10);
        promoCard.setPadding(new Insets(18));
        promoCard.setMaxWidth(210);
        promoCard.setTranslateX(-92);
        promoCard.setTranslateY(128);
        promoCard.setStyle("-fx-background-color: rgba(255,255,255,0.96); -fx-background-radius: 24;");

        Label promoBadge = new Label("NOUVEAU");
        promoBadge.setStyle("-fx-background-color: #eb8b1b; -fx-text-fill: white; -fx-font-size: 11px; "
                + "-fx-font-weight: bold; -fx-padding: 6 10 6 10; -fx-background-radius: 999;");
        Label promoTitle = new Label("Catalogue plus riche");
        promoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #16332b;");
        Label promoText = new Label("Photos produit, guide de taille, couleurs, panier detaille et commandes enrichies.");
        promoText.setWrapText(true);
        promoText.setStyle("-fx-font-size: 12px; -fx-text-fill: #55616a;");
        promoCard.getChildren().addAll(promoBadge, promoTitle, promoText);

        hero.getChildren().addAll(base, orangeCard, greenCard, bubbleTop, bubbleBottom, heroWord, promoCard);
        return hero;
    }

    private VBox createMetricCard(String title, String value) {
        Label top = new Label(title);
        top.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        Label bottom = new Label(value);
        bottom.setWrapText(true);
        bottom.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #16332b;");
        VBox box = new VBox(5, top, bottom);
        box.setPadding(new Insets(15));
        box.setPrefWidth(154);
        box.setStyle(ViewFactory.cardStyle());
        return box;
    }

    private VBox createFeatureStrip(String title, String text, String bg) {
        Label top = new Label(title);
        top.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #16332b;");
        Label bottom = new Label(text);
        bottom.setWrapText(true);
        bottom.setStyle("-fx-font-size: 13px; -fx-text-fill: #55616a;");
        VBox box = new VBox(6, top, bottom);
        box.setPadding(new Insets(18));
        box.setPrefWidth(286);
        box.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 24;");
        return box;
    }

    private VBox createFeatureCard(String title, String text) {
        Label top = new Label(title);
        top.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #16332b;");
        Label bottom = new Label(text);
        bottom.setWrapText(true);
        bottom.setStyle("-fx-font-size: 13px; -fx-text-fill: #55616a;");
        VBox box = new VBox(8, top, bottom);
        box.setPadding(new Insets(18));
        box.setPrefWidth(288);
        box.setStyle(ViewFactory.cardStyle());
        return box;
    }
}
