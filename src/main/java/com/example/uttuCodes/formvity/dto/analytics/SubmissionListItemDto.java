package com.example.uttuCodes.formvity.dto.analytics;

import com.example.uttuCodes.formvity.dto.TagResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionListItemDto {
    private UUID id;
    private LocalDateTime createdAt;
    private int publicationVersion;
    private Map<String, Object> respondent;
    private Map<String, Object> answers;
    private Map<String, Object> metadata;
    private int answeredFieldCount;
    private int totalFieldCount;
    private double completionRate;
    private List<TagResponseDto> tags;
}
