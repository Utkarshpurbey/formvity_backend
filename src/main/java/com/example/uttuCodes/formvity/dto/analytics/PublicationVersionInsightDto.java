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
public class PublicationVersionInsightDto {
    private int version;
    private String slug;
    private LocalDateTime publishedAt;
    private long responseCount;
    private double percent;
    private boolean current;
}
