package com.chrionline.client.ui.views;

import com.chrionline.client.model.CategoryDTO;
import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.service.ProductService;
import com.chrionline.client.session.AppSession;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.PriceUtils;
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
import java.util.Locale;
import java.util.stream.Collectors;

public class ProductListView extends VBox {
    private static final CategoryDTO ALL_CATEGORY = createAllCategory();

    private final ProductService productService = new ProductService();
    private final List<ProductDTO> allProducts = new ArrayList<>();

    public ProductListView() {
        setSpacing(18);
        setPadding(new Insets(22));
        setStyle("-fx-background-color: linear-gradient(to bottom, #fff8ef, #ffe4c4 65%, #edf8f3);");

        Label title = new Label("Catalogue ChriOnline");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Label subtitle = new Label(AppSession.isLoggedIn()
                ? "Filtrez les categories, ouvrez les details et ajoutez vos articles au panier."
                : "Un visiteur peut consulter les produits et les details avant de se connecter pour commander.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #5b5f63;");
        subtitle.setWrapText(true);

        VBox heading = new VBox(8, title, subtitle);

        ComboBox<CategoryDTO> categoryBox = new ComboBox<>();
        categoryBox.setPromptText("Filtrer par categorie");
        categoryBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 4;");
        categoryBox.setPrefWidth(230);

        Button homeButton = createHeaderButton("Accueil");
        homeButton.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));

        HBox toolbar;
        if (AppSession.isLoggedIn()) {
            Button profileButton = createHeaderButton("Mon profil");
            profileButton.setOnAction(event -> NavigationManager.navigateTo(new ProfileView()));

            Button historyButton = createHeaderButton("Mes commandes");
            historyButton.setOnAction(event -> NavigationManager.navigateTo(new OrderHistoryView()));

            Button cartButton = createPrimaryButton("Panier");
            cartButton.setOnAction(event -> NavigationManager.navigateTo(new CartView()));

            Button logoutButton = createHeaderButton("Deconnexion");
            logoutButton.setOnAction(event -> {
                AppSession.clear();
                NavigationManager.navigateTo(new HomeView());
            });

            toolbar = ViewFactory.createTopBar(categoryBox, homeButton, profileButton, historyButton, cartButton, logoutButton);
        } else {
            Button loginButton = createHeaderButton("Se connecter");
            loginButton.setOnAction(event -> NavigationManager.navigateTo(new LoginView()));

            Button registerButton = createPrimaryButton("S'inscrire");
            registerButton.setOnAction(event -> NavigationManager.navigateTo(new RegisterView()));

            toolbar = ViewFactory.createTopBar(categoryBox, homeButton, loginButton, registerButton);
        }

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
                imageBox.setPrefSize(128, 128);
                imageBox.setMaxSize(128, 128);

                Rectangle bg = new Rectangle(128, 128);
                bg.setArcWidth(26);
                bg.setArcHeight(26);
                bg.setFill(Color.web(item.isDisponible() ? "#ffd9b8" : "#e5e7eb"));

                Label placeholder = new Label(item.getNom().substring(0, 1).toUpperCase());
                placeholder.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #7c2d12;");
                imageBox.getChildren().addAll(bg, placeholder);

                if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
                    try {
                        Image image = new Image(item.getImageUrl(), 128, 128, true, true, true);
                        if (!image.isError()) {
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(128);
                            imageView.setFitHeight(128);
                            imageView.setPreserveRatio(true);
                            imageBox.getChildren().setAll(bg, imageView);
                        }
                    } catch (Exception ignoredException) {
                    }
                }

                Label name = new Label(item.getNom());
                name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

                Label desc = new Label(item.getDescription() == null ? "Description bientot disponible." : item.getDescription());
                desc.setWrapText(true);
                desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

                Label price = new Label(PriceUtils.formatMad(item.getPrixAffiche()));
                price.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #d97706;");

                Label stock = new Label(item.isDisponible() ? "En stock" : "Indisponible");
                stock.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (item.isDisponible() ? "#166534;" : "#991b1b;"));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button detailButton = createPrimaryButton("Voir");
                detailButton.setOnAction(event -> NavigationManager.navigateTo(new ProductDetailView(item)));

                HBox footer = new HBox(10, price, stock, spacer, detailButton);
                footer.setAlignment(Pos.CENTER_LEFT);

                VBox content = new VBox(10);
                if (item.getCategorie() != null && item.getCategorie().getNom() != null && !item.getCategorie().getNom().isBlank()) {
                    Label categoryLabel = new Label(item.getCategorie().getNom());
                    categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1f6f5f; "
                            + "-fx-background-color: #daf1e9; -fx-padding: 5 8 5 8; -fx-background-radius: 999;");
                    content.getChildren().add(categoryLabel);
                }
                content.getChildren().addAll(name, desc, footer);
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
            try {
                List<ProductDTO> products = selected == null || selected.getId() == -1
                        ? allProducts
                        : productService.getByCategory(selected.getId());
                listView.setItems(FXCollections.observableArrayList(products));
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        try {
            allProducts.addAll(productService.getAll());
            listView.setItems(FXCollections.observableArrayList(allProducts));

            List<CategoryDTO> categories = new ArrayList<>();
            categories.add(ALL_CATEGORY);
            categories.addAll(productService.getCategories());
            categoryBox.setItems(FXCollections.observableArrayList(categories));
            categoryBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }

        getChildren().addAll(heading, toolbar, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
    }

    private List<ProductDTO> filterProducts(CategoryDTO selected) {
        if (selected == null || selected.getId() == -1) {
            return allProducts;
        }

        String selectedName = normalize(selected.getNom());
        return allProducts.stream()
                .filter(product -> product.getCategorie() != null)
                .filter(product -> product.getCategorie().getId() == selected.getId()
                        || normalize(product.getCategorie().getNom()).equals(selectedName))
                .collect(Collectors.toList());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static CategoryDTO createAllCategory() {
        return new CategoryDTO() {
            @Override
            public int getId() {
                return -1;
            }

            @Override
            public String getNom() {
                return "Toutes les categories";
            }

            @Override
            public String toString() {
                return getNom();
            }
        };
    }

    private Button createPrimaryButton(String text) {
        return ViewFactory.createPrimaryButton(text);
    }

    private Button createHeaderButton(String text) {
        return ViewFactory.createSecondaryButton(text);
    }
}
