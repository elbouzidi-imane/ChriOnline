package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.Category;
import com.chrionline.server.model.Product;
import com.chrionline.server.service.ProductService;
import com.google.gson.Gson;

import java.util.List;

public class ProductHandler {

    private final ProductService productService = new ProductService();
    private final Gson gson = new Gson();

    public Message handle(Message request) {
        return switch (request.getType()) {
            case Protocol.GET_PRODUCTS   -> handleGetProducts(request);
            case Protocol.GET_PRODUCT    -> handleGetProduct(request);
            case Protocol.GET_CATEGORIES -> handleGetCategories(request);
            default -> Message.error("Type non géré par ProductHandler");
        };
    }

    // ── GET_PRODUCTS ──────────────────────────────────
    private Message handleGetProducts(Message req) {
        String payload = req.getPayload();

        List<Product> products;

        // Si payload contient un id de catégorie → filtrer
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

        String json = gson.toJson(products);
        return Message.ok(Protocol.GET_PRODUCTS, json);
    }

    // ── GET_PRODUCT ───────────────────────────────────
    private Message handleGetProduct(Message req) {
        try {
            int id = Integer.parseInt(req.getPayload().trim());
            Product product = productService.getProductById(id);
            if (product == null) {
                return Message.error("Produit introuvable");
            }
            return Message.ok(Protocol.GET_PRODUCT, gson.toJson(product));
        } catch (Exception e) {
            return Message.error("ID produit invalide");
        }
    }

    // ── GET_CATEGORIES ────────────────────────────────
    private Message handleGetCategories(Message req) {
        List<Category> categories = productService.getAllCategories();
        String json = gson.toJson(categories);
        return Message.ok(Protocol.GET_CATEGORIES, json);
    }

}