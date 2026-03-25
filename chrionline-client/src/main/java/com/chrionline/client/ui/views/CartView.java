package com.chrionline.client.ui.views;

import com.chrionline.client.model.CartDTO;
import com.chrionline.client.model.CartLineDTO;
import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.model.ProductSizeDTO;
import com.chrionline.client.service.CartService;
import com.chrionline.client.service.ProductService;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.PriceUtils;
import com.chrionline.client.util.UIUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

public class CartView extends ScrollPane {
    private final CartService cartService = new CartService();
    private final ProductService productService = new ProductService();
    private final Map<Integer, ProductDTO> productCache = new HashMap<>();
    private final Map<Integer, String> sizeLabelCache = new HashMap<>();

    private final ListView<CartLineDTO> cartList = new ListView<>();
    private final Label totalLabel = new Label();
    private final Label countLabel = new Label();
    private final Label emptyLabel = new Label("Votre panier est vide");
    private final Button checkoutButton = new Button("Passer au paiement");

    public CartView() {
        VBox page = new VBox(18);
        page.setPadding(new Insets(22));
        page.setStyle("-fx-background-color: linear-gradient(to bottom, #fff8ef, #ffe4c6 70%, #eef8f4);");

        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background: #fff8ef; -fx-background-color: #fff8ef;");
        setContent(page);

        Label title = new Label("Votre panier");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Label subtitle = new Label("Retrouvez vos articles, modifiez la quantite, supprimez une ligne et voyez le prix se mettre a jour.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #5b5f63;");

        VBox heading = new VBox(8, title, subtitle);

        Button homeButton = ViewFactory.createSecondaryButton("Accueil");
        homeButton.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));
        Button continueButton = ViewFactory.createSecondaryButton("Continuer les achats");
        continueButton.setOnAction(event -> NavigationManager.navigateTo(new ProductListView()));
        checkoutButton.setStyle("-fx-background-color: #eb8b1b; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 16; -fx-padding: 12 18 12 18;");
        checkoutButton.setOnAction(event -> NavigationManager.navigateTo(new CheckoutView()));

        HBox topActions = new HBox(10, homeButton, continueButton);

        countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5b5f63;");
        emptyLabel.setVisible(false);
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #991b1b;");

        cartList.setPrefHeight(420);
        cartList.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        cartList.setCellFactory(ignored -> new ListCell<>() {
            @Override
            protected void updateItem(CartLineDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                Label productName = new Label(resolveProductName(item));
                productName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

                Label sizeLabel = new Label("Taille : " + resolveSizeLabel(item));
                sizeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

                Label colorLabel = new Label("Couleur : " + resolveColor(item));
                colorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

                Label unitPrice = new Label("Prix unitaire : " + PriceUtils.formatMad(item.getPrixUnitaire()));
                unitPrice.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

                Label subtotal = new Label("Sous-total : " + PriceUtils.formatMad(item.getSousTotal()));
                subtotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #d97706;");

                Spinner<Integer> spinner = new Spinner<>(1, 99, item.getQuantite());
                spinner.setEditable(true);
                spinner.setPrefWidth(90);
                spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                    if (newValue == null || newValue.equals(oldValue)) {
                        return;
                    }
                    try {
                        refresh(cartService.updateCart(item.getId(), newValue));
                        UIUtils.showSuccess("Quantite mise a jour.");
                    } catch (Exception e) {
                        UIUtils.showError(e.getMessage());
                    }
                });

                VBox qtyBox = new VBox(6,
                        new Label("Quantite"),
                        spinner
                );

                Button removeButton = ViewFactory.createSecondaryButton("Supprimer");
                removeButton.setOnAction(event -> {
                    try {
                        refresh(cartService.removeFromCart(item.getId()));
                        UIUtils.showSuccess("Ligne supprimee du panier.");
                    } catch (Exception e) {
                        UIUtils.showError(e.getMessage());
                    }
                });

                VBox left = new VBox(8, productName, sizeLabel, colorLabel, unitPrice, subtotal);
                left.setAlignment(Pos.CENTER_LEFT);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox actions = new HBox(12, qtyBox, removeButton);
                actions.setAlignment(Pos.CENTER_RIGHT);

                HBox row = new HBox(16, createProductThumb(resolveProduct(item)), left, spacer, actions);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(16));
                row.setStyle("-fx-background-color: rgba(255,255,255,0.94); -fx-background-radius: 22;");

                setGraphic(row);
            }
        });

        VBox cartCard = new VBox(14, countLabel, cartList, emptyLabel);
        cartCard.setPadding(new Insets(18));
        cartCard.setStyle(ViewFactory.cardStyle());
        HBox.setHgrow(cartCard, Priority.ALWAYS);

        Label summaryTitle = new Label("Resume");
        summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Label summaryText = new Label("Le total est mis a jour automatiquement apres chaque modification.");
        summaryText.setWrapText(true);
        summaryText.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

        totalLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #d97706;");

        Label paymentHint = new Label("Vous pouvez supprimer une ligne puis retourner au catalogue pour ajouter un nouveau produit.");
        paymentHint.setWrapText(true);
        paymentHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #52606d; "
                + "-fx-background-color: rgba(245,250,248,0.92); -fx-background-radius: 14; -fx-padding: 10;");

        VBox summaryCard = new VBox(12, summaryTitle, summaryText, totalLabel, paymentHint, checkoutButton);
        summaryCard.setPadding(new Insets(18));
        summaryCard.setPrefWidth(280);
        summaryCard.setStyle(ViewFactory.cardStyle());

        HBox body = new HBox(18, cartCard, summaryCard);
        body.setAlignment(Pos.TOP_LEFT);

        page.getChildren().addAll(heading, topActions, body);
        loadCart();
    }

    private void loadCart() {
        try {
            refresh(cartService.getCart());
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void refresh(CartDTO cart) {
        warmupLineDetails(cart);
        cartList.setItems(FXCollections.observableArrayList(cart.getLignes()));
        totalLabel.setText(PriceUtils.formatMad(cart.getTotal()));
        countLabel.setText(cart.getLignes().size() + " article(s) dans votre panier");
        emptyLabel.setVisible(cart.isEmpty());
        checkoutButton.setDisable(cart.isEmpty());
    }

    private void warmupLineDetails(CartDTO cart) {
        for (CartLineDTO line : cart.getLignes()) {
            if (!productCache.containsKey(line.getProduitId())) {
                try {
                    ProductDTO product = productService.getById(line.getProduitId());
                    productCache.put(line.getProduitId(), product);
                    for (ProductSizeDTO size : product.getTailles()) {
                        sizeLabelCache.put(size.getId(), size.getValeur());
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private String resolveProductName(CartLineDTO line) {
        if (line.getProduit() != null && line.getProduit().getNom() != null && !line.getProduit().getNom().isBlank()) {
            return line.getProduit().getNom();
        }
        ProductDTO product = productCache.get(line.getProduitId());
        return product != null && product.getNom() != null ? product.getNom() : "Produit #" + line.getProduitId();
    }

    private String resolveSizeLabel(CartLineDTO line) {
        if (line.getTaille() != null && line.getTaille().getValeur() != null && !line.getTaille().getValeur().isBlank()) {
            return line.getTaille().getValeur();
        }
        return sizeLabelCache.getOrDefault(line.getTailleId(), "#" + line.getTailleId());
    }

    private String resolveColor(CartLineDTO line) {
        ProductDTO product = resolveProduct(line);
        return product != null && product.getCouleur() != null && !product.getCouleur().isBlank()
                ? product.getCouleur()
                : "-";
    }

    private ProductDTO resolveProduct(CartLineDTO line) {
        if (line.getProduit() != null) {
            return line.getProduit();
        }
        return productCache.get(line.getProduitId());
    }

    private StackPane createProductThumb(ProductDTO product) {
        StackPane wrapper = new StackPane();
        wrapper.setPrefSize(96, 112);
        wrapper.setMinSize(96, 112);
        wrapper.setMaxSize(96, 112);
        wrapper.setStyle("-fx-background-color: #fff3df; -fx-background-radius: 18;");

        Rectangle clip = new Rectangle(96, 112);
        clip.setArcWidth(18);
        clip.setArcHeight(18);

        if (product != null && product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            try {
                ImageView imageView = new ImageView(new Image(product.getImageUrl(), 96, 112, false, true, true));
                imageView.setFitWidth(96);
                imageView.setFitHeight(112);
                imageView.setPreserveRatio(false);
                imageView.setClip(clip);
                wrapper.getChildren().add(imageView);
                return wrapper;
            } catch (Exception ignored) {
            }
        }

        Label placeholder = new Label(product != null && product.getNom() != null && !product.getNom().isBlank()
                ? product.getNom().substring(0, 1).toUpperCase()
                : "C");
        placeholder.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #d97706;");
        wrapper.getChildren().add(placeholder);
        return wrapper;
    }
}
