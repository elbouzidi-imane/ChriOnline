package com.chrionline.client.model;

public class CancellationResultDTO {
    private String orderReference;
    private String status;
    private String message;
    private String refundMessage;
    private String estimatedRefundDelay;

    public String getOrderReference() {
        return orderReference;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getRefundMessage() {
        return refundMessage;
    }

    public String getEstimatedRefundDelay() {
        return estimatedRefundDelay;
    }
}
