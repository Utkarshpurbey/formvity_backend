package com.example.uttuCodes.formvity.dto;

import com.example.uttuCodes.formvity.enums.FormRoles;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkSpaceMemberInputDto {
    @NotBlank
    public UUID userId;
    @NotBlank
    public String workSpaceName;
    private FormRoles role;

}
