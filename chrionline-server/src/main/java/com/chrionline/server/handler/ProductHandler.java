package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.Category;
import com.chrionline.server.model.GuideTaille;
import com.chrionline.server.model.Product;
import com.chrionline.server.model.ProductReview;
import com.chrionline.server.model.ProductSize;
import com.chrionline.server.repository.GuideDAO;
import com.chrionline.server.repository.SizeDAO;
import com.chrionline.server.service.ProductService;
import com.chrionline.server.service.ReviewService;
import com.google.gson.Gson;

import java.util.List;

public class ProductHandler {

    private final ProductService productService = new ProductService();
    private final GuideDAO guideDAO = new GuideDAO();
    private final SizeDAO sizeDAO = new SizeDAO();
    private final ReviewService reviewService = new ReviewService();
    private final Gson gson = new Gson();

    public Message handle(Message request) {
        return switch (request.getType()) {
            case Protocol.GET_PRODUCTS -> handleGetProducts(request);
            case Protocol.GET_PRODUCT -> handleGetProduct(request);
            case Protocol.GET_CATEGORIES -> handleGetCategories();
            case Protocol.GET_PRODUCTS_BY_CATEGORIE -> handleGetProductsByCategorie(request);
            case Protocol.GET_PRODUCT_REVIEWS -> handleGetProductReviews(request);
            default -> Message.error("Type non gere par ProductHandler");
        };
    }

    private Message handleGetProducts(Message req) {
        String payload = req.getPayload();
        List<Product> products;
        if (payload != null && !payload.isEmpty()) {
            try {
                int categorieId = Integer.parseInt(payload.trim());
                products = productService.getProductsByCategorie(categorieId);
            } catch (NumberFormatException e) {
                products = productService.getAllProducts();
            }
        } else {
            products = productService.getAllProducts();
        }
        return Message.ok(Protocol.GET_PRODUCTS, gson.toJson(products));
    }

    private Message handleGetProduct(Message req) {
        try {
            int id = Integer.parseInt(req.getPayload().trim());
            Product product = productService.getProductById(id);
            if (product == null) {
                return Message.error("Produit introuvable");
            }

            List<ProductSize> sizes = sizeDAO.findByProduit(id);
            List<GuideTaille> guides = guideDAO.findByProduit(id);
            product.setTailles(sizes);
            product.setGuides(guides);

            return Message.ok(Protocol.GET_PRODUCT, gson.toJson(product));
        } catch (Exception e) {
            return Message.error("ID produit invalide");
        }
    }

    private Message handleGetCategories() {
        List<Category> categories = productService.getAllCategories();
        return Message.ok(Protocol.GET_CATEGORIES, gson.toJson(categories));
    }

    private Message handleGetProductsByCategorie(Message req) {
        try {
            int categorieId = Integer.parseInt(req.getPayload().trim());
            List<Product> products = productService.getProductsByCategorie(categorieId);
            return Message.ok(Protocol.GET_PRODUCTS_BY_CATEGORIE, gson.toJson(products));
        } catch (Exception e) {
            return Message.error("Erreur filtrage : " + e.getMessage());
        }
    }

    private Message handleGetProductReviews(Message req) {
        try {
            int productId = Integer.parseInt(req.getPayload().trim());
            List<ProductReview> reviews = reviewService.getProductReviews(productId);
            return Message.ok(Protocol.GET_PRODUCT_REVIEWS, gson.toJson(reviews));
        } catch (Exception e) {
            return Message.error("Erreur avis produit : " + e.getMessage());
        }
    }
}
