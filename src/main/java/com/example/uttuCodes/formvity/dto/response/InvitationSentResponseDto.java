package com.example.uttuCodes.formvity.dto.response;

import com.example.uttuCodes.formvity.enums.FormRoles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvitationSentResponseDto {
    private UUID workspaceId;
    private UUID userId;
    private String emailId;
    private FormRoles roles;
    private LocalDateTime joinedAt;
    private String inviteUrl;
    private boolean isNewUser;
}
