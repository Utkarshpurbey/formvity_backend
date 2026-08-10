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
public class FormTagAnalyticsSummaryDto {
    private long totalSubmissions;
    private long totalTaggedSubmissions;
    private long totalUntaggedSubmissions;
    private long totalTagAssignments;
    private List<TagAnalyticsDto> tags;
}
