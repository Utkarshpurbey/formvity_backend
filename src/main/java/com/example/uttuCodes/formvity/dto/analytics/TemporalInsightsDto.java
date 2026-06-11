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
public class TemporalInsightsDto {
    private Integer peakHour;
    private String peakDayOfWeek;
    private List<HourBucketDto> byHourOfDay;
    private List<DayOfWeekBucketDto> byDayOfWeek;
}
