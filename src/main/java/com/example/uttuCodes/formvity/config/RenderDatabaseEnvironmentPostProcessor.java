package com.example.uttuCodes.formvity.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RenderDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE = "renderDatabase";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String rawUrl = firstNonBlank(
                environment.getProperty("SPRING_DATASOURCE_URL"),
                environment.getProperty("DATABASE_URL"));

        if (rawUrl == null || rawUrl.isBlank()) {
            return;
        }

        ParsedDb parsed = parse(rawUrl);
        Map<String, Object> props = new HashMap<>();
        props.put("spring.datasource.url", parsed.jdbcUrl());

        if (isBlank(environment.getProperty("SPRING_DATASOURCE_USERNAME")) && parsed.username() != null) {
            props.put("spring.datasource.username", parsed.username());
        }
        if (isBlank(environment.getProperty("SPRING_DATASOURCE_PASSWORD")) && parsed.password() != null) {
            props.put("spring.datasource.password", parsed.password());
        }

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, props));
    }

    static ParsedDb parse(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("jdbc:")) {
            return new ParsedDb(trimmed, null, null);
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

        return new ParsedDb("jdbc:postgresql://" + hostAndDb, user, password);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    record ParsedDb(String jdbcUrl, String username, String password) {}
}
