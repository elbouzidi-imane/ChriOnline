package com.chrionline.client.ui.views;

import com.chrionline.client.model.OrderDTO;
import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.model.UserDTO;
import com.chrionline.client.service.AdminService;
import com.chrionline.client.service.ProductService;
import com.chrionline.client.session.AppSession;
import com.chrionline.client.ui.NavigationManager;
import com.chrionline.client.util.UIUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class AdminView extends VBox {
    private final AdminService adminService = new AdminService();
    private final ProductService productService = new ProductService();

    private final TableView<ProductDTO> productsTable = new TableView<>();
    private final TableView<UserDTO> usersTable = new TableView<>();
    private final TableView<OrderDTO> ordersTable = new TableView<>();

    private final Label productsCount = new Label("0");
    private final Label usersCount = new Label("0");
    private final Label ordersCount = new Label("0");

    public AdminView() {
        if (!AppSession.isLoggedIn() || !AppSession.getCurrentUser().isAdmin()) {
            NavigationManager.navigateTo(new LoginView());
            return;
        }

        VBox shell = ViewFactory.createPageShell(
                "Espace administrateur",
                "Pilotez le catalogue, les comptes clients et les commandes depuis un tableau de bord clair."
        );
        getChildren().add(shell);

        Label badge = new Label("ADMIN DASHBOARD");
        badge.setStyle("-fx-background-color: #1f6f5f; -fx-text-fill: white; -fx-font-size: 11px; "
                + "-fx-font-weight: bold; -fx-padding: 6 10 6 10; -fx-background-radius: 999;");

        Label hello = new Label("Connecte en tant que : " + AppSession.getCurrentUser().getEmail());
        hello.setStyle("-fx-font-size: 14px; -fx-text-fill: #5f6368;");

        Button homeButton = ViewFactory.createSecondaryButton("Accueil");
        homeButton.setOnAction(event -> NavigationManager.navigateTo(new HomeView()));
        Button logoutButton = ViewFactory.createPrimaryButton("Deconnexion");
        logoutButton.setOnAction(event -> {
            AppSession.clear();
            NavigationManager.navigateTo(new LoginView());
        });

        HBox topBar = ViewFactory.createTopBar(new VBox(8, badge, hello), homeButton, logoutButton);

        HBox stats = new HBox(14,
                createStatCard("Produits actifs", productsCount, "#d97706"),
                createStatCard("Utilisateurs", usersCount, "#1f6f5f"),
                createStatCard("Commandes", ordersCount, "#7c3aed"));

        TabPane tabs = new TabPane(buildProductsTab(), buildUsersTab(), buildOrdersTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: transparent;");

        VBox card = new VBox(18, topBar, stats, tabs);
        card.setPadding(new Insets(20));
        card.setStyle(ViewFactory.cardStyle());
        shell.getChildren().add(card);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        refreshAll();
    }

    private Tab buildProductsTab() {
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        productsTable.setStyle("-fx-background-radius: 18; -fx-border-radius: 18;");

        TableColumn<ProductDTO, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNom()));

        TableColumn<ProductDTO, String> priceCol = new TableColumn<>("Prix");
        priceCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f EUR", data.getValue().getPrixAffiche())));

        TableColumn<ProductDTO, String> colorCol = new TableColumn<>("Couleur");
        colorCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrDash(data.getValue().getCouleur())));

        TableColumn<ProductDTO, String> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getStock())));

        productsTable.getColumns().setAll(nameCol, priceCol, colorCol, stockCol);

        Button refresh = ViewFactory.createSecondaryButton("Actualiser");
        refresh.setOnAction(event -> loadProducts());
        Button add = ViewFactory.createPrimaryButton("Ajouter");
        add.setOnAction(event -> addProduct());
        Button edit = ViewFactory.createSecondaryButton("Modifier");
        edit.setOnAction(event -> editProduct());
        Button stock = ViewFactory.createSecondaryButton("Stock");
        stock.setOnAction(event -> updateStock());
        Button delete = ViewFactory.createSecondaryButton("Supprimer");
        delete.setOnAction(event -> deleteProduct());

        Label hint = new Label("Ajoutez, modifiez ou retirez les fiches produit. Le bouton Stock agit sur les tailles.");
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f6368;");

        VBox content = new VBox(14, ViewFactory.createTopBar(hint, refresh, add, edit, stock, delete), productsTable);
        content.setPadding(new Insets(10));
        VBox.setVgrow(productsTable, Priority.ALWAYS);
        return new Tab("Produits", content);
    }

    private Tab buildUsersTab() {
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        usersTable.setStyle("-fx-background-radius: 18; -fx-border-radius: 18;");

        TableColumn<UserDTO, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPrenom() + " " + data.getValue().getNom()));

        TableColumn<UserDTO, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEmail()));

        TableColumn<UserDTO, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrDash(data.getValue().getRole())));

        TableColumn<UserDTO, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrDash(data.getValue().getStatut())));

        usersTable.getColumns().setAll(nameCol, emailCol, roleCol, statusCol);

        Button refresh = ViewFactory.createSecondaryButton("Actualiser");
        refresh.setOnAction(event -> loadUsers());
        Button suspend = ViewFactory.createPrimaryButton("Suspendre");
        suspend.setOnAction(event -> changeUserStatus(true));
        Button activate = ViewFactory.createSecondaryButton("Activer");
        activate.setOnAction(event -> changeUserStatus(false));

        Label hint = new Label("Selectionnez un utilisateur pour suspendre ou reactiver son acces.");
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f6368;");

        VBox content = new VBox(14, ViewFactory.createTopBar(hint, refresh, suspend, activate), usersTable);
        content.setPadding(new Insets(10));
        VBox.setVgrow(usersTable, Priority.ALWAYS);
        return new Tab("Utilisateurs", content);
    }

    private Tab buildOrdersTab() {
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        ordersTable.setStyle("-fx-background-radius: 18; -fx-border-radius: 18;");

        TableColumn<OrderDTO, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getReference()));

        TableColumn<OrderDTO, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrDash(data.getValue().getStatut())));

        TableColumn<OrderDTO, String> totalCol = new TableColumn<>("Montant");
        totalCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f EUR", data.getValue().getMontantTotal())));

        TableColumn<OrderDTO, String> addressCol = new TableColumn<>("Livraison");
        addressCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrDash(data.getValue().getAdresseLivraison())));

        ordersTable.getColumns().setAll(refCol, statusCol, totalCol, addressCol);

        Button refresh = ViewFactory.createSecondaryButton("Actualiser");
        refresh.setOnAction(event -> loadOrders());

        Label hint = new Label("Vue lecture seule des commandes actuelles du systeme.");
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f6368;");

        VBox content = new VBox(14, ViewFactory.createTopBar(hint, refresh), ordersTable);
        content.setPadding(new Insets(10));
        VBox.setVgrow(ordersTable, Priority.ALWAYS);
        return new Tab("Commandes", content);
    }

    private VBox createStatCard(String title, Label valueLabel, String accentColor) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5f6368;");

        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");

        Label subtitle = new Label("Vue mise a jour depuis le serveur");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b9096;");

        VBox card = new VBox(6, titleLabel, valueLabel, subtitle);
        card.setPadding(new Insets(16));
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.92); -fx-background-radius: 20;");
        return card;
    }

    private void refreshAll() {
        loadProducts();
        loadUsers();
        loadOrders();
    }

    private void loadProducts() {
        try {
            List<ProductDTO> products = productService.getAll();
            productsTable.setItems(FXCollections.observableArrayList(products));
            productsCount.setText(String.valueOf(products.size()));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void loadUsers() {
        try {
            List<UserDTO> users = adminService.getUsers();
            usersTable.setItems(FXCollections.observableArrayList(users));
            usersCount.setText(String.valueOf(users.size()));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void loadOrders() {
        try {
            List<OrderDTO> orders = adminService.getOrders();
            ordersTable.setItems(FXCollections.observableArrayList(orders));
            ordersCount.setText(String.valueOf(orders.size()));
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void addProduct() {
        try {
            ProductFormData data = showProductForm(null);
            if (data == null) {
                return;
            }
            adminService.addProduct(data.categorieId, data.nom, data.description, data.matiere, data.couleur,
                    data.prixOriginal, data.prixReduit, data.imageUrl);
            loadProducts();
            UIUtils.showSuccess("Produit ajoute avec succes.");
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void editProduct() {
        ProductDTO selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showError("Selectionnez un produit.");
            return;
        }
        try {
            ProductFormData data = showProductForm(selected);
            if (data == null) {
                return;
            }
            adminService.updateProduct(selected.getId(), data.nom, data.description, data.matiere, data.couleur,
                    data.prixOriginal, data.prixReduit, data.imageUrl);
            loadProducts();
            UIUtils.showSuccess("Produit modifie avec succes.");
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void updateStock() {
        ProductDTO selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showError("Selectionnez un produit.");
            return;
        }
        try {
            ProductDTO fullProduct = productService.getById(selected.getId());
            if (fullProduct.getTailles().isEmpty()) {
                UIUtils.showError("Ce produit n'a pas de tailles associees.");
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Stock par taille");
            DialogPane pane = dialog.getDialogPane();
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            pane.setStyle("-fx-background-color: #fffaf5;");

            ComboBox<com.chrionline.client.model.ProductSizeDTO> sizeBox =
                    new ComboBox<>(FXCollections.observableArrayList(fullProduct.getTailles()));
            sizeBox.setCellFactory(ignored -> new ListCell<>() {
                @Override
                protected void updateItem(com.chrionline.client.model.ProductSizeDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getValeur() + " (stock " + item.getStock() + ")");
                }
            });
            sizeBox.setButtonCell(sizeBox.getCellFactory().call(null));
            sizeBox.getSelectionModel().selectFirst();

            TextField stockField = new TextField(String.valueOf(sizeBox.getValue().getStock()));
            sizeBox.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    stockField.setText(String.valueOf(newValue.getStock()));
                }
            });

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(12);
            grid.addRow(0, new Label("Taille"), sizeBox);
            grid.addRow(1, new Label("Nouveau stock"), stockField);
            pane.setContent(grid);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                int newStock = Integer.parseInt(stockField.getText().trim());
                adminService.updateStock(sizeBox.getValue().getId(), newStock);
                loadProducts();
                UIUtils.showSuccess("Stock mis a jour.");
            }
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void deleteProduct() {
        ProductDTO selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showError("Selectionnez un produit.");
            return;
        }
        try {
            if (!UIUtils.confirm("Confirmation", "Supprimer ce produit du catalogue ?")) {
                return;
            }
            adminService.deleteProduct(selected.getId());
            loadProducts();
            UIUtils.showSuccess("Produit supprime avec succes.");
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void changeUserStatus(boolean suspend) {
        UserDTO selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showError("Selectionnez un utilisateur.");
            return;
        }
        try {
            if (suspend) {
                adminService.suspendUser(selected.getId());
                UIUtils.showSuccess("Utilisateur suspendu.");
            } else {
                adminService.activateUser(selected.getId());
                UIUtils.showSuccess("Utilisateur active.");
            }
            loadUsers();
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private ProductFormData showProductForm(ProductDTO product) throws Exception {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Ajouter un produit" : "Modifier un produit");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color: #fffaf5;");

        ComboBox<Integer> categorieField = new ComboBox<>();
        categorieField.setItems(FXCollections.observableArrayList(1, 2, 3, 4));
        categorieField.getSelectionModel().select(product == null ? Integer.valueOf(1) : product.getCategorieId());

        TextField nomField = new TextField(product == null ? "" : product.getNom());
        TextField descriptionField = new TextField(product == null ? "" : nullToEmpty(product.getDescription()));
        TextField matiereField = new TextField(product == null ? "" : nullToEmpty(product.getMatiere()));
        TextField couleurField = new TextField(product == null ? "" : nullToEmpty(product.getCouleur()));
        TextField prixOriginalField = new TextField(product == null ? "" : String.valueOf(product.getPrixOriginal()));
        TextField prixReduitField = new TextField(product == null ? "" : String.valueOf(product.getPrixReduit()));
        TextField imageUrlField = new TextField(product == null ? "" : nullToEmpty(product.getImageUrl()));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.addRow(0, new Label("Categorie ID"), categorieField);
        grid.addRow(1, new Label("Nom"), nomField);
        grid.addRow(2, new Label("Description"), descriptionField);
        grid.addRow(3, new Label("Matiere"), matiereField);
        grid.addRow(4, new Label("Couleur"), couleurField);
        grid.addRow(5, new Label("Prix original"), prixOriginalField);
        grid.addRow(6, new Label("Prix reduit"), prixReduitField);
        grid.addRow(7, new Label("Image URL"), imageUrlField);
        pane.setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return null;
        }

        ProductFormData data = new ProductFormData();
        data.categorieId = categorieField.getValue();
        data.nom = nomField.getText().trim();
        data.description = descriptionField.getText().trim();
        data.matiere = matiereField.getText().trim();
        data.couleur = couleurField.getText().trim();
        data.prixOriginal = Double.parseDouble(prixOriginalField.getText().trim());
        data.prixReduit = prixReduitField.getText().trim();
        data.imageUrl = imageUrlField.getText().trim();
        return data;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static final class ProductFormData {
        int categorieId;
        String nom;
        String description;
        String matiere;
        String couleur;
        double prixOriginal;
        String prixReduit;
        String imageUrl;
    }
}
