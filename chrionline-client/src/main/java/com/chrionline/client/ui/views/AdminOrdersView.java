package com.chrionline.client.ui.views;

import com.chrionline.client.model.CancellationConfigDTO;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminOrdersView extends VBox {
    private final AdminService adminService = new AdminService();
    private final TableView<OrderDTO> ordersTable = new TableView<>();
    private final Label cancellationRulesLabel = new Label();
    private final Map<Integer, PaymentDTO> paymentCache = new HashMap<>();

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
        Button cancellationRules = ViewFactory.createSecondaryButton("Regles annulation");
        cancellationRules.setOnAction(event -> editCancellationRules());
        actions.getChildren().addAll(refresh, status, payment, refund, cancellationRules);

        Label hint = new Label("Le panneau admin gere le detail commande, les statuts, le remboursement et les regles d'annulation client.");
        hint.setWrapText(true);
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");
        cancellationRulesLabel.setWrapText(true);
        cancellationRulesLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #256c54; -fx-font-weight: bold;");

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

        TableColumn<OrderDTO, String> paymentModeCol = new TableColumn<>("Mode paiement");
        paymentModeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(resolvePaymentMode(data.getValue().getId())));

        TableColumn<OrderDTO, String> paymentStatusCol = new TableColumn<>("Etat remboursement");
        paymentStatusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(resolvePaymentStatus(data.getValue().getId())));

        TableColumn<OrderDTO, String> reasonCol = new TableColumn<>("Motif client");
        reasonCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrDash(data.getValue().getMotifAnnulation())));

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

        ordersTable.getColumns().setAll(
                refCol,
                userCol,
                dateCol,
                statusCol,
                totalCol,
                paymentModeCol,
                paymentStatusCol,
                reasonCol,
                addressCol,
                actionCol
        );
        refCol.setMinWidth(160);
        userCol.setMinWidth(120);
        dateCol.setMinWidth(150);
        statusCol.setMinWidth(120);
        totalCol.setMinWidth(120);
        paymentModeCol.setMinWidth(140);
        paymentStatusCol.setMinWidth(150);
        reasonCol.setMinWidth(220);
        addressCol.setMinWidth(240);
        actionCol.setMinWidth(120);
        ordersTable.setMinWidth(1540);

        ScrollPane tableScroll = new ScrollPane(ordersTable);
        tableScroll.setFitToHeight(true);
        tableScroll.setFitToWidth(false);
        tableScroll.setPannable(true);
        tableScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox card = new VBox(14, actions, hint, cancellationRulesLabel, tableScroll);
        card.setPadding(new Insets(18));
        card.setStyle(ViewFactory.cardStyle());
        VBox.setVgrow(tableScroll, Priority.ALWAYS);

        content.getChildren().add(card);
        getChildren().add(AdminViewSupport.createAdminPage(
                "Gestion des commandes",
                "Consultez les commandes du systeme depuis une vue admin dediee.",
                "orders",
                content
        ));

        loadOrders();
        loadCancellationRules();
    }

    private void loadOrders() {
        try {
            List<OrderDTO> orders = adminService.getOrders();
            paymentCache.clear();
            for (OrderDTO order : orders) {
                try {
                    PaymentDTO payment = adminService.getPayment(order.getId());
                    if (payment != null) {
                        paymentCache.put(order.getId(), payment);
                    }
                } catch (Exception ignored) {
                }
            }
            ordersTable.setItems(FXCollections.observableArrayList(orders));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void loadCancellationRules() {
        try {
            CancellationConfigDTO config = adminService.getCancellationConfig();
            cancellationRulesLabel.setText(
                    "Annulation client autorisee si statut = " + config.getCancellableStatus()
                            + " | Motif " + (config.isReasonRequired() ? "obligatoire" : "optionnel")
                            + " | Remboursement " + (config.isAutomaticRefund() ? "automatique" : "manuel")
                            + " | Delai estime : " + config.getEstimatedRefundDelay()
            );
        } catch (Exception e) {
            cancellationRulesLabel.setText("Regles d'annulation indisponibles.");
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
                    new Label("Paiement: " + resolvePaymentMode(detail.getId()) + " | " + resolvePaymentStatus(detail.getId())),
                    new Label("Motif annulation: " + valueOrDash(detail.getMotifAnnulation())),
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
                if (statusBox.getValue() == null || statusBox.getValue().isBlank()) {
                    UIUtils.showError("Selectionnez un statut.");
                    return;
                }
                if (statusBox.getValue().equals(selected.getStatut())) {
                    UIUtils.showInfo("Le statut choisi est identique au statut actuel.");
                    return;
                }
                adminService.updateOrderStatus(selected.getId(), statusBox.getValue());
                UIUtils.showSuccess("Statut mis a jour. Un email sera envoye si le client a active les notifications.");
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
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Traitement remboursement");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #fffaf5;");

        ComboBox<String> decisionBox = new ComboBox<>(FXCollections.observableArrayList(
                "Accepter le remboursement",
                "Refuser le remboursement"
        ));
        decisionBox.getSelectionModel().selectFirst();

        TextField messageField = new TextField();
        messageField.setPromptText("Message visible dans l'email client");

        TextField delayField = new TextField();
        delayField.setPromptText("Ex: 3 a 5 jours ouvrables");

        decisionBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            boolean approved = "Accepter le remboursement".equals(newValue);
            delayField.setDisable(!approved);
            delayField.setOpacity(approved ? 1 : 0.65);
            messageField.setPromptText(approved
                    ? "Message optionnel pour confirmer le remboursement"
                    : "Motif du refus obligatoire");
        });

        VBox content = new VBox(
                12,
                new Label("Decision admin"),
                decisionBox,
                new Label("Message / motif"),
                messageField,
                new Label("Delai estime"),
                delayField
        );
        dialog.getDialogPane().setContent(content);

        if (dialog.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            boolean approved = "Accepter le remboursement".equals(decisionBox.getValue());
            String detail = messageField.getText().trim();
            String delay = delayField.getText().trim();

            if (!approved && detail.isBlank()) {
                UIUtils.showError("Le motif du refus est obligatoire.");
                return;
            }
            if (approved && delay.isBlank()) {
                UIUtils.showError("Le delai estime est obligatoire.");
                return;
            }

            try {
                adminService.processRefundDecision(selected.getId(), approved, detail, delay);
                UIUtils.showSuccess(approved
                        ? "Remboursement confirme et email envoye au client."
                        : "Refus de remboursement envoye au client.");
                loadOrders();
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        }
    }

    private void editCancellationRules() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Regles d'annulation");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #fffaf5;");

        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList(
                "EN_ATTENTE", "VALIDEE", "EXPEDIEE", "LIVREE", "ANNULEE"
        ));
        CheckBox reasonRequired = new CheckBox("Motif d'annulation obligatoire");
        CheckBox automaticRefund = new CheckBox("Remboursement automatique");
        TextField estimatedDelay = new TextField();
        estimatedDelay.setPromptText("Ex: 3 a 5 jours ouvrables");

        try {
            CancellationConfigDTO config = adminService.getCancellationConfig();
            statusBox.getSelectionModel().select(config.getCancellableStatus());
            reasonRequired.setSelected(config.isReasonRequired());
            automaticRefund.setSelected(config.isAutomaticRefund());
            estimatedDelay.setText(config.getEstimatedRefundDelay());
        } catch (Exception ignored) {
            statusBox.getSelectionModel().select("VALIDEE");
            reasonRequired.setSelected(true);
            automaticRefund.setSelected(true);
            estimatedDelay.setText("3 a 5 jours ouvrables");
        }

        VBox content = new VBox(
                12,
                new Label("Statut annulable"),
                statusBox,
                reasonRequired,
                automaticRefund,
                new Label("Delai estime de remboursement"),
                estimatedDelay
        );
        dialog.getDialogPane().setContent(content);

        if (dialog.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            if (statusBox.getValue() == null || statusBox.getValue().isBlank()) {
                UIUtils.showError("Selectionnez un statut annulable.");
                return;
            }
            if (estimatedDelay.getText().isBlank()) {
                UIUtils.showError("Le delai estime est obligatoire.");
                return;
            }
            try {
                adminService.updateCancellationConfig(
                        statusBox.getValue(),
                        reasonRequired.isSelected(),
                        automaticRefund.isSelected(),
                        estimatedDelay.getText().trim()
                );
                UIUtils.showSuccess("Regles d'annulation mises a jour.");
                loadCancellationRules();
            } catch (Exception e) {
                UIUtils.showError(e.getMessage());
            }
        }
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String resolvePaymentMode(int orderId) {
        PaymentDTO payment = paymentCache.get(orderId);
        return payment == null ? "-" : valueOrDash(payment.getModePaiement());
    }

    private String resolvePaymentStatus(int orderId) {
        PaymentDTO payment = paymentCache.get(orderId);
        return payment == null ? "-" : valueOrDash(payment.getStatut());
    }
}
