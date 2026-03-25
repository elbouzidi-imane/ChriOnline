package com.chrionline.server.model;

public class CancellationConfig {
    private String cancellableStatus = "VALIDEE";
    private boolean reasonRequired = true;
    private boolean automaticRefund = true;
    private String estimatedRefundDelay = "3 a 5 jours ouvrables";

    public String getCancellableStatus() {
        return cancellableStatus;
    }

    public void setCancellableStatus(String cancellableStatus) {
        this.cancellableStatus = cancellableStatus;
    }

    public boolean isReasonRequired() {
        return reasonRequired;
    }

    public void setReasonRequired(boolean reasonRequired) {
        this.reasonRequired = reasonRequired;
    }

    public boolean isAutomaticRefund() {
        return automaticRefund;
    }

    public void setAutomaticRefund(boolean automaticRefund) {
        this.automaticRefund = automaticRefund;
    }

    public String getEstimatedRefundDelay() {
        return estimatedRefundDelay;
    }

    public void setEstimatedRefundDelay(String estimatedRefundDelay) {
        this.estimatedRefundDelay = estimatedRefundDelay;
    }
}
