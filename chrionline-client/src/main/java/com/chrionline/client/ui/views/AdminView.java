package com.chrionline.client.ui.views;

import com.chrionline.client.service.AdminService;
import com.chrionline.client.service.ProductService;
import com.chrionline.client.util.UIUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminView extends VBox {
    private final AdminService adminService = new AdminService();
    private final ProductService productService = new ProductService();

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
        quickActions.getChildren().addAll(productsButton, usersButton, ordersButton);

        VBox capabilitiesCard = new VBox(10);
        capabilitiesCard.setPadding(new Insets(18));
        capabilitiesCard.setStyle(ViewFactory.cardStyle());

        Label capabilitiesTitle = new Label("Fonctionnalites admin actuellement supportees");
        capabilitiesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        capabilitiesCard.getChildren().addAll(
                capabilitiesTitle,
                createBullet("Produits: ajouter, modifier, supprimer, mettre a jour le stock par taille"),
                createBullet("Utilisateurs: afficher, suspendre, reactiver"),
                createBullet("Commandes: afficher la liste globale en lecture seule"),
                createBullet("Paiement detaille et changement de statut commande: non exposes par le serveur actuel")
        );

        welcomeCard.getChildren().addAll(welcomeTitle, welcomeText, quickActions);
        content.getChildren().addAll(stats, welcomeCard, capabilitiesCard);

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
}
