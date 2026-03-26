package com.chrionline.client.ui.views;

import com.chrionline.client.model.NotificationDTO;
import com.chrionline.client.service.AdminService;
import com.chrionline.client.service.NotificationService;
import com.chrionline.client.service.ProductService;
import com.chrionline.client.util.UIUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.List;

public class AdminView extends VBox {
    private final AdminService adminService = new AdminService();
    private final ProductService productService = new ProductService();
    private final NotificationService notificationService = new NotificationService();

    private final Label productsCount = new Label("0");
    private final Label usersCount = new Label("0");
    private final Label ordersCount = new Label("0");

    public AdminView() {
        if (!AdminViewSupport.ensureAdmin()) {
            return;
        }

        VBox content = new VBox(18);

        HBox stats = new HBox(14,
                AdminViewSupport.createStatCard("Produits actifs", productsCount, "#eb8b1b", "Catalogue visible cote client"),
                AdminViewSupport.createStatCard("Utilisateurs", usersCount, "#1f6f5f", "Comptes recuperes depuis le serveur"),
                AdminViewSupport.createStatCard("Commandes", ordersCount, "#0f766e", "Vue globale des commandes"));

        VBox welcomeCard = new VBox(12);
        welcomeCard.setPadding(new Insets(18));
        welcomeCard.setStyle(ViewFactory.cardStyle());

        Label welcomeTitle = new Label("Centre de pilotage");
        welcomeTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Label welcomeText = new Label(
                "Utilisez les pages separees pour gerer les produits, les utilisateurs et les commandes sans surcharger un seul ecran."
        );
        welcomeText.setWrapText(true);
        welcomeText.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

        HBox quickActions = new HBox(12);
        Button productsButton = ViewFactory.createPrimaryButton("Gerer les produits");
        productsButton.setOnAction(event -> com.chrionline.client.ui.NavigationManager.navigateTo(new AdminProductsView()));
        Button usersButton = ViewFactory.createSecondaryButton("Voir les utilisateurs");
        usersButton.setOnAction(event -> com.chrionline.client.ui.NavigationManager.navigateTo(new AdminUsersView()));
        Button ordersButton = ViewFactory.createSecondaryButton("Suivre les commandes");
        ordersButton.setOnAction(event -> com.chrionline.client.ui.NavigationManager.navigateTo(new AdminOrdersView()));
        Button promosButton = ViewFactory.createSecondaryButton("Codes promo");
        promosButton.setOnAction(event -> com.chrionline.client.ui.NavigationManager.navigateTo(new AdminPromosView()));
        quickActions.getChildren().addAll(productsButton, usersButton, ordersButton, promosButton);

        VBox capabilitiesCard = new VBox(10);
        capabilitiesCard.setPadding(new Insets(18));
        capabilitiesCard.setStyle(ViewFactory.cardStyle());

        Label capabilitiesTitle = new Label("Fonctionnalites admin actuellement supportees");
        capabilitiesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        capabilitiesCard.getChildren().addAll(
                capabilitiesTitle,
                createBullet("Produits: ajouter, modifier, supprimer, mettre a jour le stock par taille"),
                createBullet("Utilisateurs: afficher, suspendre, reactiver"),
                createBullet("Commandes: afficher la liste globale, gerer annulations et remboursements"),
                createBullet("Codes promo: creer, activer, desactiver et consulter les statistiques d'usage")
        );

        welcomeCard.getChildren().addAll(welcomeTitle, welcomeText, quickActions);
        VBox notificationsCard = createNotificationsCard();

        content.getChildren().addAll(stats, welcomeCard, capabilitiesCard, notificationsCard);

        getChildren().add(AdminViewSupport.createAdminPage(
                "Dashboard administrateur",
                "Un espace plus moderne avec navigation separee vers chaque domaine de gestion.",
                "dashboard",
                content
        ));

        loadCounts();
    }

    private Label createBullet(String text) {
        Label label = new Label("• " + text);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        return label;
    }

    private void loadCounts() {
        try {
            productsCount.setText(String.valueOf(productService.getAll().size()));
            usersCount.setText(String.valueOf(adminService.getUsers().size()));
            ordersCount.setText(String.valueOf(adminService.getOrders().size()));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private VBox createNotificationsCard() {
        Label title = new Label("Notifications admin recentes");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        VBox items = new VBox(10);
        try {
            List<NotificationDTO> notifications = notificationService.getMyNotifications();
            if (notifications.isEmpty()) {
                items.getChildren().add(createNotificationItem("Aucune notification admin disponible.", null));
            } else {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                for (NotificationDTO notification : notifications) {
                    items.getChildren().add(createNotificationItem(
                            notification.getMessage(),
                            notification.getCreatedAt() == null ? null : format.format(notification.getCreatedAt())
                    ));
                    if (!notification.isLue()) {
                        notificationService.markAsRead(notification.getId());
                    }
                }
            }
        } catch (Exception e) {
            items.getChildren().add(createNotificationItem("Impossible de charger les notifications admin.", null));
        }

        ScrollPane scrollPane = new ScrollPane(items);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(240);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox card = new VBox(12, title, scrollPane);
        card.setPadding(new Insets(18));
        card.setStyle(ViewFactory.cardStyle());
        return card;
    }

    private VBox createNotificationItem(String message, String dateText) {
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #1f2937;");

        VBox box = new VBox(6, messageLabel);
        if (dateText != null) {
            Label dateLabel = new Label(dateText);
            dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8f98;");
            box.getChildren().add(dateLabel);
        }
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.82); -fx-background-radius: 16;");
        return box;
    }
}
