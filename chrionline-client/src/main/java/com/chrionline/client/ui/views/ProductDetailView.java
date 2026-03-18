package com.chrionline.client.ui.views;

import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.model.ProductSizeDTO;
import com.chrionline.client.service.CartService;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ProductDetailView extends VBox {
    private final CartService cartService = new CartService();

    public ProductDetailView(ProductDTO product) {
        setSpacing(18);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: linear-gradient(to bottom, #fff8ef, #ffe2c6);");

        Button homeButton = ViewFactory.createSecondaryButton("Accueil");
        homeButton.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));
        Button backButton = ViewFactory.createSecondaryButton("Retour");
        backButton.setOnAction(event -> NavigationManager.navigateTo(new ProductListView()));

        StackPane imageBox = new StackPane();
        imageBox.setPrefSize(340, 340);

        Rectangle bg = new Rectangle(340, 340);
        bg.setArcWidth(28);
        bg.setArcHeight(28);
        bg.setFill(Color.web("#ffd9b8"));

        Label placeholder = new Label(product.getNom().substring(0, 1).toUpperCase());
        placeholder.setStyle("-fx-font-size: 84px; -fx-font-weight: bold; -fx-text-fill: #7c2d12;");
        imageBox.getChildren().addAll(bg, placeholder);

        if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            try {
                Image image = new Image(product.getImageUrl(), 320, 320, true, true, true);
                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(320);
                    imageView.setFitHeight(320);
                    imageView.setPreserveRatio(true);
                    imageBox.getChildren().setAll(bg, imageView);
                }
            } catch (Exception ignoredException) {
            }
        }

        Label category = new Label(product.getCategorie() == null ? "Sans categorie" : product.getCategorie().getNom());
        category.setStyle("-fx-font-size: 12px; -fx-text-fill: #1f6f5f; "
                + "-fx-background-color: #daf1e9; -fx-padding: 5 8 5 8; -fx-background-radius: 999;");

        Label title = new Label(product.getNom());
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #162521;");

        Label description = new Label(product.getDescription() == null ? "Description bientot disponible." : product.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #4b5563;");

        Label price = new Label(String.format("%.2f EUR", product.getPrixAffiche()));
        price.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #b45309;");

        Label info = new Label("Matiere : " + safe(product.getMatiere()) + "   |   Couleur : " + safe(product.getCouleur()));
        info.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        ToggleGroup sizeGroup = new ToggleGroup();
        VBox sizesBox = new VBox(8);
        Label sizesTitle = new Label("Tailles disponibles");
        sizesTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        sizesBox.getChildren().add(sizesTitle);
        for (ProductSizeDTO size : product.getTailles()) {
            if (!size.isDisponible()) {
                continue;
            }
            RadioButton button = new RadioButton(size.getValeur() + " (" + size.getStock() + " en stock)");
            button.setUserData(size);
            button.setToggleGroup(sizeGroup);
            button.setStyle("-fx-font-size: 14px;");
            sizesBox.getChildren().add(button);
        }

        Spinner<Integer> quantitySpinner = new Spinner<>(1, 20, 1);
        quantitySpinner.setEditable(true);

        Button addButton = ViewFactory.createPrimaryButton("Ajouter au panier");
        addButton.setOnAction(event -> {
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

        VBox right = new VBox(14, category, title, description, price, info, sizesBox, new Label("Quantite"), quantitySpinner, addButton);
        right.setAlignment(Pos.TOP_LEFT);
        right.setPadding(new Insets(18));
        right.setStyle("-fx-background-color: rgba(255,255,255,0.92); -fx-background-radius: 24;");
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox content = new HBox(24, imageBox, right);
        content.setAlignment(Pos.TOP_LEFT);

        HBox actions = new HBox(10, homeButton, backButton);
        getChildren().addAll(actions, content);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
