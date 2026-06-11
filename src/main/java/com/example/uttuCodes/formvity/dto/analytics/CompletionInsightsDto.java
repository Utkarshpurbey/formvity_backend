package com.example.uttuCodes.formvity.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionInsightsDto {
    private int totalFields;
    private double avgFieldsAnswered;
    private double avgCompletionRate;
    private long fullyCompletedCount;
    private double fullyCompletedRate;
}
