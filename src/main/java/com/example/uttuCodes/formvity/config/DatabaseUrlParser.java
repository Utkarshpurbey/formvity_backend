package com.example.uttuCodes.formvity.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Parses Render-style {@code postgres://} / {@code postgresql://} URLs into JDBC settings.
 */
public final class DatabaseUrlParser {

    private DatabaseUrlParser() {
    }

    public static ConnectionDetails parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Database URL is empty");
        }

        String trimmed = raw.trim();
        if (trimmed.startsWith("jdbc:")) {
            return new ConnectionDetails(trimmed, null, null);
        }

        String schemeless = trimmed;
        if (schemeless.startsWith("postgres://")) {
            schemeless = schemeless.substring("postgres://".length());
        } else if (schemeless.startsWith("postgresql://")) {
            schemeless = schemeless.substring("postgresql://".length());
        }

        String user = null;
        String password = null;
        String hostAndDb = schemeless;

        int at = schemeless.indexOf('@');
        if (at > 0) {
            String userInfo = schemeless.substring(0, at);
            hostAndDb = schemeless.substring(at + 1);
            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                user = decode(userInfo.substring(0, colon));
                password = decode(userInfo.substring(colon + 1));
            } else {
                user = decode(userInfo);
            }
        }

        return new ConnectionDetails("jdbc:postgresql://" + hostAndDb, user, password);
    }

    public static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    public record ConnectionDetails(String jdbcUrl, String username, String password) {}
}
