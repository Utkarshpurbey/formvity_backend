package com.example.uttuCodes.formvity.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormAnalyticsOverviewDto {
    private FormAnalyticsSummaryDto summary;
    private List<TimelineBucketDto> timeline;
    private List<QuestionAnalyticsDto> questions;
    private FormAnalyticsInsightsDto insights;
    private FormTagAnalyticsSummaryDto tags;
}
