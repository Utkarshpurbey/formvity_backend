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
public class DimensionInsightDto {
    private String dimension;
    private String label;
    private long totalWithValue;
    private List<QuestionDistributionItemDto> breakdown;
}
