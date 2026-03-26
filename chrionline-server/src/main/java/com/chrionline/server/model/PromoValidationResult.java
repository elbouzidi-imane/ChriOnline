package com.chrionline.server.model;

public class PromoValidationResult {
    private final boolean valid;
    private final String message;
    private final String code;
    private final double originalTotal;
    private final double discountAmount;
    private final double finalTotal;

    public PromoValidationResult(boolean valid, String message, String code,
                                 double originalTotal, double discountAmount, double finalTotal) {
        this.valid = valid;
        this.message = message;
        this.code = code;
        this.originalTotal = originalTotal;
        this.discountAmount = discountAmount;
        this.finalTotal = finalTotal;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public double getOriginalTotal() {
        return originalTotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getFinalTotal() {
        return finalTotal;
    }
}
