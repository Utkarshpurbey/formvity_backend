package com.example.uttuCodes.formvity.service.impl;

import com.example.uttuCodes.formvity.dto.analytics.FormAnalyticsInsightsDto;
import com.example.uttuCodes.formvity.dto.analytics.FormAnalyticsOverviewDto;
import com.example.uttuCodes.formvity.dto.analytics.FormAnalyticsSummaryDto;
import com.example.uttuCodes.formvity.dto.analytics.QuestionAnalyticsDto;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormAnalyticsServiceImpl implements FormAnalyticsService {

    private final FormRepository formRepository;
    private final FormPublicationRepository formPublicationRepository;
    private final SubmissionRepository submissionRepository;
    private final WorkspaceAccessService workspaceAccessService;

    @Override
    @Transactional(readOnly = true)
    public FormAnalyticsOverviewDto getOverview(UUID workspaceId, UUID formId, UUID userId, int days) {
        AnalyticsContext ctx = loadContext(workspaceId, formId, userId, days);
        FormAnalyticsInsightsDto insights = buildInsights(ctx, days);
        return FormAnalyticsOverviewDto.builder()
                .summary(buildSummary(ctx, insights))
                .timeline(buildTimeline(ctx.form(), days))
                .questions(buildQuestions(ctx))
                .insights(insights)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FormAnalyticsSummaryDto getSummary(UUID workspaceId, UUID formId, UUID userId) {
        AnalyticsContext ctx = loadContext(workspaceId, formId, userId, null);
        return buildSummary(ctx, SubmissionInsightsAnalyzer.analyze(ctx.submissions(), ctx.fieldDefs()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineBucketDto> getTimeline(UUID workspaceId, UUID formId, UUID userId, int days) {
        return buildTimeline(requireFormAccess(workspaceId, formId, userId), days);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionAnalyticsDto> getQuestionBreakdown(UUID workspaceId, UUID formId, UUID userId, int days) {
        return buildQuestions(loadContext(workspaceId, formId, userId, days));
    }

    @Override
    @Transactional(readOnly = true)
    public FormAnalyticsInsightsDto getInsights(UUID workspaceId, UUID formId, UUID userId, int days) {
        AnalyticsContext ctx = loadContext(workspaceId, formId, userId, days);
        return buildInsights(ctx, days);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubmissionListItemDto> listSubmissions(
            UUID workspaceId, UUID formId, UUID userId, int page, int size) {
        FormEntity form = requireFormAccess(workspaceId, formId, userId);
        int totalFieldCount = FormAnalyticsSupport.extractFields(resolvePageDef(form)).size();
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return submissionRepository.findByForm_IdOrderByCreatedAtDesc(form.getId(), pageable)
                .map(submission -> toListItem(submission, totalFieldCount));
    }

    private AnalyticsContext loadContext(UUID workspaceId, UUID formId, UUID userId, Integer days) {
        FormEntity form = requireFormAccess(workspaceId, formId, userId);
        List<SubmissionEntity> all = submissionRepository.findAllWithPublicationByFormId(form.getId());
        List<SubmissionEntity> filtered = FormAnalyticsSupport.filterByDays(all, days);
        List<Map<String, Object>> fieldDefs = resolveFieldDefs(form, all);
        int windowDays = days == null ? 0 : FormAnalyticsSupport.clampDays(days);
        return new AnalyticsContext(form, filtered, fieldDefs, windowDays);
    }

    private FormAnalyticsInsightsDto buildInsights(AnalyticsContext ctx, int days) {
        FormAnalyticsInsightsDto insights =
                SubmissionInsightsAnalyzer.analyze(ctx.submissions(), ctx.fieldDefs());
        insights.setWindowDays(ctx.windowDays() > 0 ? ctx.windowDays() : FormAnalyticsSupport.clampDays(days));
        return insights;
    }

    private FormAnalyticsSummaryDto buildSummary(AnalyticsContext ctx, FormAnalyticsInsightsDto insights) {
        List<SubmissionEntity> submissions = ctx.submissions();
        LocalDateTime today = LocalDate.now().atStartOfDay();

        FormPublicationEntity publication =
                formPublicationRepository.findByForm_IdAndCurrentTrue(ctx.form().getId()).orElse(null);

        return FormAnalyticsSummaryDto.builder()
                .totalResponses(submissions.size())
                .responsesToday(FormAnalyticsSupport.countSince(submissions, today))
                .responsesLast7Days(FormAnalyticsSupport.countSince(submissions, today.minusDays(6)))
                .responsesLast30Days(FormAnalyticsSupport.countSince(submissions, today.minusDays(29)))
                .firstResponseAt(submissions.isEmpty() ? null
                        : submissions.get(submissions.size() - 1).getCreatedAt())
                .lastResponseAt(submissions.isEmpty() ? null : submissions.get(0).getCreatedAt())
                .currentPublicationVersion(publication != null ? publication.getVersion() : null)
                .uniqueRespondents(insights.getAudience().getUniqueRespondents())
                .returningRespondents(insights.getAudience().getReturningRespondents())
                .avgCompletionRate(insights.getCompletion().getAvgCompletionRate())
                .peakHour(insights.getTemporal().getPeakHour())
                .peakDayOfWeek(insights.getTemporal().getPeakDayOfWeek())
                .submissionsWithMetadata(insights.getTraffic().getWithMetadata())
                .build();
    }

    private List<TimelineBucketDto> buildTimeline(FormEntity form, int days) {
        int windowDays = FormAnalyticsSupport.clampDays(days);
        LocalDateTime from = LocalDate.now().minusDays(windowDays - 1L).atStartOfDay();

        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (int i = 0; i < windowDays; i++) {
            counts.put(LocalDate.now().minusDays(windowDays - 1L - i), 0L);
        }
        for (Object[] row : submissionRepository.countDailyByFormIdSince(form.getId(), from)) {
            LocalDate day = FormAnalyticsSupport.toLocalDate(row[0]);
            if (day != null) {
                counts.put(day, row[1] instanceof Number n ? n.longValue() : 0L);
            }
        }
        return counts.entrySet().stream()
                .map(e -> new TimelineBucketDto(e.getKey(), e.getValue()))
                .toList();
    }

    private List<QuestionAnalyticsDto> buildQuestions(AnalyticsContext ctx) {
        List<Map<String, Object>> answers = ctx.submissions().stream()
                .map(SubmissionEntity::getAnswers)
                .toList();
        return QuestionAnalyticsBuilder.build(ctx.fieldDefs(), answers, ctx.submissions().size());
    }

    private List<Map<String, Object>> resolveFieldDefs(FormEntity form, List<SubmissionEntity> submissions) {
        List<Map<String, Object>> fields = FormAnalyticsSupport.extractFields(resolvePageDef(form));
        if (!fields.isEmpty() || submissions.isEmpty()) {
            return fields;
        }
        return FormAnalyticsSupport.inferFieldsFromAnswers(
                submissions.stream().map(SubmissionEntity::getAnswers).toList());
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

    private SubmissionListItemDto toListItem(SubmissionEntity submission, int totalFieldCount) {
        Map<String, Object> answers = submission.getAnswers();
        int answered = totalFieldCount == 0 || answers == null ? 0 : (int) answers.entrySet().stream()
                .filter(e -> !FormAnalyticsSupport.isBlankAnswer(e.getValue()))
                .count();

        return SubmissionListItemDto.builder()
                .id(submission.getId())
                .createdAt(submission.getCreatedAt())
                .publicationVersion(submission.getPublication().getVersion())
                .respondent(submission.getRespondent())
                .answers(answers)
                .metadata(submission.getMetadata())
                .answeredFieldCount(answered)
                .totalFieldCount(totalFieldCount)
                .completionRate(totalFieldCount == 0 ? 0
                        : FormAnalyticsSupport.roundPercent(100.0 * answered / totalFieldCount))
                .build();
    }
}
