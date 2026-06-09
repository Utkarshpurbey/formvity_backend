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
    private LocalDateTime firstResponseAt;
    private LocalDateTime lastResponseAt;
    private Integer currentPublicationVersion;
}
