package com.example.uttuCodes.formvity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceDashboardDto {
    private UUID workspaceId;
    private String workspaceName;
    private long formCount;
    private List<FormOutputDto> recentForms;
}
