package com.chrionline.server.model;

import java.util.ArrayList;
import java.util.List;

public class PromoUsageStat {
    private PromoCode promo;
    private int usageCount;
    private double totalDiscountAmount;
    private List<String> clients = new ArrayList<>();
    private String status;

    public PromoCode getPromo() {
        return promo;
    }

    public void setPromo(PromoCode promo) {
        this.promo = promo;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public double getTotalDiscountAmount() {
        return totalDiscountAmount;
    }

    public void setTotalDiscountAmount(double totalDiscountAmount) {
        this.totalDiscountAmount = totalDiscountAmount;
    }

    public List<String> getClients() {
        return clients;
    }

    public void setClients(List<String> clients) {
        this.clients = clients;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
