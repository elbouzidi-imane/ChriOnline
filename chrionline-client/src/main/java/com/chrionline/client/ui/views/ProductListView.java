package com.chrionline.client.ui.views;

import com.chrionline.client.model.CategoryDTO;
import com.chrionline.client.model.OrderDTO;
import com.chrionline.client.model.OrderLineDTO;
import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.service.OrderService;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductListView extends VBox {
    private static final CategoryDTO ALL_CATEGORY = createAllCategory();

    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final List<ProductDTO> allProducts = new ArrayList<>();
    private final VBox recommendationsSection = new VBox(14);

    public ProductListView() {
        setSpacing(20);
        setPadding(new Insets(26));
        setStyle(ViewFactory.pageBackground());

        VBox heading = new VBox(8);
        Label title = new Label("Catalogue ChriOnline");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Label subtitle = new Label(AppSession.isLoggedIn()
                ? "Filtrez les categories, ouvrez les fiches et ajoutez vos coups de coeur au panier."
                : "Parcourez la collection librement avant de vous connecter pour commander.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #5b5f63;");
        subtitle.setWrapText(true);

        Label capsule = new Label("SELECTIONS  •  NOUVEAUTES  •  VENTE EN LIGNE");
        capsule.setStyle("-fx-background-color: rgba(255,246,235,0.94); -fx-text-fill: #a6621e; "
                + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8 14 8 14; -fx-background-radius: 999;");

        heading.getChildren().addAll(capsule, title, subtitle);

        ComboBox<CategoryDTO> categoryBox = new ComboBox<>();
        categoryBox.setPromptText("Filtrer par categorie");
        categoryBox.setStyle(ViewFactory.inputStyle());
        categoryBox.setPrefWidth(250);

        HBox toolbar;
        if (AppSession.isLoggedIn()) {
            Button homeButton = ViewFactory.createSecondaryButton("Accueil");
            homeButton.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));

            Button profileButton = ViewFactory.createSecondaryButton("Mon profil");
            profileButton.setOnAction(event -> NavigationManager.navigateTo(new ProfileView()));

            Button historyButton = ViewFactory.createSecondaryButton("Mes commandes");
            historyButton.setOnAction(event -> NavigationManager.navigateTo(new OrderHistoryView()));

            Button cartButton = ViewFactory.createPrimaryButton("Panier");
            cartButton.setOnAction(event -> NavigationManager.navigateTo(new CartView()));

            Button logoutButton = ViewFactory.createSecondaryButton("Deconnexion");
            logoutButton.setOnAction(event -> {
                AppSession.clear();
                NavigationManager.navigateTo(new HomeView());
            });

            toolbar = ViewFactory.createTopBar(categoryBox, homeButton, profileButton, historyButton, cartButton, logoutButton);
        } else {
            Button homeButton = ViewFactory.createSecondaryButton("Accueil");
            homeButton.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));

            Button loginButton = ViewFactory.createSecondaryButton("Se connecter");
            loginButton.setOnAction(event -> NavigationManager.navigateTo(new LoginView()));

            Button registerButton = ViewFactory.createPrimaryButton("S'inscrire");
            registerButton.setOnAction(event -> NavigationManager.navigateTo(new RegisterView()));

            toolbar = ViewFactory.createTopBar(categoryBox, homeButton, loginButton, registerButton);
        }

        HBox summaryRow = new HBox(16,
                createInfoTile("Navigation simple", "Des cartes plus lisibles et des actions plus nettes."),
                createInfoTile("Categories", "Un filtre direct pour isoler rapidement chaque univers."),
                createInfoTile("Fiche produit", "Acces detaille avant ajout au panier."));

        recommendationsSection.setVisible(false);
        recommendationsSection.setManaged(false);

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
                imageBox.setPrefSize(146, 146);
                imageBox.setMaxSize(146, 146);

                Rectangle bg = new Rectangle(146, 146);
                bg.setArcWidth(30);
                bg.setArcHeight(30);
                bg.setFill(Color.web(item.isDisponible() ? "#f3c99c" : "#dde1e4"));

                Label placeholder = new Label(item.getNom().substring(0, 1).toUpperCase());
                placeholder.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #7c3e10;");
                imageBox.getChildren().addAll(bg, placeholder);

                if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {
                    try {
                        Image image = new Image(item.getImageUrl(), 146, 146, true, true, true);
                        if (!image.isError()) {
                            ImageView imageView = new ImageView(image);
                            imageView.setFitWidth(146);
                            imageView.setFitHeight(146);
                            imageView.setPreserveRatio(true);
                            imageBox.getChildren().setAll(bg, imageView);
                        }
                    } catch (Exception ignoredException) {
                    }
                }

                VBox content = new VBox(10);
                content.setAlignment(Pos.CENTER_LEFT);

                if (item.getCategorie() != null && item.getCategorie().getNom() != null && !item.getCategorie().getNom().isBlank()) {
                    Label categoryLabel = new Label(item.getCategorie().getNom());
                    categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1f6f5f; "
                            + "-fx-background-color: #daf1e9; -fx-padding: 6 10 6 10; -fx-background-radius: 999;");
                    content.getChildren().add(categoryLabel);
                }

                Label name = new Label(item.getNom());
                name.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

                Label desc = new Label(item.getDescription() == null ? "Description bientot disponible." : item.getDescription());
                desc.setWrapText(true);
                desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

                HBox priceRow = new HBox(10);
                priceRow.setAlignment(Pos.CENTER_LEFT);
                Label price = new Label(PriceUtils.formatMad(item.getPrixAffiche()));
                price.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #cc6b1d;");
                priceRow.getChildren().add(price);

                if (item.hasReduction()) {
                    Label oldPrice = new Label(PriceUtils.formatMad(item.getPrixOriginal()));
                    oldPrice.setStyle("-fx-font-size: 13px; -fx-text-fill: #8a8f98; -fx-strikethrough: true;");
                    Label promoBadge = new Label("-" + item.getReductionPercentage() + "%");
                    promoBadge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; "
                            + "-fx-background-color: #c2410c; -fx-padding: 5 8 5 8; -fx-background-radius: 999;");
                    priceRow.getChildren().addAll(oldPrice, promoBadge);
                }

                Label stock = new Label(item.isDisponible() ? "Disponible" : "Rupture");
                stock.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: "
                        + (item.isDisponible() ? "#166534;" : "#991b1b;")
                        + "-fx-background-color: " + (item.isDisponible() ? "#edf9ef;" : "#fdecec;")
                        + "-fx-padding: 6 10 6 10; -fx-background-radius: 999;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button detailButton = ViewFactory.createPrimaryButton("Voir la fiche");
                detailButton.setOnAction(event -> NavigationManager.navigateTo(new ProductDetailView(item)));

                HBox footer = new HBox(12, priceRow, stock, spacer, detailButton);
                footer.setAlignment(Pos.CENTER_LEFT);

                content.getChildren().addAll(name, desc, footer);

                HBox card = new HBox(20, imageBox, content);
                card.setAlignment(Pos.CENTER_LEFT);
                card.setPadding(new Insets(18));
                card.setStyle(ViewFactory.elevatedCardStyle());

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
                listView.setItems(FXCollections.observableArrayList(filterAvailable(products)));
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        try {
            allProducts.addAll(productService.getAll());
            listView.setItems(FXCollections.observableArrayList(filterAvailable(allProducts)));

            List<CategoryDTO> categories = new ArrayList<>();
            categories.add(ALL_CATEGORY);
            categories.addAll(productService.getCategories());
            categoryBox.setItems(FXCollections.observableArrayList(categories));
            categoryBox.getSelectionModel().selectFirst();

            buildRecommendations();
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }

        getChildren().addAll(heading, toolbar, summaryRow, recommendationsSection, listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
    }

    private void buildRecommendations() {
        if (!AppSession.isLoggedIn()) {
            return;
        }

        try {
            List<ProductDTO> recommendations = computeRecommendations();
            if (recommendations.isEmpty()) {
                return;
            }

            Label title = new Label("Pour vous");
            title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

            Label subtitle = new Label("Suggestions basees sur vos categories deja commandees.");
            subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

            HBox row = new HBox(16);
            for (ProductDTO product : recommendations) {
                row.getChildren().add(createRecommendationCard(product));
            }

            recommendationsSection.getChildren().setAll(title, subtitle, row);
            recommendationsSection.setVisible(true);
            recommendationsSection.setManaged(true);
        } catch (Exception e) {
            System.err.println("ProductListView.buildRecommendations : " + e.getMessage());
        }
    }

    private List<ProductDTO> computeRecommendations() throws Exception {
        Map<Integer, ProductDTO> productsById = allProducts.stream()
                .collect(Collectors.toMap(ProductDTO::getId, product -> product, (left, right) -> left));

        List<OrderDTO> orders = orderService.getOrders();
        Map<Integer, Integer> categoryScores = new HashMap<>();
        Map<Integer, Integer> purchasedProducts = new HashMap<>();

        for (OrderDTO order : orders) {
            for (OrderLineDTO line : order.getLignes()) {
                ProductDTO orderedProduct = productsById.get(line.getProduitId());
                if (orderedProduct == null) {
                    continue;
                }
                purchasedProducts.merge(orderedProduct.getId(), line.getQuantite(), Integer::sum);
                if (orderedProduct.getCategorie() != null) {
                    categoryScores.merge(orderedProduct.getCategorie().getId(), line.getQuantite(), Integer::sum);
                }
            }
        }

        if (categoryScores.isEmpty()) {
            return List.of();
        }

        return allProducts.stream()
                .filter(ProductDTO::isDisponible)
                .filter(product -> !purchasedProducts.containsKey(product.getId()))
                .filter(product -> product.getCategorie() != null && categoryScores.containsKey(product.getCategorie().getId()))
                .sorted(Comparator
                        .comparingInt((ProductDTO product) -> categoryScores.getOrDefault(product.getCategorie().getId(), 0))
                        .reversed()
                        .thenComparing(Comparator.comparingInt((ProductDTO product) -> product.hasReduction() ? 1 : 0).reversed())
                        .thenComparing(ProductDTO::getNom, String.CASE_INSENSITIVE_ORDER))
                .limit(4)
                .toList();
    }

    private HBox createRecommendationCard(ProductDTO product) {
        Label category = new Label(product.getCategorie() == null ? "Selection" : product.getCategorie().getNom());
        category.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #1f6f5f;");

        Label name = new Label(product.getNom());
        name.setWrapText(true);
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Label price = new Label(PriceUtils.formatMad(product.getPrixAffiche()));
        price.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #cc6b1d;");

        Button button = ViewFactory.createSecondaryButton("Voir");
        button.setOnAction(event -> NavigationManager.navigateTo(new ProductDetailView(product)));

        VBox content = new VBox(8, category, name, price, button);
        content.setPadding(new Insets(18));
        content.setPrefWidth(220);
        content.setStyle(ViewFactory.elevatedCardStyle());

        return new HBox(content);
    }

    private List<ProductDTO> filterAvailable(List<ProductDTO> products) {
        return products.stream()
                .filter(ProductDTO::isDisponible)
                .collect(Collectors.toList());
    }

    private VBox createInfoTile(String title, String text) {
        Label top = new Label(title);
        top.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #17342d;");
        Label bottom = new Label(text);
        bottom.setWrapText(true);
        bottom.setStyle("-fx-font-size: 13px; -fx-text-fill: #55616a;");
        VBox box = new VBox(6, top, bottom);
        box.setPadding(new Insets(18));
        box.setPrefWidth(280);
        box.setStyle(ViewFactory.cardStyle());
        return box;
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
}
