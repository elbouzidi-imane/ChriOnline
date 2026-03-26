package com.chrionline.server.model;

public class CancellationResult {
    private final String orderReference;
    private final String status;
    private final String message;
    private final String refundMessage;
    private final String estimatedRefundDelay;

    public CancellationResult(String orderReference, String status, String message,
                              String refundMessage, String estimatedRefundDelay) {
        this.orderReference = orderReference;
        this.status = status;
        this.message = message;
        this.refundMessage = refundMessage;
        this.estimatedRefundDelay = estimatedRefundDelay;
    }

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
