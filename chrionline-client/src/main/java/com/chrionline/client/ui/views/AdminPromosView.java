package com.chrionline.client.ui.views;

import com.chrionline.client.model.PromoCodeDTO;
import com.chrionline.client.model.PromoUsageStatDTO;
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
import javafx.scene.control.DatePicker;
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
import java.time.LocalDate;
import java.util.List;

public class AdminPromosView extends VBox {
    private final AdminService adminService = new AdminService();
    private final TableView<PromoCodeDTO> table = new TableView<>();

    public AdminPromosView() {
        if (!AdminViewSupport.ensureAdmin()) {
            return;
        }

        Button refresh = ViewFactory.createSecondaryButton("Actualiser");
        refresh.setOnAction(event -> loadPromos());
        Button addPromo = ViewFactory.createPrimaryButton("Ajouter code promo");
        addPromo.setOnAction(event -> showCreateDialog());

        HBox actions = new HBox(10, refresh, addPromo);

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<PromoCodeDTO, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getCode()));

        TableColumn<PromoCodeDTO, String> typeCol = new TableColumn<>("Reduction");
        typeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                "PERCENTAGE".equalsIgnoreCase(data.getValue().getReductionType())
                        ? data.getValue().getReductionValue() + " %"
                        : PriceUtils.formatMad(data.getValue().getReductionValue())
        ));

        TableColumn<PromoCodeDTO, String> minCol = new TableColumn<>("Minimum");
        minCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(PriceUtils.formatMad(data.getValue().getMinimumOrderAmount())));

        TableColumn<PromoCodeDTO, String> periodCol = new TableColumn<>("Periode");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        periodCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                formatDate(data.getValue().getStartDate(), dateFormat) + " -> " + formatDate(data.getValue().getEndDate(), dateFormat)
        ));

        TableColumn<PromoCodeDTO, String> usesCol = new TableColumn<>("Usages");
        usesCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getMaxUsagePerUser() + " / client | " + data.getValue().getMaxUses() + " max"
        ));

        TableColumn<PromoCodeDTO, String> activeCol = new TableColumn<>("Actif");
        activeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().isActive() ? "OUI" : "NON"));

        TableColumn<PromoCodeDTO, PromoCodeDTO> statsCol = new TableColumn<>("Actions");
        statsCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        statsCol.setCellFactory(ignored -> new TableCell<>() {
            private final Button statsButton = ViewFactory.createSecondaryButton("Stats");
            private final Button toggleButton = ViewFactory.createPrimaryButton("Activer / Desactiver");
            private final HBox box = new HBox(8, statsButton, toggleButton);

            {
                statsButton.setMinWidth(90);
                toggleButton.setMinWidth(190);
                statsButton.setOnAction(event -> showStats(getItem()));
                toggleButton.setOnAction(event -> togglePromo(getItem()));
            }

            @Override
            protected void updateItem(PromoCodeDTO item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : box);
            }
        });

        table.getColumns().setAll(codeCol, typeCol, minCol, periodCol, usesCol, activeCol, statsCol);
        codeCol.setMinWidth(120);
        typeCol.setMinWidth(130);
        minCol.setMinWidth(120);
        periodCol.setMinWidth(220);
        usesCol.setMinWidth(170);
        activeCol.setMinWidth(90);
        statsCol.setMinWidth(320);
        table.setMinWidth(1190);

        ScrollPane tableScroll = new ScrollPane(table);
        tableScroll.setFitToHeight(true);
        tableScroll.setFitToWidth(false);
        tableScroll.setPannable(true);
        tableScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox card = new VBox(14, actions, tableScroll);
        card.setPadding(new Insets(18));
        card.setStyle(ViewFactory.cardStyle());
        VBox.setVgrow(tableScroll, Priority.ALWAYS);

        getChildren().add(AdminViewSupport.createAdminPage(
                "Codes promo",
                "Creez des reductions, suivez les usages et activez ou desactivez vos campagnes.",
                "promos",
                new VBox(card)
        ));

        loadPromos();
    }

    private void loadPromos() {
        try {
            List<PromoCodeDTO> promos = adminService.getPromos();
            table.setItems(FXCollections.observableArrayList(promos));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void showCreateDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter code promo");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #fffaf5;");

        TextField codeField = new TextField();
        codeField.setPromptText("PROMO20");
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("PERCENTAGE", "FIXED"));
        typeBox.getSelectionModel().selectFirst();
        TextField valueField = new TextField();
        valueField.setPromptText("20 ou 50");
        TextField minimumField = new TextField();
        minimumField.setPromptText("200");
        DatePicker startPicker = new DatePicker(LocalDate.now());
        DatePicker endPicker = new DatePicker(LocalDate.now().plusMonths(1));
        TextField maxUsesField = new TextField("100");
        TextField maxUsagePerUserField = new TextField("1");

        VBox content = new VBox(
                12,
                new Label("Code"), codeField,
                new Label("Type reduction"), typeBox,
                new Label("Valeur reduction"), valueField,
                new Label("Montant minimum"), minimumField,
                new Label("Date debut"), startPicker,
                new Label("Date expiration"), endPicker,
                new Label("Nombre max d'utilisations"), maxUsesField,
                new Label("Usage par personne"), maxUsagePerUserField
        );
        dialog.getDialogPane().setContent(content);

        if (dialog.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            try {
                String code = codeField.getText().trim().toUpperCase();
                if (codeField.getText().isBlank()) {
                    UIUtils.showError("Le code promo est obligatoire.");
                    return;
                }
                if (valueField.getText().isBlank() || minimumField.getText().isBlank()
                        || maxUsesField.getText().isBlank() || maxUsagePerUserField.getText().isBlank()) {
                    UIUtils.showError("Tous les champs numeriques sont obligatoires.");
                    return;
                }
                if (startPicker.getValue() == null || endPicker.getValue() == null) {
                    UIUtils.showError("Les dates de debut et d'expiration sont obligatoires.");
                    return;
                }
                if (endPicker.getValue().isBefore(startPicker.getValue())) {
                    UIUtils.showError("La date d'expiration doit etre apres la date de debut.");
                    return;
                }

                adminService.addPromo(
                        code,
                        typeBox.getValue(),
                        Double.parseDouble(valueField.getText().trim().replace(',', '.')),
                        Double.parseDouble(minimumField.getText().trim().replace(',', '.')),
                        startPicker.getValue().toString(),
                        endPicker.getValue().toString(),
                        Integer.parseInt(maxUsesField.getText().trim()),
                        Integer.parseInt(maxUsagePerUserField.getText().trim())
                );
                UIUtils.showSuccess("Code promo ajoute.");
                loadPromos();
            } catch (Exception e) {
                if (promoExists(codeField.getText().trim().toUpperCase())) {
                    loadPromos();
                    UIUtils.showSuccess("Code promo ajoute.");
                } else {
                    UIUtils.showError(e.getMessage());
                }
            }
        }
    }

    private void showStats(PromoCodeDTO promo) {
        if (promo == null) {
            return;
        }
        try {
            PromoUsageStatDTO stats = adminService.getPromoStats(promo.getCode());
            UIUtils.showInfo(
                    "Code : " + stats.getPromo().getCode()
                            + "\nStatut : " + stats.getStatus()
                            + "\nUtilisations : " + stats.getUsageCount()
                            + "\nTotal reductions : " + PriceUtils.formatMad(stats.getTotalDiscountAmount())
                            + "\nClients : " + String.join(", ", stats.getClients())
            );
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void togglePromo(PromoCodeDTO promo) {
        if (promo == null) {
            return;
        }
        try {
            adminService.togglePromo(promo.getId(), !promo.isActive());
            UIUtils.showSuccess("Statut du code promo mis a jour.");
            loadPromos();
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private String formatDate(java.util.Date date, SimpleDateFormat format) {
        return date == null ? "-" : format.format(date);
    }

    private boolean promoExists(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        try {
            return adminService.getPromos().stream()
                    .anyMatch(promo -> code.equalsIgnoreCase(promo.getCode()));
        } catch (Exception ignored) {
            return false;
        }
    }
}
