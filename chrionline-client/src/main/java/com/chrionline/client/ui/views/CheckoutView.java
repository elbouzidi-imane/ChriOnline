package com.chrionline.client.ui.views;

import com.chrionline.client.model.CartDTO;
import com.chrionline.client.model.CartLineDTO;
import com.chrionline.client.service.CartService;
import com.chrionline.client.service.OrderService;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.PriceUtils;
import com.chrionline.client.util.UIUtils;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;

public class CheckoutView extends ScrollPane {
    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();

    private final TextField cardNumberField = createField("Numero de carte bancaire");
    private final TextField cardHolderField = createField("Nom sur la carte");
    private final TextField expiryField = createField("Date d'expiration MM/AA");
    private final TextField cvvField = createField("Code CVV");
    private final TextField paypalEmailField = createField("Email PayPal");

    public CheckoutView() {
        VBox page = new VBox(18);
        page.setPadding(new Insets(22));
        page.setStyle("-fx-background-color: linear-gradient(to bottom, #fff8ef, #ffe5c9 68%, #edf8f3);");

        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background: #fff8ef; -fx-background-color: #fff8ef;");
        setContent(page);

        Label title = new Label("Finaliser la commande");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Label subtitle = new Label("Renseignez la livraison et choisissez votre paiement pour confirmer votre achat.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #5b5f63;");
        subtitle.setWrapText(true);

        VBox heading = new VBox(8, title, subtitle);

        Button backButton = ViewFactory.createSecondaryButton("Retour au panier");
        backButton.setOnAction(event -> NavigationManager.navigateTo(new CartView()));

        ListView<CartLineDTO> summaryList = new ListView<>();
        summaryList.setPrefHeight(280);
        summaryList.setCellFactory(ignored -> new ListCell<>() {
            @Override
            protected void updateItem(CartLineDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label titleLabel = new Label(item.getProduit().getNom() + " x" + item.getQuantite());
                titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #12372e;");
                Label detailLabel = new Label("Taille " + item.getTaille().getValeur());
                detailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
                VBox left = new VBox(4, titleLabel, detailLabel);

                Label priceLabel = new Label(PriceUtils.formatMad(item.getSousTotal()));
                priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #d97706;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                setGraphic(new HBox(10, left, spacer, priceLabel));
            }
        });

        TextField addressField = createField("Adresse de livraison");

        ComboBox<String> paymentBox = new ComboBox<>(FXCollections.observableArrayList("CARTE_BANCAIRE", "PAYPAL", "FICTIF"));
        paymentBox.setPromptText("Mode de paiement");
        paymentBox.setStyle("-fx-background-radius: 14;");

        ComboBox<String> deliveryBox = new ComboBox<>(FXCollections.observableArrayList("STANDARD", "EXPRESS", "POINT_RELAIS"));
        deliveryBox.setPromptText("Mode de livraison");
        deliveryBox.setStyle("-fx-background-radius: 14;");

        VBox paymentDetailsBox = new VBox(10);
        paymentDetailsBox.setPadding(new Insets(14));
        paymentDetailsBox.setStyle("-fx-background-color: rgba(255,255,255,0.82); -fx-background-radius: 18;");

        Label paymentDetailTitle = new Label("Informations de paiement");
        paymentDetailTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Hyperlink paypalLink = new Hyperlink("Ouvrir PayPal");
        paypalLink.setOnAction(event -> openPaypal());

        Label fictiveLabel = new Label("Mode fictif selectionne : aucune information bancaire supplementaire requise.");
        fictiveLabel.setWrapText(true);
        fictiveLabel.setStyle("-fx-text-fill: #52606d;");

        paymentBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            paymentDetailsBox.getChildren().clear();
            if ("CARTE_BANCAIRE".equals(newValue)) {
                paymentDetailsBox.getChildren().addAll(paymentDetailTitle, createCardPaymentForm());
            } else if ("PAYPAL".equals(newValue)) {
                paymentDetailsBox.getChildren().addAll(
                        paymentDetailTitle,
                        createInfoLabel("Saisissez votre email PayPal puis continuez vers le site PayPal."),
                        paypalEmailField,
                        paypalLink
                );
            } else if ("FICTIF".equals(newValue)) {
                paymentDetailsBox.getChildren().addAll(paymentDetailTitle, fictiveLabel);
            }
        });

        Button confirmButton = new Button("Confirmer la commande");
        confirmButton.setStyle("-fx-background-color: #eb8b1b; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 16; -fx-padding: 13 18 13 18;");
        confirmButton.setMaxWidth(Double.MAX_VALUE);
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
                if ("CARTE_BANCAIRE".equals(paymentBox.getValue())) {
                    validateCard();
                } else if ("PAYPAL".equals(paymentBox.getValue())) {
                    validatePaypal();
                }
            } catch (IllegalArgumentException e) {
                UIUtils.showError(e.getMessage());
                return;
            }

            try {
                String reference = orderService.placeOrder(addressField.getText().trim(), paymentBox.getValue(), deliveryBox.getValue());
                cartService.clearCart();
                UIUtils.showSuccess("Commande " + reference + " confirmee !");
                NavigationManager.navigateTo(new ProductListView());
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        });

        VBox formCard = new VBox(12,
                new Label("Adresse de livraison"), addressField,
                new Separator(),
                new Label("Mode de paiement"), paymentBox,
                paymentDetailsBox,
                new Separator(),
                new Label("Mode de livraison"), deliveryBox,
                confirmButton
        );
        formCard.setPadding(new Insets(18));
        formCard.setPrefWidth(340);
        formCard.setStyle(ViewFactory.cardStyle());

        try {
            CartDTO cart = cartService.getCart();
            summaryList.setItems(FXCollections.observableArrayList(cart.getLignes()));

            Label orderSummary = new Label("Resume");
            orderSummary.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

            Label summaryText = new Label("Vos articles seront prepares apres validation du paiement simule.");
            summaryText.setWrapText(true);
            summaryText.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

            Label totalLabel = new Label(PriceUtils.formatMad(cart.getTotal()));
            totalLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #d97706;");

            Label totalHint = new Label("Total a payer");
            totalHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

            VBox summaryCard = new VBox(12, orderSummary, summaryText, summaryList, totalHint, totalLabel);
            summaryCard.setPadding(new Insets(18));
            summaryCard.setStyle(ViewFactory.cardStyle());
            HBox.setHgrow(summaryCard, Priority.ALWAYS);

            HBox body = new HBox(18, summaryCard, formCard);
            body.setAlignment(Pos.TOP_LEFT);

            page.getChildren().addAll(heading, backButton, body);
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
            NavigationManager.navigateTo(new CartView());
        }
    }

    private void validateCard() {
        String cardNumber = cardNumberField.getText().replace(" ", "");
        if (cardNumberField.getText().isBlank() || cardHolderField.getText().isBlank()
                || expiryField.getText().isBlank() || cvvField.getText().isBlank()) {
            throw new IllegalArgumentException("Remplissez tous les champs de la carte bancaire.");
        }
        if (!cardNumber.matches("\\d{16}")) {
            throw new IllegalArgumentException("Le numero de carte doit contenir 16 chiffres.");
        }
        if (!expiryField.getText().trim().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new IllegalArgumentException("La date d'expiration doit etre au format MM/AA.");
        }
        if (!cvvField.getText().trim().matches("\\d{3}")) {
            throw new IllegalArgumentException("Le code CVV doit contenir 3 chiffres.");
        }
    }

    private void validatePaypal() {
        if (paypalEmailField.getText().isBlank()) {
            throw new IllegalArgumentException("Saisissez votre email PayPal.");
        }
        if (!paypalEmailField.getText().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("L'email PayPal est invalide.");
        }
        openPaypal();
    }

    private TextField createField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("-fx-background-radius: 14; -fx-padding: 12;");
        return field;
    }

    private GridPane createCardPaymentForm() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        Label note = createInfoLabel("Les informations de carte sont verifiees localement avant l'envoi du paiement simule.");
        grid.add(note, 0, 0, 2, 1);

        grid.add(new Label("Nom du titulaire"), 0, 1);
        grid.add(cardHolderField, 0, 2, 2, 1);
        grid.add(new Label("Numero de carte"), 0, 3);
        grid.add(cardNumberField, 0, 4, 2, 1);
        grid.add(new Label("Expiration"), 0, 5);
        grid.add(expiryField, 0, 6);
        grid.add(new Label("CVV"), 1, 5);
        grid.add(cvvField, 1, 6);

        GridPane.setHgrow(cardHolderField, Priority.ALWAYS);
        GridPane.setHgrow(cardNumberField, Priority.ALWAYS);
        GridPane.setHgrow(expiryField, Priority.ALWAYS);
        GridPane.setHgrow(cvvField, Priority.ALWAYS);
        GridPane.setHalignment(note, HPos.LEFT);
        return grid;
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #52606d; "
                + "-fx-background-color: rgba(245,250,248,0.95); -fx-background-radius: 12; -fx-padding: 10;");
        return label;
    }

    private void openPaypal() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI("https://www.paypal.com/"));
            } else {
                UIUtils.showInfo("Ouverture du navigateur non disponible sur cette machine.");
            }
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }
}
