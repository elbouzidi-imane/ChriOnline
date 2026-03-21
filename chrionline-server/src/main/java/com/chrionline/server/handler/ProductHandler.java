package com.chrionline.server.handler;

import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.server.model.Category;
import com.chrionline.server.model.GuideTaille;
import com.chrionline.server.model.Product;
import com.chrionline.server.model.ProductSize;
import com.chrionline.server.repository.GuideDAO;
import com.chrionline.server.repository.SizeDAO;
import com.chrionline.server.service.ProductService;
import com.google.gson.Gson;

import java.util.List;

public class ProductHandler {

    private final ProductService productService = new ProductService();
    private final GuideDAO       guideDAO       = new GuideDAO();
    private final SizeDAO        sizeDAO        = new SizeDAO();
    private final Gson           gson           = new Gson();

    public Message handle(Message request) {
        return switch (request.getType()) {
            case Protocol.GET_PRODUCTS              -> handleGetProducts(request);
            case Protocol.GET_PRODUCT               -> handleGetProduct(request);
            case Protocol.GET_CATEGORIES            -> handleGetCategories(request);
            case Protocol.GET_PRODUCTS_BY_CATEGORIE -> handleGetProductsByCategorie(request);
            default -> Message.error("Type non géré par ProductHandler");
        };
    }

    // ── GET_PRODUCTS ──────────────────────────────────
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

    // ── GET_PRODUCT (avec tailles + guides) ───────────
    private Message handleGetProduct(Message req) {
        try {
            int id = Integer.parseInt(req.getPayload().trim());
            Product product = productService.getProductById(id);
            if (product == null) return Message.error("Produit introuvable");

            // Enrichir avec tailles et guides
            List<ProductSize> sizes  = sizeDAO.findByProduit(id);
            List<GuideTaille> guides = guideDAO.findByProduit(id);
            product.setTailles(sizes);
            product.setGuides(guides);

            return Message.ok(Protocol.GET_PRODUCT, gson.toJson(product));
        } catch (Exception e) {
            return Message.error("ID produit invalide");
        }
    }

    // ── GET_CATEGORIES ────────────────────────────────
    private Message handleGetCategories(Message req) {
        List<Category> categories = productService.getAllCategories();
        return Message.ok(Protocol.GET_CATEGORIES, gson.toJson(categories));
    }

    // ── GET_PRODUCTS_BY_CATEGORIE ─────────────────────
    private Message handleGetProductsByCategorie(Message req) {
        try {
            int categorieId = Integer.parseInt(req.getPayload().trim());
            List<Product> products = productService.getProductsByCategorie(categorieId);
            return Message.ok(Protocol.GET_PRODUCTS_BY_CATEGORIE, gson.toJson(products));
        } catch (Exception e) {
            return Message.error("Erreur filtrage : " + e.getMessage());
        }
    }
}