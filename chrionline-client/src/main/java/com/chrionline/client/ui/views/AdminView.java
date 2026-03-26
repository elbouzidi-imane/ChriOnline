package com.chrionline.client.ui.views;

import com.chrionline.client.model.NotificationDTO;
import com.chrionline.client.model.OrderDTO;
import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.model.UserDTO;
import com.chrionline.client.service.AdminService;
import com.chrionline.client.service.NotificationService;
import com.chrionline.client.service.ProductService;
import com.chrionline.client.util.UIUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminView extends VBox {
    private final AdminService adminService = new AdminService();
    private final ProductService productService = new ProductService();
    private final NotificationService notificationService = new NotificationService();

    private final Label productsCount = new Label("0");
    private final Label usersCount = new Label("0");
    private final Label ordersCount = new Label("0");
    private final VBox chartsSection = new VBox(16);

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
        chartsSection.getChildren().add(createPlaceholderCard("Chargement des graphiques..."));

        content.getChildren().addAll(stats, welcomeCard, chartsSection, capabilitiesCard, notificationsCard);

        getChildren().add(AdminViewSupport.createAdminPage(
                "Dashboard administrateur",
                "Un espace plus moderne avec navigation separee vers chaque domaine de gestion.",
                "dashboard",
                content
        ));

        loadCounts();
        loadCharts();
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

    private void loadCharts() {
        try {
            List<ProductDTO> products = productService.getAll();
            List<UserDTO> users = adminService.getUsers();
            List<OrderDTO> orders = adminService.getOrders();

            VBox titleBlock = new VBox(6);
            Label title = new Label("Visualisations");
            title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #12372e;");
            Label subtitle = new Label("Un apercu rapide des commandes, du catalogue et de l'activite recente.");
            subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");
            titleBlock.getChildren().addAll(title, subtitle);

            FlowPane topCharts = new FlowPane();
            topCharts.setHgap(16);
            topCharts.setVgap(16);
            topCharts.getChildren().addAll(
                    createOrdersStatusChart(orders),
                    createStockChart(products),
                    createUsersChart(users)
            );

            VBox bottomChart = createOrdersTimelineChart(orders);
            chartsSection.getChildren().setAll(titleBlock, topCharts, bottomChart);
        } catch (Exception e) {
            chartsSection.getChildren().setAll(createPlaceholderCard("Impossible de charger les graphiques."));
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

    private VBox createOrdersStatusChart(List<OrderDTO> orders) {
        Map<String, Long> counts = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getStatut() == null || order.getStatut().isBlank() ? "Inconnu" : order.getStatut(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        List<PieChart.Data> data = new ArrayList<>();
        counts.forEach((status, count) -> data.add(new PieChart.Data(status + " (" + count + ")", count)));

        PieChart chart = new PieChart(FXCollections.observableArrayList(data));
        chart.setLegendVisible(false);
        chart.setLabelsVisible(true);
        chart.setPrefSize(320, 280);

        return wrapChart("Commandes par statut", "Vue immediate des statuts en cours.", chart, 340);
    }

    private VBox createStockChart(List<ProductDTO> products) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Produits");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCategoryGap(18);
        chart.setBarGap(10);
        chart.setPrefSize(360, 280);

        long rupture = products.stream().filter(product -> product.getStock() <= 0).count();
        long stockFaible = products.stream().filter(product -> product.getStock() > 0 && product.getStock() <= 5).count();
        long disponibles = products.stream().filter(product -> product.getStock() > 5).count();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Rupture", rupture));
        series.getData().add(new XYChart.Data<>("Stock faible", stockFaible));
        series.getData().add(new XYChart.Data<>("Disponibles", disponibles));
        chart.getData().add(series);

        return wrapChart("Etat du stock", "Repartition rapide du catalogue par disponibilite.", chart, 380);
    }

    private VBox createUsersChart(List<UserDTO> users) {
        long admins = users.stream().filter(UserDTO::isAdmin).count();
        long clients = users.size() - admins;
        long suspendus = users.stream().filter(user -> "SUSPENDU".equalsIgnoreCase(user.getStatut())).count();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefSize(320, 280);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Clients", clients));
        series.getData().add(new XYChart.Data<>("Admins", admins));
        series.getData().add(new XYChart.Data<>("Suspendus", suspendus));
        chart.getData().add(series);

        return wrapChart("Utilisateurs", "Roles et comptes suspendus en un coup d'oeil.", chart, 340);
    }

    private VBox createOrdersTimelineChart(List<OrderDTO> orders) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Commandes");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setPrefSize(1080, 320);

        Map<String, Long> byDay = orders.stream()
                .filter(order -> order.getDateCommande() != null)
                .collect(Collectors.groupingBy(
                        order -> new SimpleDateFormat("dd/MM").format(order.getDateCommande()),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        List<Map.Entry<String, Long>> entries = byDay.entrySet().stream()
                .sorted(Comparator.comparing(entry -> parseDay(entry.getKey())))
                .toList();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Long> entry : entries) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        chart.getData().add(series);

        return wrapChart(
                "Evolution recente des commandes",
                "Courbe simple des commandes par jour selon les donnees disponibles.",
                chart,
                1120
        );
    }

    private VBox wrapChart(String title, String subtitle, javafx.scene.Node chart, double width) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #12372e;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5b5f63;");

        VBox box = new VBox(10, titleLabel, subtitleLabel, chart);
        box.setPadding(new Insets(18));
        box.setPrefWidth(width);
        box.setStyle(ViewFactory.cardStyle());
        return box;
    }

    private VBox createPlaceholderCard(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #5b5f63;");
        VBox box = new VBox(label);
        box.setPadding(new Insets(18));
        box.setStyle(ViewFactory.cardStyle());
        return box;
    }

    private Date parseDay(String day) {
        try {
            return new SimpleDateFormat("dd/MM").parse(day);
        } catch (Exception e) {
            return new Date(0);
        }
    }
}
