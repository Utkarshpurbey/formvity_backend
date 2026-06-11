package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.dto.analytics.QuestionAnalyticsDto;
import com.example.uttuCodes.formvity.dto.analytics.QuestionDistributionItemDto;
import com.example.uttuCodes.formvity.dto.analytics.TextAnswerSampleDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class QuestionAnalyticsBuilder {

    private static final Set<String> CHOICE_TYPES = Set.of(
            "multiple_choice", "dropdown", "radio", "checkbox", "checkboxes", "select");
    private static final Set<String> SCALE_TYPES = Set.of(
            "linear_scale", "rating", "scale", "number");

    private QuestionAnalyticsBuilder() {}

    static List<QuestionAnalyticsDto> build(
            List<Map<String, Object>> fields,
            List<Map<String, Object>> allAnswers,
            long totalSubmissions) {
        if (fields.isEmpty()) {
            return List.of();
        }

        List<QuestionAnalyticsDto> results = new ArrayList<>();
        for (Map<String, Object> field : fields) {
            String fieldId = FormAnalyticsSupport.fieldId(field);
            if (fieldId == null) {
                continue;
            }
            String label = FormAnalyticsSupport.fieldLabel(field);
            if (label == null) {
                label = fieldId;
            }
            results.add(buildOne(
                    fieldId,
                    FormAnalyticsSupport.normalizeType(FormAnalyticsSupport.stringValue(field.get("type"))),
                    label,
                    field,
                    allAnswers,
                    totalSubmissions));
        }
        return results;
    }

    private static QuestionAnalyticsDto buildOne(
            String fieldId,
            String type,
            String label,
            Map<String, Object> fieldDef,
            List<Map<String, Object>> allAnswers,
            long totalSubmissions) {

        long responseCount = 0;
        List<Double> numericValues = new ArrayList<>();
        Map<String, Long> distributionCounts = new LinkedHashMap<>();
        Map<String, Long> textAnswerCounts = new LinkedHashMap<>();
        Map<String, String> optionLabels = buildOptionLabels(fieldDef);

        for (Map<String, Object> answers : allAnswers) {
            if (answers == null || !answers.containsKey(fieldId)) {
                continue;
            }
            Object rawValue = answers.get(fieldId);
            if (FormAnalyticsSupport.isBlankAnswer(rawValue)) {
                continue;
            }
            responseCount++;

            if (CHOICE_TYPES.contains(type)) {
                accumulateChoiceAnswers(rawValue, distributionCounts);
            } else if (SCALE_TYPES.contains(type)) {
                collectNumericValue(rawValue, numericValues, distributionCounts);
            } else {
                distributionCounts.merge("answered", 1L, Long::sum);
                String text = FormAnalyticsSupport.stringValue(rawValue);
                if (text != null && text.length() <= 200) {
                    textAnswerCounts.merge(text, 1L, Long::sum);
                }
            }
        }

        long skippedCount = totalSubmissions - responseCount;
        double completionRate = totalSubmissions == 0
                ? 0
                : FormAnalyticsSupport.roundPercent(100.0 * responseCount / totalSubmissions);

        QuestionAnalyticsDto.QuestionAnalyticsDtoBuilder builder = QuestionAnalyticsDto.builder()
                .fieldId(fieldId)
                .type(type)
                .label(label)
                .required(Boolean.TRUE.equals(fieldDef.get("required")))
                .responseCount(responseCount)
                .skippedCount(skippedCount)
                .completionRate(completionRate)
                .distribution(buildDistribution(distributionCounts, optionLabels, responseCount))
                .topTextAnswers(buildTopTextAnswers(textAnswerCounts, responseCount));

        if (!numericValues.isEmpty()) {
            Collections.sort(numericValues);
            builder.average(numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0))
                    .median(median(numericValues))
                    .min(Collections.min(numericValues))
                    .max(Collections.max(numericValues));
        }

        return builder.build();
    }

    private static Map<String, String> buildOptionLabels(Map<String, Object> fieldDef) {
        Map<String, String> labels = new LinkedHashMap<>();
        Object optionsObj = fieldDef.get("options");
        if (!(optionsObj instanceof List<?> options)) {
            return labels;
        }
        for (Object optionObj : options) {
            if (!(optionObj instanceof Map<?, ?> option)) {
                continue;
            }
            String id = FormAnalyticsSupport.stringValue(option.get("id"));
            String label = FormAnalyticsSupport.stringValue(option.get("label"));
            if (id != null) {
                labels.put(id, label != null ? label : id);
            } else if (label != null) {
                labels.put(label, label);
            }
        }
        return labels;
    }

    private static void accumulateChoiceAnswers(Object rawValue, Map<String, Long> distributionCounts) {
        for (Object value : flattenAnswer(rawValue)) {
            String key = FormAnalyticsSupport.stringValue(value);
            if (key != null) {
                distributionCounts.merge(key, 1L, Long::sum);
            }
        }
    }

    private static void collectNumericValue(
            Object rawValue, List<Double> numericValues, Map<String, Long> distributionCounts) {
        Double numeric = toDouble(rawValue);
        if (numeric == null) {
            return;
        }
        numericValues.add(numeric);
        String bucket = numeric == Math.rint(numeric) ? String.valueOf(numeric.longValue()) : String.valueOf(numeric);
        distributionCounts.merge(bucket, 1L, Long::sum);
    }

    private static List<QuestionDistributionItemDto> buildDistribution(
            Map<String, Long> distributionCounts,
            Map<String, String> optionLabels,
            long responseCount) {
        if (distributionCounts.isEmpty()) {
            return List.of();
        }
        List<QuestionDistributionItemDto> items = new ArrayList<>();
        for (Map.Entry<String, Long> entry : distributionCounts.entrySet()) {
            double percent = responseCount == 0 ? 0
                    : FormAnalyticsSupport.roundPercent(100.0 * entry.getValue() / responseCount);
            items.add(new QuestionDistributionItemDto(
                    entry.getKey(),
                    optionLabels.getOrDefault(entry.getKey(), entry.getKey()),
                    entry.getValue(),
                    percent));
        }
        return items;
    }

    private static List<TextAnswerSampleDto> buildTopTextAnswers(
            Map<String, Long> textAnswerCounts, long responseCount) {
        if (textAnswerCounts.isEmpty()) {
            return List.of();
        }
        return textAnswerCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> TextAnswerSampleDto.builder()
                        .value(e.getKey())
                        .count(e.getValue())
                        .percent(responseCount == 0 ? 0
                                : FormAnalyticsSupport.roundPercent(100.0 * e.getValue() / responseCount))
                        .build())
                .toList();
    }

    private static Double median(List<Double> values) {
        int mid = values.size() / 2;
        return values.size() % 2 == 0
                ? (values.get(mid - 1) + values.get(mid)) / 2.0
                : values.get(mid);
    }

    private static List<Object> flattenAnswer(Object value) {
        if (value instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        if (value instanceof Object[] array) {
            return List.of(array);
        }
        return List.of(value);
    }

    private static Double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
