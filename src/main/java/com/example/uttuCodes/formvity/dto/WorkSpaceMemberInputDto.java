package com.example.uttuCodes.formvity.dto;

import com.example.uttuCodes.formvity.enums.FormRoles;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkSpaceMemberInputDto {
    @NotBlank
    public UUID userId;
    public String displayName;
    public String userName;
    public String email;
    @NotBlank
    public String workSpaceName;
    private FormRoles role;

}

