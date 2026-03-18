package com.chrionline.client.ui.views;

import com.chrionline.client.model.CartDTO;
import com.chrionline.client.model.CartLineDTO;
import com.chrionline.client.service.CartService;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.UIUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class CartView extends VBox {
    private final CartService cartService = new CartService();
    private final TableView<CartLineDTO> table = new TableView<>();
    private final Label totalLabel = new Label();
    private final Label emptyLabel = new Label("Votre panier est vide");
    private final Button checkoutButton = ViewFactory.createPrimaryButton("Valider la commande");

    public CartView() {
        VBox shell = ViewFactory.createPageShell("Votre panier", "Verifiez vos articles avant de passer au paiement.");
        getChildren().add(shell);

        Button homeButton = ViewFactory.createSecondaryButton("Accueil");
        homeButton.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));
        Button continueButton = ViewFactory.createSecondaryButton("Continuer les achats");
        continueButton.setOnAction(event -> NavigationManager.navigateTo(new ProductListView()));
        checkoutButton.setOnAction(event -> NavigationManager.navigateTo(new CheckoutView()));

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

        TableColumn<CartLineDTO, String> unitPriceCol = new TableColumn<>("Prix unitaire");
        unitPriceCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f EUR", data.getValue().getPrixUnitaire())));

        TableColumn<CartLineDTO, String> subtotalCol = new TableColumn<>("Sous-total");
        subtotalCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f EUR", data.getValue().getSousTotal())));

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
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        emptyLabel.setVisible(false);
        emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #991b1b;");

        Label summaryTitle = new Label("Resume");
        summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #162521;");

        totalLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #b45309;");

        VBox summaryCard = new VBox(10, summaryTitle, totalLabel, checkoutButton);
        summaryCard.setPadding(new Insets(18));
        summaryCard.setStyle(ViewFactory.cardStyle());
        summaryCard.setPrefWidth(240);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox toolbar = new HBox(10, homeButton, continueButton, spacer);

        HBox content = new HBox(18, table, summaryCard);
        HBox.setHgrow(table, Priority.ALWAYS);

        shell.getChildren().addAll(toolbar, content, emptyLabel);
        VBox.setVgrow(table, Priority.ALWAYS);
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
        totalLabel.setText("Total : " + String.format("%.2f EUR", cart.getTotal()));
        emptyLabel.setVisible(cart.isEmpty());
        checkoutButton.setDisable(cart.isEmpty());
    }
}
