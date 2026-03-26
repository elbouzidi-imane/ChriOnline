package com.chrionline.server.service;

import com.chrionline.server.model.Order;
import com.chrionline.server.model.OrderLine;
import com.chrionline.server.model.ProductReview;
import com.chrionline.server.repository.OrderDAO;
import com.chrionline.server.repository.ReviewDAO;

import java.util.List;

public class ReviewService {
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    public ProductReview addReview(int userId, int orderId, int productId, int note, String commentaire, String avisTaille) {
        if (note < 1 || note > 5) {
            return null;
        }
        if (!List.of("PETIT", "JUSTE", "GRAND").contains(avisTaille)) {
            return null;
        }

        Order order = orderDAO.findById(orderId);
        if (order == null || order.getUtilisateurId() != userId || "ANNULEE".equalsIgnoreCase(order.getStatut())) {
            return null;
        }

        boolean productOrdered = false;
        for (OrderLine line : order.getLignes()) {
            if (line.getProduitId() == productId) {
                productOrdered = true;
                break;
            }
        }
        if (!productOrdered || reviewDAO.existsByUserOrderProduct(userId, orderId, productId)) {
            return null;
        }

        ProductReview review = new ProductReview();
        review.setUtilisateurId(userId);
        review.setCommandeId(orderId);
        review.setProduitId(productId);
        review.setNote(note);
        review.setCommentaire(commentaire == null ? "" : commentaire.trim());
        review.setAvisTaille(avisTaille);
        return reviewDAO.save(review);
    }

    public List<ProductReview> getProductReviews(int productId) {
        return reviewDAO.findByProduct(productId);
    }
}
