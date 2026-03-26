package com.chrionline.client.model;

public class PromoValidationResultDTO {
    private boolean valid;
    private String message;
    private String code;
    private double originalTotal;
    private double discountAmount;
    private double finalTotal;

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
