package com.chrionline.client.ui.views;

import com.chrionline.client.model.OrderDTO;
import com.chrionline.client.model.OrderLineDTO;
import com.chrionline.client.model.ProductDTO;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;

public class OrderHistoryView extends VBox {
    private final OrderService orderService = new OrderService();
    private final ProductService productService = new ProductService();

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

            VBox root = new VBox(14);
            root.setPadding(new Insets(18));
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #fffaf5, #ffe7c7);");

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
                    String productName = "Produit #" + line.getProduitId();
                    try {
                        ProductDTO product = productService.getById(line.getProduitId());
                        if (product != null && product.getNom() != null && !product.getNom().isBlank()) {
                            productName = product.getNom();
                        }
                    } catch (Exception ignored) {
                    }

                    Label title = new Label(productName);
                    title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #162521;");

                    Label details = new Label(
                            "Taille #" + line.getTailleId()
                                    + "   |   Quantite : " + line.getQuantite()
                                    + "   |   Prix unitaire : " + PriceUtils.formatMad(line.getPrixUnitaire())
                                    + "   |   Sous-total : " + PriceUtils.formatMad(line.getSousTotal())
                    );
                    details.setWrapText(true);
                    details.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f6368;");

                    VBox lineCard = new VBox(6, title, details);
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

            root.getChildren().addAll(ref, meta, address, linesBox, footer);

            Scene scene = new Scene(root, 640, 420);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }
}
