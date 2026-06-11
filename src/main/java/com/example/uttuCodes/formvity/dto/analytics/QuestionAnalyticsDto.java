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
    private boolean required;
    private long responseCount;
    private long skippedCount;
    private double completionRate;
    private Double average;
    private Double median;
    private Double min;
    private Double max;
    private List<QuestionDistributionItemDto> distribution;
    private List<TextAnswerSampleDto> topTextAnswers;
}
