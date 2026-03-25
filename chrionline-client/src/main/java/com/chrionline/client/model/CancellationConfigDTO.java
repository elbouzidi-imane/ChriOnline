package com.chrionline.client.model;

public class CancellationConfigDTO {
    private String cancellableStatus;
    private boolean reasonRequired;
    private boolean automaticRefund;
    private String estimatedRefundDelay;

    public String getCancellableStatus() {
        return cancellableStatus;
    }

    public boolean isReasonRequired() {
        return reasonRequired;
    }

    public boolean isAutomaticRefund() {
        return automaticRefund;
    }

    public String getEstimatedRefundDelay() {
        return estimatedRefundDelay;
    }
}
