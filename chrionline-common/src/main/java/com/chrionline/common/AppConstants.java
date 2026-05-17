package com.chrionline.common;
public class AppConstants {
    public static final String HOST     = "localhost";
    public static final int    PORT_TCP = 8080;
    public static final int    PORT_UDP = 8081;
    public static final int    PORT_PAYMENT_TLS = 8443;
    public static final int    PAYMENT_TLS_TIMEOUT_MILLIS = 10_000;
    public static final String PAYMENT_TLS_KEYSTORE_PATH = "chrionline-server/src/main/resources/tls/payment-keystore.jks";
    public static final String PAYMENT_TLS_TRUSTSTORE_PATH = "chrionline-client/src/main/resources/tls/payment-truststore.jks";
    public static final String PAYMENT_TLS_STORE_PASSWORD = "changeit";
    public static final long   REQUEST_SIGNATURE_WINDOW_MILLIS = 60_000L;
    public static final String DEFAULT_HMAC_SECRET = "change-this-dev-secret";
    public static final int    TCP_ACCEPT_BACKLOG = 50;
    public static final int    CLIENT_SOCKET_TIMEOUT_MILLIS = 15_000;
    public static final int    SERVER_WORKER_THREADS = 32;
    public static final int    SERVER_QUEUE_CAPACITY = 100;
    public static final long   UDP_RATE_LIMIT_WINDOW_MILLIS = 10_000L;
    public static final int    UDP_MAX_PACKETS_PER_IP = 20;
    public static final long   SESSION_TTL_MILLIS = 30 * 60_000L;
    public static final long   SENSITIVE_ACTION_OTP_TTL_MILLIS = 5 * 60_000L;
}
