package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.dto.analytics.FormAnalyticsOverviewDto;
import com.example.uttuCodes.formvity.dto.analytics.FormAnalyticsSummaryDto;
import com.example.uttuCodes.formvity.dto.analytics.QuestionAnalyticsDto;
import com.example.uttuCodes.formvity.dto.analytics.QuestionDistributionItemDto;
import com.example.uttuCodes.formvity.dto.analytics.SubmissionListItemDto;
import com.example.uttuCodes.formvity.dto.analytics.TimelineBucketDto;
import com.example.uttuCodes.formvity.entity.FormEntity;
import com.example.uttuCodes.formvity.entity.FormPublicationEntity;
import com.example.uttuCodes.formvity.entity.SubmissionEntity;
import com.example.uttuCodes.formvity.exception.FormvityException;
import com.example.uttuCodes.formvity.repository.FormPublicationRepository;
import com.example.uttuCodes.formvity.repository.FormRepository;
import com.example.uttuCodes.formvity.repository.SubmissionRepository;
import com.example.uttuCodes.formvity.service.FormAnalyticsService;
import com.example.uttuCodes.formvity.utils.WorkspaceAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormAnalyticsServiceImpl implements FormAnalyticsService {

    private static final Set<String> CHOICE_TYPES = Set.of(
            "multiple_choice", "dropdown", "radio", "checkbox", "checkboxes", "select");
    private static final Set<String> SCALE_TYPES = Set.of(
            "linear_scale", "rating", "scale", "number");

    private final FormRepository formRepository;
    private final FormPublicationRepository formPublicationRepository;
    private final SubmissionRepository submissionRepository;
    private final WorkspaceAccessService workspaceAccessService;

    @Override
    @Transactional(readOnly = true)
    public FormAnalyticsOverviewDto getOverview(UUID workspaceId, UUID formId, UUID userId, int days) {
        return FormAnalyticsOverviewDto.builder()
                .summary(getSummary(workspaceId, formId, userId))
                .timeline(getTimeline(workspaceId, formId, userId, days))
                .questions(getQuestionBreakdown(workspaceId, formId, userId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FormAnalyticsSummaryDto getSummary(UUID workspaceId, UUID formId, UUID userId) {
        FormEntity form = requireFormAccess(workspaceId, formId, userId);
        long total = submissionRepository.countByForm_Id(form.getId());
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long today = submissionRepository.countByForm_IdAndCreatedAtAfter(form.getId(), startOfToday);

        FormPublicationEntity publication =
                formPublicationRepository.findByForm_IdAndCurrentTrue(form.getId()).orElse(null);

        return FormAnalyticsSummaryDto.builder()
                .totalResponses(total)
                .responsesToday(today)
                .firstResponseAt(submissionRepository.findFirstByForm_IdOrderByCreatedAtAsc(form.getId())
                        .map(SubmissionEntity::getCreatedAt)
                        .orElse(null))
                .lastResponseAt(submissionRepository.findFirstByForm_IdOrderByCreatedAtDesc(form.getId())
                        .map(SubmissionEntity::getCreatedAt)
                        .orElse(null))
                .currentPublicationVersion(publication != null ? publication.getVersion() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineBucketDto> getTimeline(UUID workspaceId, UUID formId, UUID userId, int days) {
        FormEntity form = requireFormAccess(workspaceId, formId, userId);
        int windowDays = Math.min(Math.max(days, 1), 90);
        LocalDateTime from = LocalDate.now().minusDays(windowDays - 1L).atStartOfDay();

        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (int i = 0; i < windowDays; i++) {
            counts.put(LocalDate.now().minusDays(windowDays - 1L - i), 0L);
        }

        for (Object[] row : submissionRepository.countDailyByFormIdSince(form.getId(), from)) {
            LocalDate day = toLocalDate(row[0]);
            long count = row[1] instanceof Number n ? n.longValue() : 0L;
            if (day != null) {
                counts.put(day, count);
            }
        }

        return counts.entrySet().stream()
                .map(e -> new TimelineBucketDto(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionAnalyticsDto> getQuestionBreakdown(UUID workspaceId, UUID formId, UUID userId) {
        FormEntity form = requireFormAccess(workspaceId, formId, userId);
        long totalSubmissions = submissionRepository.countByForm_Id(form.getId());
        if (totalSubmissions == 0) {
            return List.of();
        }

        Map<String, Object> pageDef = resolvePageDef(form);
        List<Map<String, Object>> fields = extractFields(pageDef);
        if (fields.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> allAnswers = submissionRepository.findAnswersByFormId(form.getId());
        List<QuestionAnalyticsDto> results = new ArrayList<>();

        for (Map<String, Object> field : fields) {
            String fieldId = stringValue(field.get("id"));
            if (fieldId == null) {
                continue;
            }
            String type = normalizeType(stringValue(field.get("type")));
            String label = stringValue(field.get("label"));
            if (label == null) {
                label = fieldId;
            }

            results.add(buildQuestionAnalytics(fieldId, type, label, field, allAnswers, totalSubmissions));
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubmissionListItemDto> listSubmissions(
            UUID workspaceId, UUID formId, UUID userId, int page, int size) {
        requireFormAccess(workspaceId, formId, userId);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return submissionRepository.findByForm_IdOrderByCreatedAtDesc(formId, pageable)
                .map(this::toListItem);
    }

    private FormEntity requireFormAccess(UUID workspaceId, UUID formId, UUID userId) {
        workspaceAccessService.requireUserExistInWorkSpace(userId, workspaceId);
        return formRepository.findByIdAndWorkspace_WorkSpaceId(formId, workspaceId)
                .orElseThrow(() -> FormvityException.notFound("Form not found: " + formId));
    }

    private Map<String, Object> resolvePageDef(FormEntity form) {
        return formPublicationRepository.findByForm_IdAndCurrentTrue(form.getId())
                .map(FormPublicationEntity::getPublishedPageDef)
                .filter(def -> def != null && !def.isEmpty())
                .orElse(form.getDraftPageDef());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractFields(Map<String, Object> pageDef) {
        if (pageDef == null) {
            return List.of();
        }
        Object pagesObj = pageDef.get("pages");
        if (!(pagesObj instanceof List<?> pages)) {
            return List.of();
        }

        List<Map<String, Object>> fields = new ArrayList<>();
        for (Object pageObj : pages) {
            if (!(pageObj instanceof Map<?, ?> page)) {
                continue;
            }
            Object fieldsObj = page.get("fields");
            if (!(fieldsObj instanceof List<?> pageFields)) {
                continue;
            }
            for (Object fieldObj : pageFields) {
                if (fieldObj instanceof Map<?, ?> rawField) {
                    fields.add((Map<String, Object>) rawField);
                }
            }
        }
        return fields;
    }

    private QuestionAnalyticsDto buildQuestionAnalytics(
            String fieldId,
            String type,
            String label,
            Map<String, Object> fieldDef,
            List<Map<String, Object>> allAnswers,
            long totalSubmissions) {

        long responseCount = 0;
        List<Double> numericValues = new ArrayList<>();
        Map<String, Long> distributionCounts = new LinkedHashMap<>();
        Map<String, String> optionLabels = buildOptionLabels(fieldDef);

        for (Map<String, Object> answers : allAnswers) {
            if (answers == null || !answers.containsKey(fieldId)) {
                continue;
            }
            Object rawValue = answers.get(fieldId);
            if (isBlankAnswer(rawValue)) {
                continue;
            }
            responseCount++;

            if (CHOICE_TYPES.contains(type)) {
                accumulateChoiceAnswers(rawValue, distributionCounts, optionLabels);
            } else if (SCALE_TYPES.contains(type)) {
                collectNumericValue(rawValue, numericValues, distributionCounts);
            } else {
                accumulateTextAnswer(rawValue, distributionCounts);
            }
        }

        long skippedCount = totalSubmissions - responseCount;
        List<QuestionDistributionItemDto> distribution = buildDistribution(distributionCounts, optionLabels, responseCount);

        QuestionAnalyticsDto.QuestionAnalyticsDtoBuilder builder = QuestionAnalyticsDto.builder()
                .fieldId(fieldId)
                .type(type)
                .label(label)
                .responseCount(responseCount)
                .skippedCount(skippedCount)
                .distribution(distribution);

        if (!numericValues.isEmpty()) {
            builder.average(numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0))
                    .min(Collections.min(numericValues))
                    .max(Collections.max(numericValues));
        }

        return builder.build();
    }

    private Map<String, String> buildOptionLabels(Map<String, Object> fieldDef) {
        Map<String, String> labels = new LinkedHashMap<>();
        Object optionsObj = fieldDef.get("options");
        if (!(optionsObj instanceof List<?> options)) {
            return labels;
        }
        for (Object optionObj : options) {
            if (!(optionObj instanceof Map<?, ?> option)) {
                continue;
            }
            String id = stringValue(option.get("id"));
            String label = stringValue(option.get("label"));
            if (id != null) {
                labels.put(id, label != null ? label : id);
            } else if (label != null) {
                labels.put(label, label);
            }
        }
        return labels;
    }

    private void accumulateChoiceAnswers(
            Object rawValue, Map<String, Long> distributionCounts, Map<String, String> optionLabels) {
        for (Object value : flattenAnswer(rawValue)) {
            String key = stringValue(value);
            if (key == null) {
                continue;
            }
            String normalizedKey = optionLabels.containsKey(key) ? key : key;
            distributionCounts.merge(normalizedKey, 1L, Long::sum);
        }
    }

    private void collectNumericValue(Object rawValue, List<Double> numericValues, Map<String, Long> distributionCounts) {
        Double numeric = toDouble(rawValue);
        if (numeric == null) {
            return;
        }
        numericValues.add(numeric);
        String bucket = formatNumber(numeric);
        distributionCounts.merge(bucket, 1L, Long::sum);
    }

    private void accumulateTextAnswer(Object rawValue, Map<String, Long> distributionCounts) {
        distributionCounts.merge("answered", 1L, Long::sum);
    }

    private List<QuestionDistributionItemDto> buildDistribution(
            Map<String, Long> distributionCounts,
            Map<String, String> optionLabels,
            long responseCount) {

        if (distributionCounts.isEmpty()) {
            return List.of();
        }

        List<QuestionDistributionItemDto> items = new ArrayList<>();
        for (Map.Entry<String, Long> entry : distributionCounts.entrySet()) {
            double percent = responseCount == 0 ? 0 : roundPercent(100.0 * entry.getValue() / responseCount);
            String label = optionLabels.getOrDefault(entry.getKey(), entry.getKey());
            items.add(new QuestionDistributionItemDto(entry.getKey(), label, entry.getValue(), percent));
        }
        return items;
    }

    private SubmissionListItemDto toListItem(SubmissionEntity submission) {
        return SubmissionListItemDto.builder()
                .id(submission.getId())
                .createdAt(submission.getCreatedAt())
                .publicationVersion(submission.getPublication().getVersion())
                .respondent(submission.getRespondent())
                .answers(submission.getAnswers())
                .build();
    }

    private LocalDate toLocalDate(Object value) {
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

    private String normalizeType(String type) {
        if (type == null) {
            return "text";
        }
        return type.toLowerCase(Locale.ROOT).trim();
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private boolean isBlankAnswer(Object value) {
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

    private List<Object> flattenAnswer(Object value) {
        if (value instanceof Collection<?> collection) {
            return new ArrayList<>(collection);
        }
        if (value instanceof Object[] array) {
            return List.of(array);
        }
        return List.of(value);
    }

    private Double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private double roundPercent(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
