package com.chrionline.client.ui.views;

import com.chrionline.client.model.CartDTO;
import com.chrionline.client.model.CartLineDTO;
import com.chrionline.client.service.CartService;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.PriceUtils;
import com.chrionline.client.util.UIUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class CartView extends ScrollPane {
    private final CartService cartService = new CartService();
    private final TableView<CartLineDTO> table = new TableView<>();
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

        Label subtitle = new Label("Verifiez vos articles, ajustez les quantites puis passez au paiement.");
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

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-background-radius: 18; -fx-border-radius: 18;");
        table.setPrefHeight(360);

        TableColumn<CartLineDTO, String> productCol = new TableColumn<>("Produit");
        productCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getProduit().getNom()));

        TableColumn<CartLineDTO, String> sizeCol = new TableColumn<>("Taille");
        sizeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTaille().getValeur()));

        TableColumn<CartLineDTO, CartLineDTO> qtyCol = new TableColumn<>("Quantite");
        qtyCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        qtyCol.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(CartLineDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
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
                setGraphic(spinner);
            }
        });

        TableColumn<CartLineDTO, String> unitPriceCol = new TableColumn<>("Prix");
        unitPriceCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(PriceUtils.formatMad(data.getValue().getPrixUnitaire())));

        TableColumn<CartLineDTO, String> subtotalCol = new TableColumn<>("Sous-total");
        subtotalCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(PriceUtils.formatMad(data.getValue().getSousTotal())));

        TableColumn<CartLineDTO, CartLineDTO> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        actionCol.setCellFactory(ignored -> new TableCell<>() {
            private final Button removeButton = ViewFactory.createSecondaryButton("Retirer");

            {
                removeButton.setOnAction(event -> {
                    CartLineDTO line = getItem();
                    if (line == null) {
                        return;
                    }
                    try {
                        refresh(cartService.removeFromCart(line.getId()));
                    } catch (Exception e) {
                        UIUtils.showError(e.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(CartLineDTO item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : removeButton);
            }
        });

        table.getColumns().setAll(productCol, sizeCol, qtyCol, unitPriceCol, subtotalCol, actionCol);

        countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5b5f63;");
        emptyLabel.setVisible(false);
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #991b1b;");

        VBox cartCard = new VBox(14, countLabel, table, emptyLabel);
        cartCard.setPadding(new Insets(18));
        cartCard.setStyle(ViewFactory.cardStyle());
        HBox.setHgrow(cartCard, Priority.ALWAYS);

        Label summaryTitle = new Label("Resume");
        summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Label summaryText = new Label("Tous vos articles sont prets pour la validation.");
        summaryText.setWrapText(true);
        summaryText.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

        totalLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #d97706;");

        Label paymentHint = new Label("Paiement simule disponible par carte, PayPal ou mode fictif.");
        paymentHint.setWrapText(true);
        paymentHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #52606d; "
                + "-fx-background-color: rgba(245,250,248,0.92); -fx-background-radius: 14; -fx-padding: 10;");

        VBox summaryCard = new VBox(12, summaryTitle, summaryText, totalLabel, paymentHint, checkoutButton);
        summaryCard.setPadding(new Insets(18));
        summaryCard.setPrefWidth(260);
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
        table.setItems(FXCollections.observableArrayList(cart.getLignes()));
        totalLabel.setText(PriceUtils.formatMad(cart.getTotal()));
        countLabel.setText(cart.getLignes().size() + " article(s) dans votre panier");
        emptyLabel.setVisible(cart.isEmpty());
        checkoutButton.setDisable(cart.isEmpty());
    }
}
