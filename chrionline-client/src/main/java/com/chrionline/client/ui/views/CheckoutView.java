package com.chrionline.client.ui.views;

import com.chrionline.client.model.CartDTO;
import com.chrionline.client.model.CartLineDTO;
import com.chrionline.client.service.CartService;
import com.chrionline.client.service.OrderService;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class CheckoutView extends VBox {
    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();

    public CheckoutView() {
        VBox shell = ViewFactory.createPageShell("Finaliser la commande", "Choisissez votre livraison et votre mode de paiement.");
        getChildren().add(shell);

        Button backButton = ViewFactory.createSecondaryButton("Retour au panier");
        backButton.setOnAction(event -> NavigationManager.navigateTo(new CartView()));

        ListView<CartLineDTO> summaryList = new ListView<>();
        summaryList.setCellFactory(ignored -> new ListCell<>() {
            @Override
            protected void updateItem(CartLineDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label title = new Label(item.getProduit().getNom() + " x" + item.getQuantite());
                title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                Label price = new Label(String.format("%.2f EUR", item.getSousTotal()));
                price.setStyle("-fx-font-size: 14px; -fx-text-fill: #b45309; -fx-font-weight: bold;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                setGraphic(new HBox(10, title, spacer, price));
            }
        });

        TextField addressField = new TextField();
        addressField.setPromptText("Adresse de livraison");
        addressField.setStyle("-fx-background-radius: 14; -fx-padding: 12;");

        ComboBox<String> paymentBox = new ComboBox<>(FXCollections.observableArrayList("CARTE_BANCAIRE", "PAYPAL", "FICTIF"));
        paymentBox.setPromptText("Mode de paiement");
        paymentBox.setStyle("-fx-background-radius: 14;");

        ComboBox<String> deliveryBox = new ComboBox<>(FXCollections.observableArrayList("STANDARD", "EXPRESS", "POINT_RELAIS"));
        deliveryBox.setPromptText("Mode de livraison");
        deliveryBox.setStyle("-fx-background-radius: 14;");

        Button confirmButton = ViewFactory.createPrimaryButton("Confirmer la commande");
        confirmButton.setOnAction(event -> {
            if (addressField.getText().isBlank()) {
                UIUtils.showError("Adresse livraison obligatoire.");
                return;
            }
            if (paymentBox.getValue() == null || deliveryBox.getValue() == null) {
                UIUtils.showError("Selectionnez le mode de paiement et de livraison.");
                return;
            }
            try {
                String reference = orderService.placeOrder(addressField.getText().trim(), paymentBox.getValue(), deliveryBox.getValue());
                cartService.clearCart();
                UIUtils.showInfo("Commande " + reference + " confirmee !");
                NavigationManager.navigateTo(new ProductListView());
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        VBox formCard = new VBox(12,
                new Label("Adresse de livraison"), addressField,
                new Label("Paiement"), paymentBox,
                new Label("Livraison"), deliveryBox,
                confirmButton);
        formCard.setPadding(new Insets(18));
        formCard.setStyle(ViewFactory.cardStyle());
        formCard.setPrefWidth(300);

        try {
            CartDTO cart = cartService.getCart();
            summaryList.setItems(FXCollections.observableArrayList(cart.getLignes()));
            Label totalLabel = new Label("Total : " + String.format("%.2f EUR", cart.getTotal()));
            totalLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #b45309;");

            Label summaryTitle = new Label("Resume de commande");
            summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            VBox summaryCard = new VBox(12, summaryTitle, summaryList, totalLabel);
            summaryCard.setPadding(new Insets(18));
            summaryCard.setStyle(ViewFactory.cardStyle());
            VBox.setVgrow(summaryList, Priority.ALWAYS);

            HBox body = new HBox(18, summaryCard, formCard);
            HBox.setHgrow(summaryCard, Priority.ALWAYS);
            body.setAlignment(Pos.TOP_LEFT);

            shell.getChildren().addAll(backButton, body);
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
            NavigationManager.navigateTo(new CartView());
        }
    }
}
