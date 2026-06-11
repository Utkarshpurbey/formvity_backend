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
public class AudienceInsightsDto {
    private long uniqueRespondents;
    private long returningRespondents;
    private long withEmail;
    private long withName;
    private long withPhone;
    private List<DimensionInsightDto> emailDomains;
    private List<DimensionInsightDto> respondentAttributes;
}
