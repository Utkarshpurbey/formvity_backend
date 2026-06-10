package com.example.uttuCodes.formvity.dto;

import com.example.uttuCodes.formvity.entity.SubmissionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitFormResponseDto {
    private UUID submissionId;
    private LocalDateTime submittedAt;

    public static SubmitFormResponseDto from(SubmissionEntity submission) {
        return SubmitFormResponseDto.builder()
                .submissionId(submission.getId())
                .submittedAt(submission.getCreatedAt())
                .build();
    }
}
