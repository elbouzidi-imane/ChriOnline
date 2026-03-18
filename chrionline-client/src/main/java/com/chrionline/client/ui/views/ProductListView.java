package com.chrionline.client.ui.views;

import com.chrionline.client.model.CategoryDTO;
import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.service.ProductService;
import com.chrionline.client.session.AppSession;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.UIUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductListView extends VBox {
    private final ProductService productService = new ProductService();
    private final List<ProductDTO> allProducts = new ArrayList<>();

    public ProductListView() {
        setSpacing(18);
        setPadding(new Insets(22));
        setStyle("-fx-background-color: linear-gradient(to bottom, #fff8ef, #ffe2c6);");

        Label badge = new Label("NOUVELLE COLLECTION");
        badge.setStyle("-fx-background-color: #1f6f5f; -fx-text-fill: white; -fx-font-size: 11px; "
                + "-fx-font-weight: bold; -fx-padding: 6 10 6 10; -fx-background-radius: 999;");

        Label title = new Label("Catalogue ChriOnline");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #162521;");

        Label subtitle = new Label("Des looks plus modernes, plus colores, prets a commander.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #5b5f63;");

        VBox heading = new VBox(8, badge, title, subtitle);

        ComboBox<CategoryDTO> categoryBox = new ComboBox<>();
        categoryBox.setPromptText("Filtrer par categorie");
        categoryBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 4;");

        Button homeButton = createHeaderButton("Accueil");
        homeButton.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));

        Button historyButton = createHeaderButton("Mes commandes");
        historyButton.setOnAction(event -> NavigationManager.navigateTo(new OrderHistoryView()));

        Button cartButton = createPrimaryButton("Panier");
        cartButton.setOnAction(event -> NavigationManager.navigateTo(new CartView()));

        Button logoutButton = createHeaderButton("Deconnexion");
        logoutButton.setOnAction(event -> {
            AppSession.clear();
            NavigationManager.navigateTo(new HomeView());
        });

        HBox toolbar = ViewFactory.createTopBar(categoryBox, homeButton, historyButton, cartButton, logoutButton);

        ListView<ProductDTO> listView = new ListView<>();
        listView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        listView.setCellFactory(ignored -> new ListCell<>() {
            @Override
            protected void updateItem(ProductDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                StackPane imageBox = new StackPane();
                imageBox.setPrefSize(120, 120);
                imageBox.setMaxSize(120, 120);

                Rectangle bg = new Rectangle(120, 120);
                bg.setArcWidth(24);
                bg.setArcHeight(24);
                bg.setFill(Color.web(item.isDisponible() ? "#ffd9b8" : "#e5e7eb"));

                Label placeholder = new Label(item.getNom().substring(0, 1).toUpperCase());
                placeholder.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #7c2d12;");
                imageBox.getChildren().addAll(bg, placeholder);

                if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
                    try {
                        Image image = new Image(item.getImageUrl(), 120, 120, true, true, true);
                        if (!image.isError()) {
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(120);
                            imageView.setFitHeight(120);
                            imageView.setPreserveRatio(true);
                            imageBox.getChildren().setAll(bg, imageView);
                        }
                    } catch (Exception ignoredException) {
                    }
                }

                String category = item.getCategorie() == null ? "Sans categorie" : item.getCategorie().getNom();

                Label name = new Label(item.getNom());
                name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #162521;");

                Label categoryLabel = new Label(category);
                categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1f6f5f; "
                        + "-fx-background-color: #daf1e9; -fx-padding: 5 8 5 8; -fx-background-radius: 999;");

                Label desc = new Label(item.getDescription() == null ? "Description bientot disponible." : item.getDescription());
                desc.setWrapText(true);
                desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

                Label price = new Label(String.format("%.2f EUR", item.getPrixAffiche()));
                price.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #b45309;");

                Label stock = new Label(item.isDisponible() ? "En stock" : "Indisponible");
                stock.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (item.isDisponible() ? "#166534;" : "#991b1b;"));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button detailButton = createPrimaryButton("Voir");
                detailButton.setOnAction(event -> NavigationManager.navigateTo(new ProductDetailView(item)));

                HBox footer = new HBox(10, price, stock, spacer, detailButton);
                footer.setAlignment(Pos.CENTER_LEFT);

                VBox content = new VBox(10, categoryLabel, name, desc, footer);
                content.setAlignment(Pos.CENTER_LEFT);
                content.setFillWidth(true);

                HBox card = new HBox(18, imageBox, content);
                card.setAlignment(Pos.CENTER_LEFT);
                card.setPadding(new Insets(16));
                card.setStyle(ViewFactory.cardStyle());

                setGraphic(card);
            }
        });

        listView.setOnMouseClicked(event -> {
            ProductDTO selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                NavigationManager.navigateTo(new ProductDetailView(selected));
            }
        });

        categoryBox.setOnAction(event -> {
            CategoryDTO selected = categoryBox.getSelectionModel().getSelectedItem();
            List<ProductDTO> filtered = selected == null
                    ? allProducts
                    : allProducts.stream()
                    .filter(product -> product.getCategorie() != null && product.getCategorie().getId() == selected.getId())
                    .collect(Collectors.toList());
            listView.setItems(FXCollections.observableArrayList(filtered));
        });

        try {
            allProducts.addAll(productService.getAll());
            listView.setItems(FXCollections.observableArrayList(allProducts));
            categoryBox.setItems(FXCollections.observableArrayList(productService.getCategories()));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }

        getChildren().addAll(heading, toolbar, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
    }

    private Button createPrimaryButton(String text) {
        return ViewFactory.createPrimaryButton(text);
    }

    private Button createHeaderButton(String text) {
        return ViewFactory.createSecondaryButton(text);
    }
}
