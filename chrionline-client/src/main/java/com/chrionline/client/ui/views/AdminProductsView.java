package com.chrionline.client.ui.views;

import com.chrionline.client.model.CategoryDTO;
import com.chrionline.client.model.GuideDTO;
import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.model.ProductSizeDTO;
import com.chrionline.client.service.AdminService;
import com.chrionline.client.service.ProductService;
import com.chrionline.client.util.PriceUtils;
import com.chrionline.client.util.UIUtils;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

public class AdminProductsView extends VBox {
    private static final Path PRODUCT_IMAGES_DIR = Path.of(System.getProperty("user.dir"), "product-images");
    private final AdminService adminService = new AdminService();
    private final ProductService productService = new ProductService();
    private final TableView<ProductDTO> productsTable = new TableView<>();
    private List<CategoryDTO> categories = List.of();

    public AdminProductsView() {
        if (!AdminViewSupport.ensureAdmin()) {
            return;
        }

        VBox content = new VBox(18);

        FlowPane headerActions = new FlowPane();
        headerActions.setHgap(10);
        headerActions.setVgap(10);
        Button refresh = ViewFactory.createSecondaryButton("Actualiser");
        refresh.setOnAction(event -> loadProducts());
        Button add = ViewFactory.createPrimaryButton("Ajouter un produit");
        add.setOnAction(event -> addProduct());
        Button categoriesInfo = ViewFactory.createSecondaryButton("Voir categories");
        categoriesInfo.setOnAction(event -> showCategoryInfo());
        Button addCategory = ViewFactory.createPrimaryButton("Ajouter categorie");
        addCategory.setOnAction(event -> addCategory());
        Button edit = ViewFactory.createSecondaryButton("Modifier produit");
        edit.setOnAction(event -> editProduct());
        Button stock = ViewFactory.createSecondaryButton("Gerer stock par taille");
        stock.setOnAction(event -> updateStock());
        Button sizes = ViewFactory.createSecondaryButton("Gerer tailles");
        sizes.setOnAction(event -> manageSizes());
        Button guides = ViewFactory.createSecondaryButton("Gerer guides");
        guides.setOnAction(event -> manageGuides());
        Button delete = ViewFactory.createSecondaryButton("Supprimer produit");
        delete.setOnAction(event -> deleteProduct());
        headerActions.getChildren().addAll(refresh, add, addCategory, categoriesInfo, edit, stock, sizes, guides, delete);

        Label hint = new Label("Cette page gere les produits, le stock par taille et les guides. Les categories sont choisies par nom pour eviter la saisie manuelle d'un id.");
        hint.setWrapText(true);
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #5b5f63;");

        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        productsTable.setStyle("-fx-background-radius: 18; -fx-border-radius: 18;");

        TableColumn<ProductDTO, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNom()));

        TableColumn<ProductDTO, String> categoryCol = new TableColumn<>("Categorie");
        categoryCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(resolveCategoryName(data.getValue())));

        TableColumn<ProductDTO, String> priceCol = new TableColumn<>("Prix");
        priceCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(PriceUtils.formatMad(data.getValue().getPrixAffiche())));

        TableColumn<ProductDTO, String> colorCol = new TableColumn<>("Couleur");
        colorCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(valueOrDash(data.getValue().getCouleur())));

        TableColumn<ProductDTO, String> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().getStock())));

        productsTable.getColumns().setAll(nameCol, categoryCol, priceCol, colorCol, stockCol);

        VBox card = new VBox(14, headerActions, hint, productsTable);
        card.setPadding(new Insets(18));
        card.setStyle(ViewFactory.cardStyle());
        VBox.setVgrow(productsTable, Priority.ALWAYS);

        content.getChildren().add(card);
        getChildren().add(AdminViewSupport.createAdminPage(
                "Gestion des produits",
                "Ajoutez et mettez a jour le catalogue depuis une page dediee.",
                "products",
                content
        ));

        loadProducts();
    }

    private void loadProducts() {
        try {
            try {
                categories = adminService.getAdminCategories();
            } catch (Exception ignored) {
                categories = productService.getCategories();
            }
            if (categories == null || categories.isEmpty()) {
                categories = productService.getCategories();
            }
            List<ProductDTO> products = productService.getAll();
            productsTable.setItems(FXCollections.observableArrayList(products));
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

    private void addCategory() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une categorie");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color: #fffaf5;");

        TextField nameField = new TextField();
        nameField.setPromptText("Nom de la categorie");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        VBox content = new VBox(12,
                new Label("Cette categorie apparaitra ensuite dans l'admin et dans le filtre client."),
                nameField,
                descriptionField);
        pane.setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            adminService.addCategory(nameField.getText().trim(), descriptionField.getText().trim());
            loadProducts();
            UIUtils.showSuccess("Categorie ajoutee avec succes.");
        } catch (Exception e) {
            UIUtils.showError(e.getMessage()
                    + "\n\nLe serveur doit aussi router ADMIN_ADD_CATEGORY et ADMIN_GET_CATEGORIES dans ClientHandler.");
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

            ComboBox<ProductSizeDTO> sizeBox = new ComboBox<>(FXCollections.observableArrayList(fullProduct.getTailles()));
            sizeBox.setCellFactory(ignored -> new ListCell<>() {
                @Override
                protected void updateItem(ProductSizeDTO item, boolean empty) {
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
                adminService.updateStock(sizeBox.getValue().getId(), Integer.parseInt(stockField.getText().trim()));
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

    private void manageSizes() {
        ProductDTO selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showError("Selectionnez un produit.");
            return;
        }
        try {
            List<ProductSizeDTO> sizes = adminService.getSizes(selected.getId());
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Gestion des tailles");
            DialogPane pane = dialog.getDialogPane();
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            pane.setStyle("-fx-background-color: #fffaf5;");

            ComboBox<ProductSizeDTO> sizeBox = new ComboBox<>(FXCollections.observableArrayList(sizes));
            sizeBox.setCellFactory(ignored -> new ListCell<>() {
                @Override
                protected void updateItem(ProductSizeDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getValeur() + " (stock " + item.getStock() + ")");
                }
            });
            sizeBox.setButtonCell(sizeBox.getCellFactory().call(null));
            if (!sizes.isEmpty()) {
                sizeBox.getSelectionModel().selectFirst();
            }

            TextField valueField = new TextField();
            valueField.setPromptText("Nouvelle taille");
            TextField stockField = new TextField();
            stockField.setPromptText("Stock");

            Button addButton = ViewFactory.createPrimaryButton("Ajouter");
            addButton.setOnAction(event -> {
                try {
                    adminService.addSize(selected.getId(), valueField.getText().trim(), Integer.parseInt(stockField.getText().trim()));
                    UIUtils.showSuccess("Taille ajoutee.");
                    dialog.close();
                    loadProducts();
                } catch (Exception e) {
                    UIUtils.showError(e.getMessage());
                }
            });

            Button deleteButton = ViewFactory.createSecondaryButton("Supprimer la taille selectionnee");
            deleteButton.setOnAction(event -> {
                if (sizeBox.getValue() == null) {
                    UIUtils.showError("Selectionnez une taille.");
                    return;
                }
                try {
                    adminService.deleteSize(sizeBox.getValue().getId());
                    UIUtils.showSuccess("Taille supprimee.");
                    dialog.close();
                    loadProducts();
                } catch (Exception e) {
                    UIUtils.showError(e.getMessage());
                }
            });

            VBox content = new VBox(12,
                    new Label("Tailles existantes"),
                    sizeBox,
                    new Label("Ajouter une nouvelle taille"),
                    valueField,
                    stockField,
                    new HBox(10, addButton, deleteButton));
            pane.setContent(content);
            dialog.showAndWait();
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private void manageGuides() {
        ProductDTO selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIUtils.showError("Selectionnez un produit.");
            return;
        }
        try {
            List<GuideDTO> guides = adminService.getGuides(selected.getId());
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Gestion des guides");
            DialogPane pane = dialog.getDialogPane();
            pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            pane.setStyle("-fx-background-color: #fffaf5;");

            ComboBox<GuideDTO> guideBox = new ComboBox<>(FXCollections.observableArrayList(guides));
            guideBox.setCellFactory(ignored -> new ListCell<>() {
                @Override
                protected void updateItem(GuideDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTaille() + " - poitrine " + valueOrDash(item.getPoitrine()));
                }
            });
            guideBox.setButtonCell(guideBox.getCellFactory().call(null));
            if (!guides.isEmpty()) {
                guideBox.getSelectionModel().selectFirst();
            }

            TextField tailleField = new TextField();
            tailleField.setPromptText("Taille");
            TextField poitrineField = new TextField();
            poitrineField.setPromptText("Poitrine");
            TextField tailleCmField = new TextField();
            tailleCmField.setPromptText("Tour de taille");
            TextField hanchesField = new TextField();
            hanchesField.setPromptText("Hanches");

            Button addButton = ViewFactory.createPrimaryButton("Ajouter guide");
            addButton.setOnAction(event -> {
                try {
                    adminService.addGuide(selected.getId(), tailleField.getText().trim(), poitrineField.getText().trim(),
                            tailleCmField.getText().trim(), hanchesField.getText().trim());
                    UIUtils.showSuccess("Guide ajoute.");
                    dialog.close();
                } catch (Exception e) {
                    UIUtils.showError(e.getMessage());
                }
            });

            Button deleteButton = ViewFactory.createSecondaryButton("Supprimer guide");
            deleteButton.setOnAction(event -> {
                if (guideBox.getValue() == null) {
                    UIUtils.showError("Selectionnez un guide.");
                    return;
                }
                try {
                    adminService.deleteGuide(guideBox.getValue().getId());
                    UIUtils.showSuccess("Guide supprime.");
                    dialog.close();
                } catch (Exception e) {
                    UIUtils.showError(e.getMessage());
                }
            });

            VBox content = new VBox(12,
                    new Label("Guides existants"),
                    guideBox,
                    new Label("Ajouter un guide"),
                    tailleField, poitrineField, tailleCmField, hanchesField,
                    new HBox(10, addButton, deleteButton));
            pane.setContent(content);
            dialog.showAndWait();
        } catch (Exception e) {
            UIUtils.showError(e.getMessage());
        }
    }

    private ProductFormData showProductForm(ProductDTO product) {
        if (categories == null || categories.isEmpty()) {
            try {
                try {
                    categories = adminService.getAdminCategories();
                } catch (Exception ignored) {
                    categories = productService.getCategories();
                }
                if (categories == null || categories.isEmpty()) {
                    categories = productService.getCategories();
                }
            } catch (Exception e) {
                UIUtils.showError("Impossible de charger les categories : " + e.getMessage());
                return null;
            }
        }
        if (categories == null || categories.isEmpty()) {
            UIUtils.showError("Aucune categorie disponible. Ajoutez d'abord une categorie.");
            return null;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Ajouter un produit" : "Modifier un produit");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setStyle("-fx-background-color: #fffaf5;");

        ComboBox<CategoryDTO> categorieField = new ComboBox<>(FXCollections.observableArrayList(categories));
        categorieField.setCellFactory(ignored -> new ListCell<>() {
            @Override
            protected void updateItem(CategoryDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom() + " (#" + item.getId() + ")");
            }
        });
        categorieField.setButtonCell(categorieField.getCellFactory().call(null));
        if (product == null) {
            if (!categories.isEmpty()) {
                categorieField.getSelectionModel().selectFirst();
            }
        } else {
            categories.stream()
                    .filter(category -> category.getId() == product.getCategorieId())
                    .findFirst()
                    .ifPresent(category -> categorieField.getSelectionModel().select(category));
        }

        TextField nomField = new TextField(product == null ? "" : product.getNom());
        TextField descriptionField = new TextField(product == null ? "" : nullToEmpty(product.getDescription()));
        TextField matiereField = new TextField(product == null ? "" : nullToEmpty(product.getMatiere()));
        TextField couleurField = new TextField(product == null ? "" : nullToEmpty(product.getCouleur()));
        TextField prixOriginalField = new TextField(product == null ? "" : String.valueOf(product.getPrixOriginal()));
        TextField prixReduitField = new TextField(product == null ? "" : String.valueOf(product.getPrixReduit()));
        TextField imagePathField = new TextField(product == null ? "" : nullToEmpty(product.getImageUrl()));
        imagePathField.setEditable(false);
        imagePathField.setPromptText("Aucune image importee");

        StackPane imagePreview = createImagePreview(imagePathField.getText());

        Button importImageButton = ViewFactory.createSecondaryButton("Importer image");
        importImageButton.setOnAction(event -> {
            try {
                String importedImage = importProductImage(dialog);
                if (importedImage != null) {
                    imagePathField.setText(importedImage);
                    refreshImagePreview(imagePreview, importedImage);
                }
            } catch (Exception e) {
                UIUtils.showError("Impossible d'importer l'image : " + e.getMessage());
            }
        });

        Button clearImageButton = ViewFactory.createSecondaryButton("Retirer image");
        clearImageButton.setOnAction(event -> {
            imagePathField.clear();
            refreshImagePreview(imagePreview, "");
        });

        HBox imageRow = new HBox(10, imagePathField, importImageButton, clearImageButton);
        HBox.setHgrow(imagePathField, Priority.ALWAYS);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.addRow(0, new Label("Categorie"), categorieField);
        grid.addRow(1, new Label("Nom"), nomField);
        grid.addRow(2, new Label("Description"), descriptionField);
        grid.addRow(3, new Label("Matiere"), matiereField);
        grid.addRow(4, new Label("Couleur"), couleurField);
        grid.addRow(5, new Label("Prix original"), prixOriginalField);
        grid.addRow(6, new Label("Prix reduit"), prixReduitField);
        grid.addRow(7, new Label("Image"), imageRow);
        grid.addRow(8, new Label("Apercu"), imagePreview);
        pane.setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return null;
        }

        ProductFormData data = new ProductFormData();
        if (categorieField.getValue() == null) {
            UIUtils.showError("Selectionnez une categorie.");
            return null;
        }
        if (nomField.getText().isBlank() || prixOriginalField.getText().isBlank()) {
            UIUtils.showError("Le nom et le prix original sont obligatoires.");
            return null;
        }
        data.categorieId = categorieField.getValue().getId();
        data.nom = nomField.getText().trim();
        data.description = descriptionField.getText().trim();
        data.matiere = matiereField.getText().trim();
        data.couleur = couleurField.getText().trim();
        try {
            data.prixOriginal = Double.parseDouble(normalizeDecimal(prixOriginalField.getText()));
        } catch (NumberFormatException e) {
            UIUtils.showError("Le prix original est invalide.");
            return null;
        }
        if (!prixReduitField.getText().isBlank()) {
            try {
                Double.parseDouble(normalizeDecimal(prixReduitField.getText()));
                data.prixReduit = normalizeDecimal(prixReduitField.getText());
            } catch (NumberFormatException e) {
                UIUtils.showError("Le prix reduit est invalide.");
                return null;
            }
        } else {
            data.prixReduit = "";
        }
        data.imageUrl = imagePathField.getText().trim();
        return data;
    }

    private String importProductImage(Dialog<ButtonType> dialog) throws Exception {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image produit");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.gif")
        );

        File selectedFile = chooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
        if (selectedFile == null) {
            return null;
        }

        Files.createDirectories(PRODUCT_IMAGES_DIR);

        String originalName = selectedFile.getName();
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }

        String safeName = "product-" + System.currentTimeMillis() + extension;
        Path destination = PRODUCT_IMAGES_DIR.resolve(safeName);
        Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toUri().toString();
    }

    private StackPane createImagePreview(String imageUrl) {
        StackPane preview = new StackPane();
        preview.setPrefSize(150, 150);
        preview.setMinSize(150, 150);
        preview.setMaxSize(150, 150);
        preview.setStyle("-fx-background-color: rgba(255,250,245,0.95); -fx-background-radius: 22;");
        refreshImagePreview(preview, imageUrl);
        return preview;
    }

    private void refreshImagePreview(StackPane preview, String imageUrl) {
        preview.getChildren().clear();

        Rectangle bg = new Rectangle(150, 150);
        bg.setArcWidth(24);
        bg.setArcHeight(24);
        bg.setFill(Color.web("#f3e6d8"));
        preview.getChildren().add(bg);

        if (imageUrl == null || imageUrl.isBlank()) {
            Label placeholder = new Label("Aucune image");
            placeholder.setStyle("-fx-font-size: 13px; -fx-text-fill: #7b6d62; -fx-font-weight: bold;");
            preview.getChildren().add(placeholder);
            return;
        }

        try {
            Image image = new Image(imageUrl, 140, 140, true, true, true);
            if (image.isError()) {
                throw new IllegalArgumentException("Image invalide");
            }
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(140);
            imageView.setFitHeight(140);
            imageView.setPreserveRatio(true);
            preview.getChildren().add(imageView);
        } catch (Exception e) {
            Label placeholder = new Label("Apercu indisponible");
            placeholder.setStyle("-fx-font-size: 13px; -fx-text-fill: #a94442; -fx-font-weight: bold;");
            preview.getChildren().add(placeholder);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String normalizeDecimal(String value) {
        return value == null ? "" : value.trim().replace(',', '.');
    }

    private String resolveCategoryName(ProductDTO product) {
        if (product.getCategorie() != null && product.getCategorie().getNom() != null
                && !product.getCategorie().getNom().isBlank()) {
            return product.getCategorie().getNom();
        }
        return categories.stream()
                .filter(category -> category.getId() == product.getCategorieId())
                .map(CategoryDTO::getNom)
                .findFirst()
                .orElse("-");
    }

    private void showCategoryInfo() {
        String categoryList = categories.isEmpty()
                ? "Aucune categorie chargee."
                : categories.stream()
                .map(category -> "- " + category.getNom() + " (#" + category.getId() + ")")
                .reduce((left, right) -> left + "\n" + right)
                .orElse("Aucune categorie chargee.");

        UIUtils.showInfo("Categories disponibles :\n" + categoryList
                + "\n\nSi l'ajout echoue encore, il faut completer le routage serveur dans ClientHandler.");
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
