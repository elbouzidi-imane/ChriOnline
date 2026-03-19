package com.chrionline.client.ui.views;

import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.model.ProductSizeDTO;
import com.chrionline.client.service.CartService;
import com.chrionline.client.session.AppSession;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.PriceUtils;
import com.chrionline.client.util.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailView extends ScrollPane {
    private final CartService cartService = new CartService();

    public ProductDetailView(ProductDTO product) {
        VBox page = new VBox(20);
        page.setPadding(new Insets(22));
        page.setStyle("-fx-background-color: linear-gradient(to bottom, #fff8ef, #ffe7cc 70%, #eef8f4);");

        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background: #fff8ef; -fx-background-color: #fff8ef;");
        setContent(page);

        Button homeButton = ViewFactory.createSecondaryButton("Accueil");
        homeButton.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));
        Button backButton = ViewFactory.createSecondaryButton("Retour");
        backButton.setOnAction(event -> NavigationManager.navigateTo(new ProductListView()));

        HBox actions = new HBox(10, homeButton, backButton);

        StackPane imageBox = new StackPane();
        imageBox.setPrefSize(360, 360);

        Rectangle bg = new Rectangle(360, 360);
        bg.setArcWidth(30);
        bg.setArcHeight(30);
        bg.setFill(Color.web("#ffd9b8"));

        Label placeholder = new Label(product.getNom().substring(0, 1).toUpperCase());
        placeholder.setStyle("-fx-font-size: 90px; -fx-font-weight: bold; -fx-text-fill: #7c2d12;");
        imageBox.getChildren().addAll(bg, placeholder);

        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            try {
                Image image = new Image(product.getImageUrl(), 336, 336, true, true, true);
                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(336);
                    imageView.setFitHeight(336);
                    imageView.setPreserveRatio(true);
                    imageBox.getChildren().setAll(bg, imageView);
                }
            } catch (Exception ignored) {
            }
        }

        VBox headerBox = new VBox(10);
        if (product.getCategorie() != null && product.getCategorie().getNom() != null && !product.getCategorie().getNom().isBlank()) {
            Label category = new Label(product.getCategorie().getNom());
            category.setStyle("-fx-font-size: 12px; -fx-text-fill: #1f6f5f; "
                    + "-fx-background-color: #daf1e9; -fx-padding: 6 10 6 10; -fx-background-radius: 999;");
            headerBox.getChildren().add(category);
        }

        Label title = new Label(product.getNom());
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Label description = new Label(product.getDescription() == null ? "Description bientot disponible." : product.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #4b5563;");

        Label price = new Label(PriceUtils.formatMad(product.getPrixAffiche()));
        price.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #d97706;");

        Label info = new Label("Matiere : " + safe(product.getMatiere()) + "   |   Couleur : " + safe(product.getCouleur()));
        info.setWrapText(true);
        info.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        headerBox.getChildren().addAll(title, description, price, info);

        ToggleGroup sizeGroup = new ToggleGroup();
        FlowPane sizesPane = new FlowPane();
        sizesPane.setHgap(10);
        sizesPane.setVgap(10);

        for (ProductSizeDTO size : product.getTailles()) {
            if (!size.isDisponible()) {
                continue;
            }
            RadioButton button = new RadioButton(size.getValeur() + " (" + size.getStock() + " stock)");
            button.setUserData(size);
            button.setToggleGroup(sizeGroup);
            button.setStyle("-fx-background-color: white; -fx-padding: 10 14 10 14; -fx-background-radius: 14; "
                    + "-fx-border-color: #e7d7c8; -fx-border-radius: 14; -fx-font-size: 13px;");
            sizesPane.getChildren().add(button);
        }

        Label sizesTitle = new Label("Choisissez votre taille");
        sizesTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Button sizeGuideButton = new Button("Guide de taille");
        sizeGuideButton.setStyle("-fx-background-color: #1f6f5f; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 16; -fx-padding: 12 16 12 16;");
        sizeGuideButton.setOnAction(event -> showSizeGuide(product));

        Spinner<Integer> quantitySpinner = new Spinner<>(1, 20, 1);
        quantitySpinner.setEditable(true);
        quantitySpinner.setPrefWidth(120);

        Label qtyLabel = new Label("Quantite");
        qtyLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Button addButton = new Button(AppSession.isLoggedIn() ? "Ajouter au panier" : "Se connecter pour commander");
        addButton.setStyle("-fx-background-color: #eb8b1b; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; "
                + "-fx-background-radius: 16; -fx-padding: 13 18 13 18;");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(event -> {
            if (!AppSession.isLoggedIn()) {
                UIUtils.showInfo("Connectez-vous ou inscrivez-vous pour ajouter ce produit au panier.");
                NavigationManager.navigateTo(new LoginView());
                return;
            }
            if (sizeGroup.getSelectedToggle() == null) {
                UIUtils.showError("Selectionnez une taille.");
                return;
            }
            try {
                ProductSizeDTO selectedSize = (ProductSizeDTO) sizeGroup.getSelectedToggle().getUserData();
                cartService.addToCart(product.getId(), selectedSize.getId(), quantitySpinner.getValue());
                UIUtils.showSuccess("Produit ajoute au panier.");
                NavigationManager.navigateTo(new CartView());
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        VBox right = new VBox(16,
                headerBox,
                sizeGuideButton,
                sizesTitle,
                sizesPane,
                qtyLabel,
                quantitySpinner,
                addButton
        );
        right.setAlignment(Pos.TOP_LEFT);
        right.setPadding(new Insets(20));
        right.setStyle("-fx-background-color: rgba(255,255,255,0.94); -fx-background-radius: 26;");
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox content = new HBox(24, imageBox, right);
        content.setAlignment(Pos.TOP_LEFT);

        page.getChildren().addAll(actions, content);
    }

    private void showSizeGuide(ProductDTO product) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Guide de taille");
        stage.setResizable(false);

        VBox root = new VBox(18);
        root.setPadding(new Insets(22));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #fff9f2, #ffe7d0);");

        Label title = new Label("Guide de taille");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #162521;");
        Label subtitle = new Label(product.getNom() + " - guide indicatif pour faciliter votre choix.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: #52606d; -fx-font-size: 13px;");

        FlowPane cards = new FlowPane();
        cards.setHgap(12);
        cards.setVgap(12);

        List<ProductSizeDTO> tailles = product.getTailles();
        if (tailles.isEmpty()) {
            Label empty = new Label("Aucune taille n'est disponible pour ce produit.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #7c2d12;");
            cards.getChildren().add(empty);
        } else {
            for (ProductSizeDTO size : tailles) {
                cards.getChildren().add(createGuideCard(size));
            }
        }

        Label help = new Label(
                "Conseil: si vous etes entre deux tailles, prenez la plus grande pour une coupe plus confortable."
        );
        help.setWrapText(true);
        help.setStyle("-fx-font-size: 13px; -fx-text-fill: #4b5563; -fx-background-color: rgba(255,255,255,0.82); "
                + "-fx-background-radius: 16; -fx-padding: 12;");

        Button closeButton = ViewFactory.createPrimaryButton("Fermer");
        closeButton.setOnAction(event -> stage.close());

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.getChildren().addAll(title, subtitle, cards, help, closeButton);

        stage.setScene(new Scene(scrollPane, 560, 480));
        stage.showAndWait();
    }

    private VBox createGuideCard(ProductSizeDTO size) {
        Map<String, String> hints = guideFor(size.getValeur());

        Label sizeLabel = new Label(size.getValeur());
        sizeLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1f6f5f;");

        Label stockLabel = new Label(size.getStock() > 0 ? size.getStock() + " en stock" : "Rupture");
        stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7c2d12;");

        VBox card = new VBox(8, sizeLabel, stockLabel);
        card.setPrefWidth(160);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.92); -fx-background-radius: 20;");

        for (Map.Entry<String, String> entry : hints.entrySet()) {
            Label row = new Label(entry.getKey() + ": " + entry.getValue());
            row.setWrapText(true);
            row.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155;");
            card.getChildren().add(row);
        }

        return card;
    }

    private Map<String, String> guideFor(String sizeValue) {
        Map<String, String> hints = new LinkedHashMap<>();
        String normalized = sizeValue == null ? "" : sizeValue.trim().toUpperCase();
        switch (normalized) {
            case "XS" -> {
                hints.put("Tour de poitrine", "78-82 cm");
                hints.put("Tour de taille", "58-62 cm");
                hints.put("Conseil coupe", "Silhouette fine");
            }
            case "S" -> {
                hints.put("Tour de poitrine", "82-86 cm");
                hints.put("Tour de taille", "62-66 cm");
                hints.put("Conseil coupe", "Pres du corps");
            }
            case "M" -> {
                hints.put("Tour de poitrine", "90-94 cm");
                hints.put("Tour de taille", "70-74 cm");
                hints.put("Conseil coupe", "Ajustement standard");
            }
            case "L" -> {
                hints.put("Tour de poitrine", "98-102 cm");
                hints.put("Tour de taille", "78-82 cm");
                hints.put("Conseil coupe", "Plus d'aisance");
            }
            case "XL" -> {
                hints.put("Tour de poitrine", "106-110 cm");
                hints.put("Tour de taille", "86-90 cm");
                hints.put("Conseil coupe", "Coupe confortable");
            }
            default -> {
                hints.put("Correspondance", "Consultez votre taille habituelle");
                hints.put("Conseil coupe", "Comparez avec un vetement similaire");
                hints.put("Astuce", "Si doute, prenez la plus grande taille");
            }
        }
        return hints;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
