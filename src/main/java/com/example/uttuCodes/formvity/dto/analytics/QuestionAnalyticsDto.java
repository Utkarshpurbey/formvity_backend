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
public class QuestionAnalyticsDto {
    private String fieldId;
    private String type;
    private String label;
    private long responseCount;
    private long skippedCount;
    private Double average;
    private Double min;
    private Double max;
    private List<QuestionDistributionItemDto> distribution;
}
