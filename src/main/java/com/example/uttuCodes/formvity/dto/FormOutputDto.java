package com.example.uttuCodes.formvity.dto;

import com.example.uttuCodes.formvity.enums.FormStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormOutputDto {
    private UUID id;
    private UUID workspaceId;
    private UUID createdByUser;
    private FormStatus status;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
