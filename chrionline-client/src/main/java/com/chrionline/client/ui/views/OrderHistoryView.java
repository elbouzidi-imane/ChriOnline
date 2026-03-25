package com.chrionline.client.ui.views;

import com.chrionline.client.model.OrderDTO;
import com.chrionline.client.model.OrderLineDTO;
import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.model.ProductSizeDTO;
import com.chrionline.client.service.OrderService;
import com.chrionline.client.service.ProductService;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.PriceUtils;
import com.chrionline.client.util.UIUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class OrderHistoryView extends VBox {
    private final OrderService orderService = new OrderService();
    private final ProductService productService = new ProductService();
    private final Map<Integer, ProductDTO> productCache = new HashMap<>();

    public OrderHistoryView() {
        VBox shell = ViewFactory.createPageShell(
                "Historique des commandes",
                "Retrouvez vos references, vos statuts et ouvrez le detail complet de chaque commande."
        );
        getChildren().add(shell);

        Button homeButton = ViewFactory.createSecondaryButton("Accueil");
        homeButton.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));
        Button backButton = ViewFactory.createSecondaryButton("Retour au catalogue");
        backButton.setOnAction(event -> NavigationManager.navigateTo(new ProductListView()));

        Label info = new Label("Toutes vos commandes sont affichees du plus recent au plus ancien.");
        info.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f6368;");

        TableView<OrderDTO> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-background-radius: 18; -fx-border-radius: 18;");

        TableColumn<OrderDTO, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getReference()));

        TableColumn<OrderDTO, String> dateCol = new TableColumn<>("Date");
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        dateCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getDateCommande() == null ? "" : format.format(data.getValue().getDateCommande())));

        TableColumn<OrderDTO, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getStatut()));

        TableColumn<OrderDTO, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(PriceUtils.formatMad(data.getValue().getMontantTotal())));

        TableColumn<OrderDTO, OrderDTO> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        actionCol.setCellFactory(ignored -> new TableCell<>() {
            private final Button detailButton = ViewFactory.createPrimaryButton("Voir");

            {
                detailButton.setOnAction(event -> {
                    OrderDTO order = getItem();
                    if (order != null) {
                        showDetails(order);
                    }
                });
            }

            @Override
            protected void updateItem(OrderDTO item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : detailButton);
            }
        });

        table.getColumns().setAll(refCol, dateCol, statusCol, totalCol, actionCol);

        try {
            table.getItems().setAll(orderService.getOrders());
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }

        Label summaryTitle = new Label("Mes commandes");
        summaryTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #162521;");

        VBox card = new VBox(14, ViewFactory.createTopBar(summaryTitle, homeButton, backButton), info, table);
        card.setPadding(new Insets(18));
        card.setStyle(ViewFactory.cardStyle());
        shell.getChildren().add(card);
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private void showDetails(OrderDTO order) {
        try {
            OrderDTO detailedOrder = orderService.getOrderById(order.getId());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Commande " + detailedOrder.getReference());

            VBox content = new VBox(14);
            content.setPadding(new Insets(18));
            content.setStyle("-fx-background-color: linear-gradient(to bottom, #fffaf5, #ffe7c7);");

            ScrollPane root = new ScrollPane(content);
            root.setFitToWidth(true);
            root.setStyle("-fx-background: #fffaf5; -fx-background-color: #fffaf5;");

            Label ref = new Label("Reference : " + detailedOrder.getReference());
            ref.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #162521;");

            Label meta = new Label(
                    "Statut : " + detailedOrder.getStatut()
                            + "   |   Total : " + PriceUtils.formatMad(detailedOrder.getMontantTotal())
            );
            meta.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f6368;");

            Label address = new Label("Livraison : " + (detailedOrder.getAdresseLivraison() == null ? "-" : detailedOrder.getAdresseLivraison()));
            address.setWrapText(true);
            address.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

            VBox linesBox = new VBox(10);
            if (detailedOrder.getLignes().isEmpty()) {
                Label empty = new Label("Aucune ligne de commande disponible.");
                empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #991b1b;");
                linesBox.getChildren().add(empty);
            } else {
                for (OrderLineDTO line : detailedOrder.getLignes()) {
                    ProductDTO product = resolveProduct(line.getProduitId());
                    String productName = product != null && product.getNom() != null && !product.getNom().isBlank()
                            ? product.getNom()
                            : "Produit #" + line.getProduitId();
                    String sizeValue = resolveSize(product, line.getTailleId());
                    String colorValue = product != null && product.getCouleur() != null && !product.getCouleur().isBlank()
                            ? product.getCouleur()
                            : "-";

                    Label title = new Label(productName);
                    title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #162521;");

                    Label details = new Label("Taille : " + sizeValue + "   |   Couleur : " + colorValue);
                    details.setWrapText(true);
                    details.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f6368;");

                    Label pricing = new Label(
                            "Quantite : " + line.getQuantite()
                                    + "   |   Prix unitaire : " + PriceUtils.formatMad(line.getPrixUnitaire())
                                    + "   |   Sous-total : " + PriceUtils.formatMad(line.getSousTotal())
                    );
                    pricing.setWrapText(true);
                    pricing.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

                    VBox infoBox = new VBox(6, title, details, pricing);
                    infoBox.setAlignment(Pos.CENTER_LEFT);
                    Region lineSpacer = new Region();
                    HBox.setHgrow(lineSpacer, Priority.ALWAYS);

                    HBox lineCard = new HBox(14, createProductThumb(product), infoBox, lineSpacer);
                    lineCard.setPadding(new Insets(12));
                    lineCard.setStyle("-fx-background-color: rgba(255,255,255,0.92); -fx-background-radius: 16;");
                    linesBox.getChildren().add(lineCard);
                }
            }

            Button closeButton = ViewFactory.createPrimaryButton("Fermer");
            closeButton.setOnAction(event -> stage.close());
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox footer = new HBox(10, spacer, closeButton);
            footer.setAlignment(Pos.CENTER_RIGHT);

            content.getChildren().addAll(ref, meta, address, linesBox, footer);

            Scene scene = new Scene(root, 720, 520);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private ProductDTO resolveProduct(int productId) {
        if (productCache.containsKey(productId)) {
            return productCache.get(productId);
        }
        try {
            ProductDTO product = productService.getById(productId);
            productCache.put(productId, product);
            return product;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveSize(ProductDTO product, int tailleId) {
        if (product == null) {
            return "#" + tailleId;
        }
        for (ProductSizeDTO size : product.getTailles()) {
            if (size.getId() == tailleId) {
                return size.getValeur();
            }
        }
        return "#" + tailleId;
    }

    private StackPane createProductThumb(ProductDTO product) {
        StackPane wrapper = new StackPane();
        wrapper.setPrefSize(92, 108);
        wrapper.setMinSize(92, 108);
        wrapper.setMaxSize(92, 108);
        wrapper.setStyle("-fx-background-color: #fff3df; -fx-background-radius: 18;");

        Rectangle clip = new Rectangle(92, 108);
        clip.setArcWidth(18);
        clip.setArcHeight(18);

        if (product != null && product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
            try {
                ImageView imageView = new ImageView(new Image(product.getImageUrl(), 92, 108, false, true, true));
                imageView.setFitWidth(92);
                imageView.setFitHeight(108);
                imageView.setPreserveRatio(false);
                imageView.setClip(clip);
                wrapper.getChildren().add(imageView);
                return wrapper;
            } catch (Exception ignored) {
            }
        }

        Label placeholder = new Label(product != null && product.getNom() != null && !product.getNom().isBlank()
                ? product.getNom().substring(0, 1).toUpperCase()
                : "P");
        placeholder.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #d97706;");
        wrapper.getChildren().add(placeholder);
        return wrapper;
    }
}
