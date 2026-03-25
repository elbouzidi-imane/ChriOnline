package com.chrionline.client.ui.views;

import com.chrionline.client.model.OrderDTO;
import com.chrionline.client.model.OrderLineDTO;
import com.chrionline.client.model.PaymentDTO;
import com.chrionline.client.service.AdminService;
import com.chrionline.client.util.PriceUtils;
import com.chrionline.client.util.UIUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
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
        Button status = ViewFactory.createSecondaryButton("Changer statut");
        status.setOnAction(event -> updateStatus());
        Button payment = ViewFactory.createSecondaryButton("Paiement");
        payment.setOnAction(event -> showPayment());
        Button refund = ViewFactory.createSecondaryButton("Rembourser");
        refund.setOnAction(event -> refundPayment());
        actions.getChildren().addAll(refresh, status, payment, refund);

        Label hint = new Label("Le serveur permet maintenant le detail commande, la mise a jour du statut et le remboursement.");
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

        TableColumn<OrderDTO, OrderDTO> actionCol = new TableColumn<>("Detail");
        actionCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        actionCol.setCellFactory(ignored -> new TableCell<>() {
            private final Button detailButton = ViewFactory.createPrimaryButton("Voir");

            {
                detailButton.setOnAction(event -> showOrderDetail(getItem()));
            }

            @Override
            protected void updateItem(OrderDTO item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : detailButton);
            }
        });

        ordersTable.getColumns().setAll(refCol, userCol, dateCol, statusCol, totalCol, addressCol, actionCol);

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

    private void showOrderDetail(OrderDTO order) {
        if (order == null) {
            return;
        }
        try {
            OrderDTO detail = adminService.getOrderDetail(order.getId());
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Detail commande");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setStyle("-fx-background-color: #fffaf5;");

            VBox linesBox = new VBox(10);
            for (OrderLineDTO line : detail.getLignes()) {
                Label row = new Label("Produit #" + line.getProduitId()
                        + " | Taille #" + line.getTailleId()
                        + " | Qté " + line.getQuantite()
                        + " | PU " + PriceUtils.formatMad(line.getPrixUnitaire())
                        + " | ST " + PriceUtils.formatMad(line.getSousTotal()));
                row.setWrapText(true);
                row.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
                linesBox.getChildren().add(row);
            }
            VBox content = new VBox(12,
                    new Label("Reference: " + detail.getReference()),
                    new Label("Statut: " + detail.getStatut()),
                    new Label("Montant: " + PriceUtils.formatMad(detail.getMontantTotal())),
                    new Label("Adresse: " + valueOrDash(detail.getAdresseLivraison())),
                    linesBox);
            dialog.getDialogPane().setContent(content);
            dialog.showAndWait();
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void updateStatus() {
        OrderDTO selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showError("Selectionnez une commande.");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Changer statut");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #fffaf5;");
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList(
                "EN_ATTENTE", "VALIDEE", "EXPEDIEE", "LIVREE", "ANNULEE"));
        statusBox.getSelectionModel().select(selected.getStatut());
        dialog.getDialogPane().setContent(new VBox(12, new Label("Nouveau statut"), statusBox));

        if (dialog.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            try {
                adminService.updateOrderStatus(selected.getId(), statusBox.getValue());
                UIUtils.showSuccess("Statut mis a jour.");
                loadOrders();
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        }
    }

    private void showPayment() {
        OrderDTO selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showError("Selectionnez une commande.");
            return;
        }
        try {
            PaymentDTO payment = adminService.getPayment(selected.getId());
            UIUtils.showInfo("Paiement " + payment.getReference()
                    + "\nMode: " + payment.getModePaiement()
                    + "\nStatut: " + payment.getStatut()
                    + "\nMontant: " + PriceUtils.formatMad(payment.getMontant()));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void refundPayment() {
        OrderDTO selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showError("Selectionnez une commande.");
            return;
        }
        try {
            adminService.remboursement(selected.getId());
            UIUtils.showSuccess("Paiement rembourse.");
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
