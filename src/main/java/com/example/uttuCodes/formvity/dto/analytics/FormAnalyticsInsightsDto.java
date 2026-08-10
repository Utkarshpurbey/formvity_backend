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
public class FormAnalyticsInsightsDto {
    private int windowDays;
    private AudienceInsightsDto audience;
    private TrafficInsightsDto traffic;
    private TemporalInsightsDto temporal;
    private CompletionInsightsDto completion;
    private List<PublicationVersionInsightDto> publications;
    private FormTagAnalyticsSummaryDto tags;
}
