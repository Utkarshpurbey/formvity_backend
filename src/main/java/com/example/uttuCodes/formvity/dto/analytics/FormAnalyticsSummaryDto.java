package com.example.uttuCodes.formvity.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormAnalyticsSummaryDto {
    private long totalResponses;
    private long responsesToday;
    private long responsesLast7Days;
    private long responsesLast30Days;
    private LocalDateTime firstResponseAt;
    private LocalDateTime lastResponseAt;
    private Integer currentPublicationVersion;
    private long uniqueRespondents;
    private long returningRespondents;
    private double avgCompletionRate;
    private Integer peakHour;
    private String peakDayOfWeek;
    private long submissionsWithMetadata;
}
