package com.example.uttuCodes.formvity.dto;

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
public class ActivateUserResponseDto {

    private String token;
    private UUID id;
    private String displayName;
    private UUID workspaceId;
    private LocalDateTime joinedAt;
}
