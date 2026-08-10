package com.example.uttuCodes.formvity.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagAnalyticsDto {
    private UUID tagId;
    private String name;
    private String hexCode;
    private long taggedSubmissionsCount;
    private double percentage;
    private long manualCount;
    private long aiCount;
    private long importCount;
}
