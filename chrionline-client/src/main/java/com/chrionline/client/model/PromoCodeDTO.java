package com.chrionline.client.model;

import java.util.Date;

public class PromoCodeDTO {
    private int id;
    private String code;
    private String reductionType;
    private double reductionValue;
    private double minimumOrderAmount;
    private Date startDate;
    private Date endDate;
    private int maxUses;
    private int maxUsagePerUser;
    private boolean active;

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getReductionType() {
        return reductionType;
    }

    public double getReductionValue() {
        return reductionValue;
    }

    public double getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getMaxUsagePerUser() {
        return maxUsagePerUser;
    }

    public boolean isActive() {
        return active;
    }
}
