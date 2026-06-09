package com.example.uttuCodes.formvity.service;

import com.example.uttuCodes.formvity.dto.analytics.FormAnalyticsOverviewDto;
import com.example.uttuCodes.formvity.dto.analytics.FormAnalyticsSummaryDto;
import com.example.uttuCodes.formvity.dto.analytics.QuestionAnalyticsDto;
import com.example.uttuCodes.formvity.dto.analytics.SubmissionListItemDto;
import com.example.uttuCodes.formvity.dto.analytics.TimelineBucketDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface FormAnalyticsService {

    FormAnalyticsOverviewDto getOverview(UUID workspaceId, UUID formId, UUID userId, int days);

    FormAnalyticsSummaryDto getSummary(UUID workspaceId, UUID formId, UUID userId);

    List<TimelineBucketDto> getTimeline(UUID workspaceId, UUID formId, UUID userId, int days);

    List<QuestionAnalyticsDto> getQuestionBreakdown(UUID workspaceId, UUID formId, UUID userId);

    Page<SubmissionListItemDto> listSubmissions(UUID workspaceId, UUID formId, UUID userId, int page, int size);
}
