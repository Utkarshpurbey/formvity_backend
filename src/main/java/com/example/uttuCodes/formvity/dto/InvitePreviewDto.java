package com.example.uttuCodes.formvity.dto;

import com.example.uttuCodes.formvity.enums.FormRoles;
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
public class InvitePreviewDto {

    private String email;
    private UUID workspaceId;
    private String workspaceName;
    private FormRoles role;
    private LocalDateTime expiresAt;
}
