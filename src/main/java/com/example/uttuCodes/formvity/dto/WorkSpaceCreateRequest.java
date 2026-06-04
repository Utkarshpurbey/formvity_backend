package com.example.uttuCodes.formvity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkSpaceCreateRequest {

    @NotBlank(message = "Workspace name is required")
    private String workSpaceName;
}
