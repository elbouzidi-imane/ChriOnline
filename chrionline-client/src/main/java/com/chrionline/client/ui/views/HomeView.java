package com.chrionline.client.ui.views;

import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.function.Supplier;

public class HomeView extends ScrollPane {
    public HomeView() {
        VBox root = new VBox(28);
        root.setPadding(new Insets(28));
        root.setStyle(ViewFactory.pageBackground());

        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setContent(root);

        Label logo = new Label("ChriOnline");
        logo.setStyle("-fx-font-size: 38px; -fx-font-weight: bold; -fx-text-fill: #15372f;");

        Button categoriesButton = ViewFactory.createSecondaryButton("Catalogue");
        categoriesButton.setOnAction(event -> safeNavigate(ProductListView::new));
        Button loginButton = ViewFactory.createSecondaryButton("Connexion");
        loginButton.setOnAction(event -> safeNavigate(LoginView::new));
        Button registerButton = ViewFactory.createPrimaryButton("Inscription");
        registerButton.setOnAction(event -> safeNavigate(RegisterView::new));

        HBox topBar = ViewFactory.createTopBar(logo, categoriesButton, loginButton, registerButton);

        VBox left = new VBox(20);
        left.setAlignment(Pos.CENTER_LEFT);
        left.setFillWidth(true);
        HBox.setHgrow(left, Priority.ALWAYS);

        Label capsule = new Label("COLLECTIONS CURATEES  •  FEMME  •  HOMME  •  ENFANT");
        capsule.setStyle("-fx-background-color: rgba(22,70,61,0.96); -fx-text-fill: white; "
                + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 9 16 9 16; -fx-background-radius: 999;");

        Label title = new Label("Une vitrine mode plus nette, plus premium et plus facile a parcourir.");
        title.setWrapText(true);
        title.setMaxWidth(560);
        title.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #17342d;");

        Label subtitle = new Label("Explorez les produits, comparez les tailles, finalisez votre panier puis suivez vos commandes dans une interface plus elegante et plus lisible.");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(560);
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #54646d; -fx-line-spacing: 2px;");

        HBox ctaRow = new HBox(12);
        Button shopNowButton = ViewFactory.createPrimaryButton("Explorer la collection");
        shopNowButton.setOnAction(event -> safeNavigate(ProductListView::new));
        Button accountButton = ViewFactory.createSecondaryButton("Creer mon espace");
        accountButton.setOnAction(event -> safeNavigate(RegisterView::new));
        ctaRow.getChildren().addAll(shopNowButton, accountButton);

        HBox metrics = new HBox(14,
                createMetricCard("Mode libre", "Catalogue visible sans compte"),
                createMetricCard("Guides utiles", "Tailles et details produits"),
                createMetricCard("Suivi clair", "Panier, commande et historique"));

        left.getChildren().addAll(capsule, title, subtitle, ctaRow, metrics);

        StackPane right = buildHeroPanel();

        HBox heroRow = new HBox(24, left, right);
        heroRow.setAlignment(Pos.TOP_LEFT);

        HBox strips = new HBox(16,
                createFeatureStrip("Livraison flexible", "Standard, express et point relais avec parcours plus lisible.", "#fff3e4"),
                createFeatureStrip("Paiement simplifie", "Carte, PayPal ou mode fictif dans un checkout plus propre.", "#eaf4ef"),
                createFeatureStrip("Admin plus direct", "Produits, commandes et utilisateurs reunis dans une UI plus structuree.", "#f7f1ea"));

        Label focusTitle = new Label("Une interface plus mode, moins scolaire");
        focusTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #16332b;");

        HBox cards = new HBox(16,
                createFeatureCard("Navigation plus elegante", "Des cartes plus aeriennes, un contraste plus net et une hierarchie plus claire des actions."),
                createFeatureCard("Catalogue mieux valorise", "Les produits gagnent en presence avec un cadre plus premium et des badges mieux integres."),
                createFeatureCard("Parcours plus fluide", "Les ecrans de connexion et d'inscription prennent une direction visuelle plus forte et plus coherente."));

        root.getChildren().addAll(topBar, heroRow, strips, focusTitle, cards);
    }

    private void safeNavigate(Supplier<Parent> viewSupplier) {
        try {
            NavigationManager.navigateTo(viewSupplier.get());
        } catch (Throwable error) {
            UIUtils.showError("Navigation impossible : " + error.getClass().getSimpleName()
                    + (error.getMessage() == null || error.getMessage().isBlank() ? "" : " - " + error.getMessage()));
        }
    }

    private StackPane buildHeroPanel() {
        StackPane hero = new StackPane();
        hero.setPrefSize(390, 460);
        hero.setMaxSize(390, 460);
        hero.setStyle(ViewFactory.glassPanelStyle());

        Rectangle base = new Rectangle(390, 460);
        base.setArcWidth(56);
        base.setArcHeight(56);
        base.setFill(Color.rgb(255, 250, 244, 0.48));

        Rectangle sandPanel = new Rectangle(220, 300);
        sandPanel.setArcWidth(34);
        sandPanel.setArcHeight(34);
        sandPanel.setTranslateX(70);
        sandPanel.setTranslateY(-42);
        sandPanel.setFill(Color.web("#d58f43"));

        Rectangle greenPanel = new Rectangle(248, 316);
        greenPanel.setArcWidth(34);
        greenPanel.setArcHeight(34);
        greenPanel.setTranslateX(-52);
        greenPanel.setTranslateY(24);
        greenPanel.setFill(Color.web("#1b5f52"));

        Circle bubbleTop = new Circle(46, Color.web("#fff1df"));
        bubbleTop.setTranslateX(-124);
        bubbleTop.setTranslateY(-142);

        Circle bubbleBottom = new Circle(42, Color.web("#ddefe8"));
        bubbleBottom.setTranslateX(128);
        bubbleBottom.setTranslateY(152);

        Label heroWord = new Label("CURATED\nSTYLE");
        heroWord.setStyle("-fx-font-size: 46px; -fx-font-weight: bold; -fx-text-fill: white;");
        heroWord.setTranslateX(-28);
        heroWord.setTranslateY(-18);

        VBox promoCard = new VBox(10);
        promoCard.setPadding(new Insets(18));
        promoCard.setMaxWidth(205);
        promoCard.setTranslateX(-86);
        promoCard.setTranslateY(132);
        promoCard.setStyle(ViewFactory.elevatedCardStyle());

        Label promoBadge = new Label("NOUVELLE EXPERIENCE");
        promoBadge.setStyle("-fx-background-color: #17342d; -fx-text-fill: white; -fx-font-size: 11px; "
                + "-fx-font-weight: bold; -fx-padding: 6 10 6 10; -fx-background-radius: 999;");
        Label promoTitle = new Label("Interface retravaillee");
        promoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #16332b;");
        Label promoText = new Label("Un rendu plus editorial, plus chaud et plus coherent sur l'ensemble du parcours client.");
        promoText.setWrapText(true);
        promoText.setStyle("-fx-font-size: 12px; -fx-text-fill: #55616a;");
        promoCard.getChildren().addAll(promoBadge, promoTitle, promoText);

        hero.getChildren().addAll(base, sandPanel, greenPanel, bubbleTop, bubbleBottom, heroWord, promoCard);
        return hero;
    }

    private VBox createMetricCard(String title, String value) {
        Label top = new Label(title);
        top.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        Label bottom = new Label(value);
        bottom.setWrapText(true);
        bottom.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #16332b;");
        VBox box = new VBox(5, top, bottom);
        box.setPadding(new Insets(16));
        box.setPrefWidth(160);
        box.setStyle(ViewFactory.cardStyle());
        return box;
    }

    private VBox createFeatureStrip(String title, String text, String bg) {
        Label top = new Label(title);
        top.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #16332b;");
        Label bottom = new Label(text);
        bottom.setWrapText(true);
        bottom.setStyle("-fx-font-size: 13px; -fx-text-fill: #55616a;");
        VBox box = new VBox(6, top, bottom);
        box.setPadding(new Insets(20));
        box.setPrefWidth(286);
        box.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 26; "
                + "-fx-border-color: rgba(255,255,255,0.75); -fx-border-radius: 26;");
        return box;
    }

    private VBox createFeatureCard(String title, String text) {
        Label top = new Label(title);
        top.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #16332b;");
        Label bottom = new Label(text);
        bottom.setWrapText(true);
        bottom.setStyle("-fx-font-size: 13px; -fx-text-fill: #55616a;");
        VBox box = new VBox(8, top, bottom);
        box.setPadding(new Insets(20));
        box.setPrefWidth(288);
        box.setStyle(ViewFactory.elevatedCardStyle());
        return box;
    }
}
