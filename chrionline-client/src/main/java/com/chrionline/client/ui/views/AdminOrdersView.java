package com.chrionline.client.ui.views;

import com.chrionline.client.model.OrderDTO;
import com.chrionline.client.service.AdminService;
import com.chrionline.client.util.PriceUtils;
import com.chrionline.client.util.UIUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.List;

public class AdminOrdersView extends VBox {
    private final AdminService adminService = new AdminService();
    private final TableView<OrderDTO> ordersTable = new TableView<>();

    public AdminOrdersView() {
        if (!AdminViewSupport.ensureAdmin()) {
            return;
        }

        VBox content = new VBox(18);

        HBox actions = new HBox(10);
        Button refresh = ViewFactory.createSecondaryButton("Actualiser");
        refresh.setOnAction(event -> loadOrders());
        actions.getChildren().add(refresh);

        Label hint = new Label("Le serveur expose actuellement une liste globale des commandes en lecture seule. Le changement de statut n'est pas encore disponible.");
        hint.setWrapText(true);
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        ordersTable.setStyle("-fx-background-radius: 18; -fx-border-radius: 18;");

        TableColumn<OrderDTO, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getReference()));

        TableColumn<OrderDTO, String> userCol = new TableColumn<>("Utilisateur ID");
        userCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getUtilisateurId())));

        TableColumn<OrderDTO, String> dateCol = new TableColumn<>("Date");
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        dateCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getDateCommande() == null ? "-" : format.format(data.getValue().getDateCommande())
        ));

        TableColumn<OrderDTO, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrDash(data.getValue().getStatut())));

        TableColumn<OrderDTO, String> totalCol = new TableColumn<>("Montant");
        totalCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(PriceUtils.formatMad(data.getValue().getMontantTotal())));

        TableColumn<OrderDTO, String> addressCol = new TableColumn<>("Livraison");
        addressCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrDash(data.getValue().getAdresseLivraison())));

        ordersTable.getColumns().setAll(refCol, userCol, dateCol, statusCol, totalCol, addressCol);

        VBox card = new VBox(14, actions, hint, ordersTable);
        card.setPadding(new Insets(18));
        card.setStyle(ViewFactory.cardStyle());
        VBox.setVgrow(ordersTable, Priority.ALWAYS);

        content.getChildren().add(card);
        getChildren().add(AdminViewSupport.createAdminPage(
                "Gestion des commandes",
                "Consultez les commandes du systeme depuis une vue admin dediee.",
                "orders",
                content
        ));

        loadOrders();
    }

    private void loadOrders() {
        try {
            List<OrderDTO> orders = adminService.getOrders();
            ordersTable.setItems(FXCollections.observableArrayList(orders));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
