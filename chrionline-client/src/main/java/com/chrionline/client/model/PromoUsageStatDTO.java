package com.chrionline.client.model;

import java.util.List;

public class PromoUsageStatDTO {
    private PromoCodeDTO promo;
    private int usageCount;
    private double totalDiscountAmount;
    private List<String> clients;
    private String status;

    public PromoCodeDTO getPromo() {
        return promo;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public double getTotalDiscountAmount() {
        return totalDiscountAmount;
    }

    public List<String> getClients() {
        return clients == null ? List.of() : clients;
    }

    public String getStatus() {
        return status;
    }
}
