package com.example.uttuCodes.formvity.dto;

import com.example.uttuCodes.formvity.enums.FormStatus;
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
public class PublishStatusResponse {
    private UUID formId;
    private FormStatus formStatus;
    private Boolean isLive;
    private String slug;
    private String publicUrl;
    private Integer version;
    private LocalDateTime lastPublishedAt;
    private LocalDateTime lastUnpublishedAt;
    private boolean hasDraftChange;
}
