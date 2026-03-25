package com.chrionline.server.service;

import com.chrionline.server.model.CancellationConfig;

public final class CancellationConfigService {
    private static final CancellationConfig CONFIG = new CancellationConfig();

    private CancellationConfigService() {
    }

    public static CancellationConfig getConfig() {
        return CONFIG;
    }

    public static CancellationConfig updateConfig(String cancellableStatus, boolean reasonRequired,
                                                  boolean automaticRefund, String estimatedRefundDelay) {
        if (cancellableStatus != null && !cancellableStatus.isBlank()) {
            CONFIG.setCancellableStatus(cancellableStatus.trim());
        }
        CONFIG.setReasonRequired(reasonRequired);
        CONFIG.setAutomaticRefund(automaticRefund);
        CONFIG.setEstimatedRefundDelay(
                estimatedRefundDelay == null || estimatedRefundDelay.isBlank()
                        ? "3 a 5 jours ouvrables"
                        : estimatedRefundDelay.trim()
        );
        return CONFIG;
    }
}
