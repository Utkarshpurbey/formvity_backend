package com.example.uttuCodes.formvity.dto;

import com.example.uttuCodes.formvity.enums.OwnerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateInfoDto {
    private UUID id;
    private Map<String, Object> formDef;
    private UUID workspaceId;
    private UUID userId;
    private OwnerType ownerType;
}

