package com.chrionline.server.service;

import com.chrionline.server.model.PromoCode;
import com.chrionline.server.model.PromoUsageStat;
import com.chrionline.server.model.PromoValidationResult;
import com.chrionline.server.repository.PromoCodeDAO;
import com.chrionline.server.repository.PromoUsageDAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PromoService {
    private final PromoCodeDAO promoCodeDAO = new PromoCodeDAO();
    private final PromoUsageDAO promoUsageDAO = new PromoUsageDAO();

    public PromoCode addPromo(PromoCode promo) {
        if (promo == null || promo.getCode() == null || promo.getCode().isBlank()) {
            return null;
        }
        if (promoCodeDAO.existsByCode(promo.getCode())) {
            return null;
        }
        if (promo.getStartDate() != null && promo.getEndDate() != null
                && promo.getEndDate().before(promo.getStartDate())) {
            return null;
        }
        return promoCodeDAO.save(promo);
    }

    public boolean existsByCode(String code) {
        return code != null && !code.isBlank() && promoCodeDAO.existsByCode(code);
    }

    public List<PromoCode> getPromos() {
        return promoCodeDAO.findAll();
    }

    public boolean togglePromo(int promoId, boolean active) {
        return promoCodeDAO.updateActive(promoId, active);
    }

    public PromoValidationResult validatePromo(String code, double cartTotal, int userId) {
        if (code == null || code.isBlank()) {
            return new PromoValidationResult(false, "Code promo obligatoire.", "", cartTotal, 0, cartTotal);
        }

        PromoCode promo = promoCodeDAO.findByCode(code.trim());
        if (promo == null) {
            return new PromoValidationResult(false, "Code promo introuvable.", code, cartTotal, 0, cartTotal);
        }
        if (!promo.isActive()) {
            return new PromoValidationResult(false, "Code promo desactive.", promo.getCode(), cartTotal, 0, cartTotal);
        }

        Date today = new Date();
        if (promo.getStartDate() != null && today.before(promo.getStartDate())) {
            return new PromoValidationResult(false, "Le code promo n'est pas encore actif.", promo.getCode(), cartTotal, 0, cartTotal);
        }
        if (promo.getEndDate() != null && today.after(promo.getEndDate())) {
            return new PromoValidationResult(false, "Le code promo est expire.", promo.getCode(), cartTotal, 0, cartTotal);
        }
        if (cartTotal < promo.getMinimumOrderAmount()) {
            return new PromoValidationResult(
                    false,
                    "Montant minimum non atteint.",
                    promo.getCode(),
                    cartTotal,
                    0,
                    cartTotal
            );
        }

        if (promo.getMaxUses() > 0 && promoUsageDAO.countUsages(promo.getId()) >= promo.getMaxUses()) {
            return new PromoValidationResult(false, "Nombre maximum d'utilisations atteint.", promo.getCode(), cartTotal, 0, cartTotal);
        }
        if (promo.getMaxUsagePerUser() > 0 && promoUsageDAO.countUsagesByUser(promo.getId(), userId) >= promo.getMaxUsagePerUser()) {
            return new PromoValidationResult(false, "Vous avez deja utilise ce code promo.", promo.getCode(), cartTotal, 0, cartTotal);
        }

        double discount = computeDiscount(promo, cartTotal);
        double finalTotal = Math.max(0, cartTotal - discount);
        return new PromoValidationResult(
                true,
                "Reduction appliquee avec succes.",
                promo.getCode(),
                cartTotal,
                discount,
                finalTotal
        );
    }

    public boolean registerUsage(String code, int userId, int orderId, double discountAmount) {
        PromoCode promo = promoCodeDAO.findByCode(code);
        if (promo == null) {
            return false;
        }
        return promoUsageDAO.saveUsage(promo.getId(), userId, orderId, discountAmount);
    }

    public PromoUsageStat getPromoStats(String code) {
        PromoCode promo = promoCodeDAO.findByCode(code);
        if (promo == null) {
            return null;
        }

        PromoUsageStat stat = new PromoUsageStat();
        stat.setPromo(promo);
        stat.setUsageCount(promoUsageDAO.countUsages(promo.getId()));
        stat.setTotalDiscountAmount(promoUsageDAO.sumDiscount(promo.getId()));
        stat.setClients(new ArrayList<>(promoUsageDAO.findClients(promo.getId())));
        stat.setStatus(resolveStatus(promo));
        return stat;
    }

    private double computeDiscount(PromoCode promo, double cartTotal) {
        if ("PERCENTAGE".equalsIgnoreCase(promo.getReductionType())) {
            return Math.round((cartTotal * promo.getReductionValue() / 100.0) * 100.0) / 100.0;
        }
        return Math.min(cartTotal, promo.getReductionValue());
    }

    private String resolveStatus(PromoCode promo) {
        if (!promo.isActive()) {
            return "DESACTIVE";
        }
        Date today = new Date();
        if (promo.getEndDate() != null && today.after(promo.getEndDate())) {
            return "EXPIRE";
        }
        return "ACTIF";
    }
}
