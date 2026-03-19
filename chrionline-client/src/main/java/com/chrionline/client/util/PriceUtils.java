package com.chrionline.client.util;

public final class PriceUtils {
    private PriceUtils() {
    }

    public static String formatMad(double amount) {
        return String.format("%.2f DH", amount);
    }
}
