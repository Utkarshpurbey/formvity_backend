package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.entity.SubmissionEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class FormAnalyticsSupport {

    private FormAnalyticsSupport() {}

    static int clampDays(int days) {
        return Math.min(Math.max(days, 1), 90);
    }

    static List<SubmissionEntity> filterByDays(List<SubmissionEntity> submissions, Integer days) {
        if (days == null) {
            return submissions;
        }
        LocalDateTime from = LocalDate.now().minusDays(clampDays(days) - 1L).atStartOfDay();
        return submissions.stream()
                .filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().isBefore(from))
                .toList();
    }

    static long countSince(List<SubmissionEntity> submissions, LocalDateTime from) {
        return submissions.stream()
                .filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().isBefore(from))
                .count();
    }

    static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof java.util.Date utilDate) {
            return new Date(utilDate.getTime()).toLocalDate();
        }
        return null;
    }

    static String normalizeType(String type) {
        return type == null ? "text" : type.toLowerCase(Locale.ROOT).trim();
    }

    static String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    static boolean isBlankAnswer(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String s) {
            return s.trim().isEmpty();
        }
        if (value instanceof Collection<?> c) {
            return c.isEmpty();
        }
        return false;
    }

    static double roundPercent(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    static String fieldId(Map<String, Object> field) {
        return firstNonBlank(
                stringValue(field.get("id")),
                stringValue(field.get("fieldId")),
                stringValue(field.get("name")),
                stringValue(field.get("key")));
    }

    static String fieldLabel(Map<String, Object> field) {
        return firstNonBlank(
                stringValue(field.get("label")),
                stringValue(field.get("title")),
                stringValue(field.get("question")),
                stringValue(field.get("name")));
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> extractFields(Map<String, Object> pageDef) {
        if (pageDef == null) {
            return List.of();
        }

        List<Map<String, Object>> fields = new ArrayList<>();
        collectFieldMaps(pageDef.get("fields"), fields);

        Object pagesObj = pageDef.get("pages");
        if (pagesObj instanceof List<?> pages) {
            for (Object pageObj : pages) {
                if (!(pageObj instanceof Map<?, ?> page)) {
                    continue;
                }
                collectFieldMaps(page.get("fields"), fields);
                collectFieldMaps(page.get("questions"), fields);
                collectFieldMaps(page.get("elements"), fields);
            }
        }

        Object sectionsObj = pageDef.get("sections");
        if (sectionsObj instanceof List<?> sections) {
            for (Object sectionObj : sections) {
                if (!(sectionObj instanceof Map<?, ?> section)) {
                    continue;
                }
                collectFieldMaps(section.get("fields"), fields);
                collectFieldMaps(section.get("questions"), fields);
            }
        }

        return fields;
    }

    @SuppressWarnings("unchecked")
    private static void collectFieldMaps(Object fieldsObj, List<Map<String, Object>> fields) {
        if (!(fieldsObj instanceof List<?> pageFields)) {
            return;
        }
        for (Object fieldObj : pageFields) {
            if (fieldObj instanceof Map<?, ?> rawField) {
                fields.add((Map<String, Object>) rawField);
            }
        }
    }

    static List<Map<String, Object>> inferFieldsFromAnswers(List<Map<String, Object>> allAnswers) {
        Map<String, Map<String, Object>> inferred = new LinkedHashMap<>();
        for (Map<String, Object> answers : allAnswers) {
            if (answers == null) {
                continue;
            }
            for (String key : answers.keySet()) {
                if (key == null || key.isBlank()) {
                    continue;
                }
                inferred.putIfAbsent(key, Map.of("id", key, "type", "text", "label", key));
            }
        }
        return new ArrayList<>(inferred.values());
    }
}
