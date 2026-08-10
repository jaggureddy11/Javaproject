package com.routeresq.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProperties {

    @Value("${routeresq.jwt.secret:super-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256-security-routeresq-2026}")
    private String secret;

    @Value("${routeresq.jwt.expiration-ms:900000}")
    private long expirationMs; // Default 15 minutes (900,000 ms)

    @Value("${routeresq.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs; // Default 7 days

    public JwtProperties() {
    }

    public JwtProperties(String secret, long expirationMs, long refreshExpirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public void setRefreshExpirationMs(long refreshExpirationMs) {
        this.refreshExpirationMs = refreshExpirationMs;
    }
}
