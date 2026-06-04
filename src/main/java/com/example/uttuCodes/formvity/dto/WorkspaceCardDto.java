package com.example.uttuCodes.formvity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceCardDto {
    private UUID workspaceId;
    private String workspaceName;
    private long formCount;
}
